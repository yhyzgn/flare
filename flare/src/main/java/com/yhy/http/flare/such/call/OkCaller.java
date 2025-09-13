package com.yhy.http.flare.such.call;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.call.Callback;
import com.yhy.http.flare.call.Caller;
import com.yhy.http.flare.convert.Converter;
import com.yhy.http.flare.http.request.RequestFactory;
import com.yhy.http.flare.model.InternalResponse;
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
 *
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
    private final Converter<ResponseBody, T> responseConverter;
    private final Object[] args;

    private volatile boolean canceled;
    @Nullable
    private okhttp3.Call rawCall;
    @Nullable
    private Throwable failureHandler;
    private boolean executed;

    public OkCaller(RequestFactory requestFactory, Flare flare, Converter<ResponseBody, T> responseConverter, Object[] args) {
        this.requestFactory = requestFactory;
        this.flare = flare;
        this.responseConverter = responseConverter;
        this.args = args;
    }

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
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                callFailure(e);
            }

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

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

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

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

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
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private OkHttpClient.Builder newBuilder() {
        // Create a new OkHttpClient.Builder with the Flare's configuration.
        OkHttpClient ok = flare.clientBuilder().build();
        // Copy the OkHttpClient's configuration to the builder.
        return new OkHttpClient.Builder()
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
                .sslSocketFactory(ok.sslSocketFactory(), Opt.ofNullable(ok.x509TrustManager()).or(flare::sslTrustManager).get())
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
            return InternalResponse.success(rawResponse, null);
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
        try {
            T body = responseConverter.convert(catchingBody);
            return InternalResponse.success(rawResponse, body);
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

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

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

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public @NotNull BufferedSource source() {
            return delegateSource;
        }

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
