package com.yhyzgn.http.flare;

import com.yhyzgn.http.flare.annotation.Header;
import com.yhyzgn.http.flare.call.CallAdapter;
import com.yhyzgn.http.flare.convert.Converter;
import com.yhyzgn.http.flare.delegate.DynamicHeaderDelegate;
import com.yhyzgn.http.flare.delegate.InterceptorDelegate;
import com.yhyzgn.http.flare.delegate.MethodAnnotationDelegate;
import com.yhyzgn.http.flare.such.adapter.GuavaCallAdapter;
import com.yhyzgn.http.flare.such.convert.JacksonConverter;
import com.yhyzgn.http.flare.such.logging.HttpLoggerInterceptor;
import com.yhyzgn.http.flare.utils.Assert;
import com.yhyzgn.http.flare.utils.Opt;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * 一个 HTTP 请求客户端
 * <p>
 * Created on 2025-09-10 14:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class Flare {
    private final HttpUrl baseUrl;
    private final List<Interceptor> netInterceptors;
    private final List<Interceptor> interceptors;
    private final Map<String, String> headers;
    private final List<Header.Dynamic> dynamicHeaders;
    private final Dispatcher dispatcher;
    private final DynamicHeaderDelegate dynamicHeaderDelegate;
    private final InterceptorDelegate interceptorDelegate;
    private final MethodAnnotationDelegate methodAnnotationDelegate;
    private final OkHttpClient.Builder client;
    private final Boolean logEnabled;
    private final Interceptor loggerInterceptor;
    private final CallAdapter.Factory callAdapterFactory;
    private final Converter.Factory converterFactory;
    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager sslTrustManager;
    private final HostnameVerifier sslHostnameVerifier;
    private final Duration timeout;

    private Flare(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.dynamicHeaders = builder.dynamicHeaders;
        this.dispatcher = builder.dispatcher;
        this.dynamicHeaderDelegate = builder.dynamicHeaderDelegate;
        this.interceptorDelegate = builder.interceptorDelegate;
        this.methodAnnotationDelegate = builder.methodAnnotationDelegate;
        this.client = builder.client;
        this.logEnabled = builder.logEnabled;
        this.loggerInterceptor = builder.loggerInterceptor;
        this.callAdapterFactory = builder.callAdapterFactory;
        this.converterFactory = builder.converterFactory;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.sslTrustManager = builder.sslTrustManager;
        this.sslHostnameVerifier = builder.sslHostnameVerifier;
        this.timeout = builder.timeout;
    }

    @NotNull
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    @NotNull
    public List<Interceptor> netInterceptors() {
        return netInterceptors;
    }

    @NotNull
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    @NotNull
    public Map<String, String> headers() {
        return headers;
    }

    @NotNull
    public List<Header.Dynamic> dynamicHeaders() {
        return dynamicHeaders;
    }

    public DynamicHeaderDelegate headerDelegate() {
        return dynamicHeaderDelegate;
    }

    public InterceptorDelegate interceptorDelegate() {
        return interceptorDelegate;
    }

    public MethodAnnotationDelegate methodAnnotationDelegate() {
        return methodAnnotationDelegate;
    }

    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return callAdapterFactory.get(returnType, annotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        return (Converter<T, String>) converterFactory.stringConverter(type, annotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> Converter<T, RequestBody> requestConverter(Type type, Annotation[] parameterAnnotations) {
        return (Converter<T, RequestBody>) converterFactory.requestBodyConverter(type, parameterAnnotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> Converter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        return (Converter<ResponseBody, T>) converterFactory.responseBodyConverter(responseType, annotations, this);
    }

    public OkHttpClient.Builder client() {
        return newBuilder();
    }

    public <T> T create(Class<T> api) {
        Objects.requireNonNull(api, "api can not be null.");
        validateInterface(api);
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return loadHttpMethod(method).invoke(null != args ? args : new Object[0]);
        });
    }

    private void validateInterface(Class<?> api) {
        if (!api.isInterface()) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] must be interface.");
        }
        if (api.getTypeParameters().length != 0) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] can not contains any typeParameter.");
        }
        for (Method method : api.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                loadHttpMethod(method);
            }
        }
    }

    private OkHttpClient.Builder newBuilder() {
        OkHttpClient ok = client.build();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
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
                .hostnameVerifier(ok.hostnameVerifier())
                .certificatePinner(ok.certificatePinner())
                .callTimeout(Duration.ofMillis(ok.callTimeoutMillis()))
                .connectTimeout(Duration.ofMillis(ok.connectTimeoutMillis()))
                .readTimeout(Duration.ofMillis(ok.readTimeoutMillis()))
                .writeTimeout(Duration.ofMillis(ok.writeTimeoutMillis()))
                .pingInterval(Duration.ofMillis(ok.pingIntervalMillis()))
                .certificatePinner(ok.certificatePinner());

        // 配置 ssl
        if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
            builder.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
        }

        return builder;
    }


    public static class Builder {
        private final List<Interceptor> netInterceptors = new ArrayList<>();
        private final List<Interceptor> interceptors = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();
        private final List<Header.Dynamic> dynamicHeaders = new ArrayList<>();

        private HttpUrl baseUrl;
        private Dispatcher dispatcher;
        private DynamicHeaderDelegate dynamicHeaderDelegate;
        private InterceptorDelegate interceptorDelegate;
        private MethodAnnotationDelegate methodAnnotationDelegate;
        private OkHttpClient.Builder client;
        private Boolean logEnabled;
        private Interceptor loggerInterceptor;
        private CallAdapter.Factory callAdapterFactory;
        private Converter.Factory converterFactory;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager sslTrustManager;
        private HostnameVerifier sslHostnameVerifier;
        private Duration timeout;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder netInterceptor(Interceptor interceptor) {
            this.netInterceptors.add(interceptor);
            return this;
        }

        /**
         * 配置分发器
         *
         * @param dispatcher 分发器
         * @return builder
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder dynamicHeaderDelegate(DynamicHeaderDelegate delegate) {
            this.dynamicHeaderDelegate = delegate;
            return this;
        }

        public Builder interceptorDelegate(InterceptorDelegate delegate) {
            this.interceptorDelegate = delegate;
            return this;
        }

        public Builder methodAnnotationDelegate(MethodAnnotationDelegate delegate) {
            this.methodAnnotationDelegate = delegate;
            return this;
        }

        /**
         * 静态请求头
         *
         * @param name  名称
         * @param value 静态值
         * @return builder
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * 动态请求头
         *
         * @param header 动态支持
         * @return builder
         */
        public Builder header(Header.Dynamic header) {
            this.dynamicHeaders.add(header);
            return this;
        }

        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.callAdapterFactory = factory;
            return this;
        }

        public Builder converterFactory(Converter.Factory factory) {
            this.converterFactory = factory;
            return this;
        }

        public Builder https(SSLSocketFactory factory, X509TrustManager manager, HostnameVerifier verifier) {
            this.sslSocketFactory = factory;
            this.sslTrustManager = manager;
            this.sslHostnameVerifier = verifier;
            return this;
        }

        public Builder client(OkHttpClient.Builder client) {
            this.client = client;
            return this;
        }

        public Builder timeout(long millis) {
            this.timeout = Duration.ofMillis(millis);
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder logEnabled(boolean enabled) {
            this.logEnabled = enabled;
            return this;
        }

        public Builder loggerInterceptor(Interceptor interceptor) {
            this.loggerInterceptor = interceptor;
            return this;
        }

        public Flare build() {
            Assert.notNull(baseUrl, "baseUrl cannot be null");

            callAdapterFactory = Opt.ofNullable(callAdapterFactory).orElse(new GuavaCallAdapter());
            converterFactory = Opt.ofNullable(converterFactory).orElse(new JacksonConverter());

            client = Opt.ofNullable(client).orElse(new OkHttpClient.Builder());

            // 配置分发器
            client.dispatcher(Opt.ofNullable(dispatcher).orElse(virtualDispatcher()));

            // 配置 ssl
            if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
                client.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
            }

            if (Objects.equals(logEnabled, true)) {
                // 配置日志拦截器
                netInterceptors.add(Opt.ofNullable(loggerInterceptor).orElse(new HttpLoggerInterceptor()));
            }

            // 配置全局拦截器
            netInterceptors.forEach(client::addNetworkInterceptor);
            interceptors.forEach(client::addInterceptor);

            // 配置超时
            Opt.ofNullable(timeout).ifValid(t -> client.connectTimeout(t).callTimeout(t).readTimeout(t).writeTimeout(t));

            // 创建 Flare 实例
            return new Flare(this);
        }

        /**
         * 创建虚拟线程池分发器
         *
         * @return 分发器
         */
        private static Dispatcher virtualDispatcher() {
            return new Dispatcher(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("flare-virtual-thread-", 0).factory()));
        }
    }
}
