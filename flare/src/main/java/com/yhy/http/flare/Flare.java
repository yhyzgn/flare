package com.yhy.http.flare;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.cache.HandlerCache;
import com.yhy.http.flare.call.CallAdapter;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import com.yhy.http.flare.delegate.InterceptorDelegate;
import com.yhy.http.flare.delegate.MethodAnnotationDelegate;
import com.yhy.http.flare.http.HttpHandler;
import com.yhy.http.flare.http.HttpHandlerAdapter;
import com.yhy.http.flare.such.adapter.GuavaCallAdapter;
import com.yhy.http.flare.such.convert.FormFieldConverterFactory;
import com.yhy.http.flare.such.convert.JacksonConverterFactory;
import com.yhy.http.flare.such.convert.StringConverterFactory;
import com.yhy.http.flare.such.delegate.ConstructorDynamicHeaderDelegate;
import com.yhy.http.flare.such.delegate.ConstructorInterceptorDelegate;
import com.yhy.http.flare.such.delegate.ConstructorMethodAnnotationDelegate;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.utils.Assert;
import com.yhy.http.flare.utils.Opt;
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
    private final DynamicHeaderDelegate dynamicHeaderDelegate;
    private final InterceptorDelegate interceptorDelegate;
    private final MethodAnnotationDelegate methodAnnotationDelegate;
    private final OkHttpClient.Builder clientBuilder;
    private final CallAdapter.Factory callAdapterFactory;
    private final BodyConverter.Factory jsonConverterFactory;
    private final StringConverter.Factory stringConverterFactory;
    private final FormFieldConverter.Factory formFieldConverterFactory;
    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager sslTrustManager;
    private final HostnameVerifier sslHostnameVerifier;

    private Flare(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.dynamicHeaders = builder.dynamicHeaders;
        this.dynamicHeaderDelegate = builder.dynamicHeaderDelegate;
        this.interceptorDelegate = builder.interceptorDelegate;
        this.methodAnnotationDelegate = builder.methodAnnotationDelegate;
        this.clientBuilder = builder.clientBuilder;
        this.callAdapterFactory = builder.callAdapterFactory;
        this.jsonConverterFactory = builder.jsonConverterFactory;
        this.stringConverterFactory = builder.stringConverterFactory;
        this.formFieldConverterFactory = builder.formFieldConverterFactory;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.sslTrustManager = builder.sslTrustManager;
        this.sslHostnameVerifier = builder.sslHostnameVerifier;
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

    public Opt<SSLSocketFactory> sslSocketFactory() {
        return Opt.ofNullable(sslSocketFactory);
    }

    public Opt<X509TrustManager> sslTrustManager() {
        return Opt.ofNullable(sslTrustManager);
    }

    public Opt<HostnameVerifier> sslHostnameVerifier() {
        return Opt.ofNullable(sslHostnameVerifier);
    }

    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return callAdapterFactory.get(returnType, annotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> StringConverter<T> stringConverter(Type type, Annotation[] annotations) {
        return (StringConverter<T>) stringConverterFactory.converter(type, annotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> BodyConverter<T, RequestBody> requestConverter(Type type, Annotation[] parameterAnnotations) {
        return (BodyConverter<T, RequestBody>) jsonConverterFactory.requestBodyConverter(type, parameterAnnotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> BodyConverter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        return (BodyConverter<ResponseBody, T>) jsonConverterFactory.responseBodyConverter(responseType, annotations, this);
    }

    @SuppressWarnings("unchecked")
    public <T> FormFieldConverter<T> formFieldConverter(Type type, Annotation[] annotations) {
        return (FormFieldConverter<T>) formFieldConverterFactory.converter(type, annotations, this);
    }

    public OkHttpClient.Builder clientBuilder() {
        return clientBuilder;
    }

    @SuppressWarnings("unchecked")
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
        Arrays.stream(api.getDeclaredMethods()).filter(method -> !Modifier.isStatic(method.getModifiers())).forEach(this::loadHttpMethod);
    }

    private HttpHandler<?> loadHttpMethod(Method method) {
        HttpHandler<?> result = HandlerCache.get(method);
        if (null != result) {
            return result;
        }
        // 解析方法注解
        result = HttpHandlerAdapter.parseAnnotations(this, method);
        HandlerCache.put(method, result);
        return result;
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
        private OkHttpClient.Builder clientBuilder;
        private Boolean logEnabled;
        private Interceptor loggerInterceptor;
        private CallAdapter.Factory callAdapterFactory;
        private BodyConverter.Factory jsonConverterFactory;
        private StringConverter.Factory stringConverterFactory;
        private FormFieldConverter.Factory formFieldConverterFactory;
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

        public Builder converterFactory(BodyConverter.Factory factory) {
            this.jsonConverterFactory = factory;
            return this;
        }

        public Builder stringConverterFactory(StringConverter.Factory factory) {
            this.stringConverterFactory = factory;
            return this;
        }

        public Builder formFieldConverterFactory(FormFieldConverter.Factory factory) {
            this.formFieldConverterFactory = factory;
            return this;
        }

        public Builder https(SSLSocketFactory factory, X509TrustManager manager, HostnameVerifier verifier) {
            this.sslSocketFactory = factory;
            this.sslTrustManager = manager;
            this.sslHostnameVerifier = verifier;
            return this;
        }

        public Builder clientBuilder(OkHttpClient.Builder client) {
            this.clientBuilder = client;
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
            jsonConverterFactory = Opt.ofNullable(jsonConverterFactory).orElse(new JacksonConverterFactory());
            stringConverterFactory = Opt.ofNullable(stringConverterFactory).orElse(new StringConverterFactory());
            formFieldConverterFactory = Opt.ofNullable(formFieldConverterFactory).orElse(new FormFieldConverterFactory());

            dynamicHeaderDelegate = Opt.ofNullable(dynamicHeaderDelegate).orElse(ConstructorDynamicHeaderDelegate.create());
            interceptorDelegate = Opt.ofNullable(interceptorDelegate).orElse(ConstructorInterceptorDelegate.create());
            methodAnnotationDelegate = Opt.ofNullable(methodAnnotationDelegate).orElse(ConstructorMethodAnnotationDelegate.create());

            clientBuilder = Opt.ofNullable(clientBuilder).orElse(new OkHttpClient.Builder());

            // 配置分发器
            clientBuilder.dispatcher(Opt.ofNullable(dispatcher).orElse(virtualDispatcher()));

            // 配置 ssl
            if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
                clientBuilder.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
            }

            if (Objects.equals(logEnabled, true)) {
                // 配置日志拦截器
                netInterceptors.add(Opt.ofNullable(loggerInterceptor).orElse(new HttpLoggerInterceptor()));
            }

            // 配置全局拦截器
            netInterceptors.forEach(clientBuilder::addNetworkInterceptor);
            interceptors.forEach(clientBuilder::addInterceptor);

            // 配置超时
            Opt.ofNullable(timeout).ifValid(t -> clientBuilder.connectTimeout(t).callTimeout(t).readTimeout(t).writeTimeout(t));

            // 创建 Flare 实例
            return new Flare(this);
        }

        /**
         * 创建虚拟线程池分发器
         *
         * @return 分发器
         */
        private static Dispatcher virtualDispatcher() {
            return new Dispatcher(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()));
        }
    }
}
