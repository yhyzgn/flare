package com.yhy.http.flare.such.call;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.call.Callback;
import com.yhy.http.flare.call.Caller;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.http.request.RequestFactory;
import com.yhy.http.flare.model.InternalResponse;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import com.yhy.http.flare.utils.BufferUtils;
import com.yhy.http.flare.utils.Opt;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * 基于 OkHttp 的 {@link Caller} 默认实现。
 * <p>
 * 封装一次接口方法调用：由 {@link RequestFactory} 构建 OkHttp 请求，发送后把原始响应
 * 解析为 {@link InternalResponse}（含响应体转换）。支持同步执行、异步入队、取消与克隆；
 * 每个实例只能执行一次，重复执行抛出 {@link IllegalStateException}。
 * <p>
 * Created on 2025-09-11 06:29
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class OkCaller<T> implements Caller<T> {
    private final Flare flare;
    private final RequestFactory requestFactory;
    private final BodyConverter<ResponseBody, T> responseConverter;
    private final Object[] args;

    private volatile boolean canceled;
    @Nullable
    private okhttp3.Call rawCall;
    @Nullable
    private Throwable failureHandler;
    private boolean executed;

    /**
     * 创建 OkCaller 实例。
     *
     * @param requestFactory   请求工厂，负责把方法参数构建为 OkHttp 请求
     * @param flare            所属 Flare 客户端，提供全局配置
     * @param responseConverter 响应体转换器，把原始响应体转换为方法声明的返回类型
     * @param args             代理方法调用参数
     */
    public OkCaller(RequestFactory requestFactory, Flare flare, BodyConverter<ResponseBody, T> responseConverter, Object[] args) {
        this.requestFactory = requestFactory;
        this.flare = flare;
        this.responseConverter = responseConverter;
        this.args = args;
    }

    /**
     * 同步执行请求并解析响应。
     * <p>
     * 首次调用时创建底层 {@code okhttp3.Call}；若此前已执行过或创建请求失败过，按失败语义直接抛出。
     *
     * @return 解析后的响应包装
     * @throws IOException 网络请求或读取响应体时发生 I/O 错误
     */
    @Override
    public InternalResponse<T> execute() throws IOException {
        okhttp3.Call call;
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            if (failureHandler != null) {
                if (failureHandler instanceof IOException) {
                    throw (IOException) failureHandler;
                } else if (failureHandler instanceof RuntimeException) {
                    throw (RuntimeException) failureHandler;
                } else {
                    throw (Error) failureHandler;
                }
            }
            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (RuntimeException | Error e) {
                    failureHandler = e;
                    throw e;
                }
            }
        }
        if (canceled) {
            call.cancel();
        }
        return parseResponse(call.execute());
    }

    /**
     * 获取本次调用将发送的 OkHttp 请求。
     * <p>
     * 底层 Call 尚未创建时会先构建请求；此前创建请求失败过则抛出当时的失败原因。
     *
     * @return 待发送的 OkHttp 请求
     */
    @Override
    public synchronized Request request() {
        okhttp3.Call call = rawCall;
        if (null != call) {
            return call.request();
        }
        if (failureHandler != null) {
            if (failureHandler instanceof IOException) {
                throw new RuntimeException("Unable to create request.", failureHandler);
            } else if (failureHandler instanceof RuntimeException) {
                throw (RuntimeException) failureHandler;
            } else {
                throw (Error) failureHandler;
            }
        }
        try {
            call = rawCall = createRawCall();
            return call.request();
        } catch (RuntimeException | Error e) {
            failureHandler = e;
            throw e;
        }
    }

    /**
     * 异步执行请求。
     * <p>
     * 请求构建失败会直接回调 {@code onFailure}；响应在回调线程中解析，
     * 解析失败同样以 {@code onFailure} 通知。
     *
     * @param callback 结果回调，不允许为 {@code null}
     */
    @Override
    public void enqueue(Callback<T> callback) {
        Objects.requireNonNull(callback, "callback can not be null.");
        okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            failure = failureHandler;
            if (call == null && failure == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    failure = failureHandler = t;
                }
            }
        }
        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }
        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            /**
             * 网络请求失败时转发给调用方回调。
             *
             * @param call 底层 OkHttp 调用
             * @param e    失败原因
             */
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                callFailure(e);
            }

            /**
             * 原始响应到达时先解析为 {@link InternalResponse} 再回调调用方；
             * 解析失败按 {@code onFailure} 处理。
             *
             * @param call        底层 OkHttp 调用
             * @param rawResponse OkHttp 原始响应
             */
            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response rawResponse) {
                InternalResponse<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }
                try {
                    callback.onResponse(OkCaller.this, response);
                } catch (Throwable t) {
                    log.error("", t);
                }
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkCaller.this, e);
                } catch (Throwable t) {
                    log.error("", t);
                }
            }
        });
    }

    /**
     * 请求是否已执行（同步或异步均计入）。
     *
     * @return 已执行返回 {@code true}
     */
    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    /**
     * 取消本次调用。
     * <p>
     * 底层 Call 尚未创建时先标记取消标志，创建后由执行流程联动取消。
     */
    @Override
    public void cancel() {
        canceled = true;
        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * 本次调用是否已取消。
     *
     * @return 已取消返回 {@code true}
     */
    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    /**
     * 克隆出可再次执行的调用实例。
     * <p>
     * 克隆实例共享请求工厂、客户端配置与转换器，但拥有独立的执行状态。
     *
     * @return 新的 {@link OkCaller} 实例
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public OkCaller<T> clone() {
        return new OkCaller<>(requestFactory, flare, responseConverter, args);
    }

    private okhttp3.Call createRawCall() {
        OkHttpClient.Builder builder = newBuilder();
        try {
            Request request = requestFactory.create(builder, args);
            return builder.build().newCall(request);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private OkHttpClient.Builder newBuilder() {
        // Create a new OkHttpClient.Builder with the Flare's configuration.
        OkHttpClient ok = flare.clientBuilder().build();
        // Copy the OkHttpClient's configuration to the builder.
        return new OkHttpClient.Builder()
            .taskRunner$okhttp(ok.getTaskRunner$okhttp())
            .dispatcher(ok.dispatcher())
            .connectionPool(ok.connectionPool())
            .eventListenerFactory(ok.eventListenerFactory())
            .retryOnConnectionFailure(ok.retryOnConnectionFailure())
            .authenticator(ok.authenticator())
            .followRedirects(ok.followRedirects())
            .followSslRedirects(ok.followSslRedirects())
            .cookieJar(ok.cookieJar())
            .cache(ok.cache())
            .dns(ok.dns())
            .proxy(ok.proxy())
            .proxySelector(ok.proxySelector())
            .proxyAuthenticator(ok.proxyAuthenticator())
            .socketFactory(ok.socketFactory())
            .connectionSpecs(ok.connectionSpecs())
            .protocols(ok.protocols())
            .sslSocketFactory(ok.sslSocketFactory(), Opt.ofNullable(ok.x509TrustManager()).orElse(flare.sslTrustManager().orElse(new VoidSSLX509TrustManager())))
            .hostnameVerifier(ok.hostnameVerifier())
            .certificatePinner(ok.certificatePinner())
            .callTimeout(Duration.ofMillis(ok.callTimeoutMillis()))
            .connectTimeout(Duration.ofMillis(ok.connectTimeoutMillis()))
            .readTimeout(Duration.ofMillis(ok.readTimeoutMillis()))
            .writeTimeout(Duration.ofMillis(ok.writeTimeoutMillis()))
            .pingInterval(Duration.ofMillis(ok.pingIntervalMillis()));
    }

    private InternalResponse<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
            .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
            .build();

        // 根据配置决定是否忽略 HTTP 状态码
        if (!flare.ignoreHttpStatus()) {
            int code = rawResponse.code();
            if (code < 200 || code >= 300) {
                try {
                    // Buffer the entire body to avoid future I/O.
                    return InternalResponse.error(rawResponse, BufferUtils.buffer(rawBody));
                } finally {
                    rawBody.close();
                }
            }
            if (code == 204 || code == 205) {
                rawBody.close();
                return InternalResponse.success(rawResponse, null, true);
            }
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
        try {
            T body = responseConverter.convert(catchingBody);
            return InternalResponse.success(rawResponse, body, flare.ignoreHttpStatus());
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    static final class NoContentResponseBody extends ResponseBody {
        @Nullable
        private final MediaType contentType;
        private final long contentLength;

        NoContentResponseBody(@Nullable MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        /**
         * 响应体的 Content-Type。
         *
         * @return 原始响应体的 Content-Type，可能为 {@code null}
         */
        @Override
        public MediaType contentType() {
            return contentType;
        }

        /**
         * 响应体的内容长度。
         *
         * @return 原始响应体的内容长度
         */
        @Override
        public long contentLength() {
            return contentLength;
        }

        /**
         * 获取可读取的数据源。
         * <p>
         * 该响应体仅承载元信息，不持有实际数据，读取时直接抛出 {@link IllegalStateException}。
         *
         * @return 不支持调用，始终抛出异常
         */
        @Override
        public @NotNull BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    static final class ExceptionCatchingResponseBody extends ResponseBody {
        private final ResponseBody delegate;
        private final BufferedSource delegateSource;
        @Nullable
        IOException thrownException;

        ExceptionCatchingResponseBody(ResponseBody delegate) {
            this.delegate = delegate;
            this.delegateSource = Okio.buffer(new ForwardingSource(delegate.source()) {
                /**
                 * 读取数据并在发生 {@link IOException} 时记录该异常。
                 *
                 * @param sink      目标缓冲区
                 * @param byteCount 最多读取的字节数
                 * @return 实际读取的字节数
                 * @throws IOException 底层读取失败时抛出
                 */
                @Override
                public long read(@NotNull Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        /**
         * 响应体的 Content-Type。
         *
         * @return 委托响应体的 Content-Type
         */
        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        /**
         * 响应体的内容长度。
         *
         * @return 委托响应体的内容长度
         */
        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        /**
         * 获取包装后的数据源。
         * <p>
         * 读取过程中发生的 {@link IOException} 会被记录，供 {@code throwIfCaught()} 重放。
         *
         * @return 捕获读异常的缓冲数据源
         */
        @Override
        public @NotNull BufferedSource source() {
            return delegateSource;
        }

        /**
         * 关闭底层委托响应体。
         */
        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
