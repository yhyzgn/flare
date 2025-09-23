package com.yhy.http.flare;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.cache.HandlerCache;
import com.yhy.http.flare.call.CallAdapter;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import com.yhy.http.flare.delegate.InterceptorDelegate;
import com.yhy.http.flare.delegate.MethodAnnotationDelegate;
import com.yhy.http.flare.http.HttpHandler;
import com.yhy.http.flare.http.HttpHandlerAdapter;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.such.adapter.GuavaCallAdapter;
import com.yhy.http.flare.such.convert.FormFieldConverterFactory;
import com.yhy.http.flare.such.convert.JacksonConverterFactory;
import com.yhy.http.flare.such.convert.StringConverterFactory;
import com.yhy.http.flare.such.delegate.ConstructorDispatcherProviderDelegate;
import com.yhy.http.flare.such.delegate.ConstructorDynamicHeaderDelegate;
import com.yhy.http.flare.such.delegate.ConstructorInterceptorDelegate;
import com.yhy.http.flare.such.delegate.ConstructorMethodAnnotationDelegate;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.provider.VirtualThreadDispatcherProvider;
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

/**
 * 一个 HTTP 请求客户端
 * <p>
 * Created on 2025-09-10 14:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unused")
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
    private final BodyConverter.Factory bodyConverterFactory;
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
        this.bodyConverterFactory = builder.bodyConverterFactory;
        this.stringConverterFactory = builder.stringConverterFactory;
        this.formFieldConverterFactory = builder.formFieldConverterFactory;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.sslTrustManager = builder.sslTrustManager;
        this.sslHostnameVerifier = builder.sslHostnameVerifier;
    }

    /**
     * Base URL
     *
     * @return Base URL
     */
    @NotNull
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * 网络拦截器
     *
     * @return 网络拦截器
     */
    @NotNull
    public List<Interceptor> netInterceptors() {
        return netInterceptors;
    }

    /**
     * 非网络拦截器
     *
     * @return 非网络拦截器
     */
    @NotNull
    public List<Interceptor> interceptors() {
        return interceptors;
    }

    /**
     * 静态请求头
     *
     * @return 静态请求头
     */
    @NotNull
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * 动态请求头
     *
     * @return 动态请求头
     */
    @NotNull
    public List<Header.Dynamic> dynamicHeaders() {
        return dynamicHeaders;
    }

    /**
     * 动态请求头处理器委托实例
     *
     * @return 动态请求头处理器委托实例
     */
    public DynamicHeaderDelegate headerDelegate() {
        return dynamicHeaderDelegate;
    }

    /**
     * 拦截器处理器委托实例
     *
     * @return 拦截器处理器委托实例
     */
    public InterceptorDelegate interceptorDelegate() {
        return interceptorDelegate;
    }

    /**
     * 方法注解处理器委托实例
     *
     * @return 方法注解处理器委托实例
     */
    public MethodAnnotationDelegate methodAnnotationDelegate() {
        return methodAnnotationDelegate;
    }

    /**
     * sslSocketFactory
     *
     * @return sslSocketFactory
     */
    public Opt<SSLSocketFactory> sslSocketFactory() {
        return Opt.ofNullable(sslSocketFactory);
    }

    /**
     * sslTrustManager
     *
     * @return sslTrustManager
     */
    public Opt<X509TrustManager> sslTrustManager() {
        return Opt.ofNullable(sslTrustManager);
    }

    /**
     * sslHostnameVerifier
     *
     * @return sslHostnameVerifier
     */
    public Opt<HostnameVerifier> sslHostnameVerifier() {
        return Opt.ofNullable(sslHostnameVerifier);
    }

    /**
     * 请求发送处理器
     *
     * @param returnType  请求返回类型
     * @param annotations 请求注解
     * @return 请求发送处理器
     */
    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return callAdapterFactory.get(returnType, annotations, this);
    }

    /**
     * 字符串转换器
     *
     * @param <T> 字符串转换器类型
     * @return 字符串转换器
     */
    @SuppressWarnings("unchecked")
    public <T> StringConverter<T> stringConverter() {
        return (StringConverter<T>) stringConverterFactory.converter(this);
    }

    /**
     * 请求数据转换器
     *
     * @param type                 请求数据类型
     * @param parameterAnnotations 请求数据注解
     * @param <T>                  请求数据类型
     * @return 请求数据转换器
     */
    @SuppressWarnings("unchecked")
    public <T> BodyConverter<T, RequestBody> requestConverter(Type type, Annotation[] parameterAnnotations) {
        return (BodyConverter<T, RequestBody>) bodyConverterFactory.requestBodyConverter(type, parameterAnnotations, this);
    }

    /**
     * 响应数据转换器
     *
     * @param responseType 响应数据类型
     * @param annotations  响应数据注解
     * @param <T>          响应数据类型
     * @return 响应数据转换器
     */
    @SuppressWarnings("unchecked")
    public <T> BodyConverter<ResponseBody, T> responseConverter(Type responseType, Annotation[] annotations) {
        return (BodyConverter<ResponseBody, T>) bodyConverterFactory.responseBodyConverter(responseType, annotations, this);
    }

    /**
     * form 表单数据转换器
     *
     * @param type        表单数据类型
     * @param annotations 表单数据注解
     * @param <T>         表单数据类型
     * @return 表单数据转换器
     */
    @SuppressWarnings("unchecked")
    public <T> FormFieldConverter<T> formFieldConverter(Type type, Annotation[] annotations) {
        return (FormFieldConverter<T>) formFieldConverterFactory.converter(type, annotations, this);
    }

    /**
     * 创建请求客户端
     *
     * @return 请求客户端
     */
    public OkHttpClient.Builder clientBuilder() {
        return clientBuilder;
    }

    /**
     * 创建接口实例
     *
     * @param api 接口类，必须是接口类型
     * @param <T> 接口类型
     * @return 接口实例
     */
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

    /**
     * 校验接口是否合法
     *
     * @param api 接口类
     */
    private void validateInterface(Class<?> api) {
        if (!api.isInterface()) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] must be interface.");
        }
        if (api.getTypeParameters().length != 0) {
            throw new IllegalArgumentException("[" + api.getCanonicalName() + "] can not contains any typeParameter.");
        }
        Arrays.stream(api.getDeclaredMethods()).filter(method -> !Modifier.isStatic(method.getModifiers())).forEach(this::loadHttpMethod);
    }

    /**
     * 从缓存中加载方法对应的 HttpHandler
     *
     * @param method 方法
     * @return HttpHandler
     */
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
        private DynamicHeaderDelegate dynamicHeaderDelegate;
        private InterceptorDelegate interceptorDelegate;
        private MethodAnnotationDelegate methodAnnotationDelegate;
        private Dispatcher dispatcher;
        private DispatcherProviderDelegate dispatcherProviderDelegate;
        private Class<? extends DispatcherProvider> dispatcherProviderClass;
        private DispatcherProvider dispatcherProvider;
        private OkHttpClient.Builder clientBuilder;
        private Boolean logEnabled;
        private Interceptor loggerInterceptor;
        private CallAdapter.Factory callAdapterFactory;
        private BodyConverter.Factory bodyConverterFactory;
        private StringConverter.Factory stringConverterFactory;
        private FormFieldConverter.Factory formFieldConverterFactory;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager sslTrustManager;
        private HostnameVerifier sslHostnameVerifier;
        private Duration timeout;

        /**
         * 配置 URL 前缀
         *
         * @param baseUrl URL 前缀
         * @return builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        /**
         * 配置非网络拦截器
         *
         * @param interceptor 非网络拦截器
         * @return builder
         */
        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        /**
         * 配置网络拦截器
         *
         * @param interceptor 网络拦截器
         * @return builder
         */
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

        /**
         * 配置动态请求头处理器委托实例
         *
         * @param delegate 动态请求头处理器委托实例
         * @return builder
         */
        public Builder dynamicHeaderDelegate(DynamicHeaderDelegate delegate) {
            this.dynamicHeaderDelegate = delegate;
            return this;
        }

        /**
         * 配置拦截器处理器委托实例
         *
         * @param delegate 拦截器处理器委托实例
         * @return builder
         */
        public Builder interceptorDelegate(InterceptorDelegate delegate) {
            this.interceptorDelegate = delegate;
            return this;
        }

        /**
         * 配置方法注解处理器委托实例
         *
         * @param delegate 方法注解处理器委托实例
         * @return builder
         */
        public Builder methodAnnotationDelegate(MethodAnnotationDelegate delegate) {
            this.methodAnnotationDelegate = delegate;
            return this;
        }

        /**
         * 配置请求分发器提供者委托实例
         *
         * @param delegate 请求分发器提供者委托实例
         * @return builder
         */
        public Builder dispatcherProviderDelegate(DispatcherProviderDelegate delegate) {
            this.dispatcherProviderDelegate = delegate;
            return this;
        }

        /**
         * 配置请求分发器提供者
         *
         * @param provider 请求分发器提供者
         * @return builder
         */
        public Builder dispatcherProviderClass(Class<? extends DispatcherProvider> provider) {
            this.dispatcherProviderClass = provider;
            return this;
        }

        /**
         * 配置请求分发器提供者
         *
         * @param provider 请求分发器提供者
         * @return builder
         */
        public Builder dispatcherProvider(DispatcherProvider provider) {
            this.dispatcherProvider = provider;
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

        /**
         * 配置 CallAdapter.Factory 实例
         *
         * @param factory CallAdapter.Factory 实例
         * @return builder
         */
        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.callAdapterFactory = factory;
            return this;
        }

        /**
         * 配置 BodyConverter.Factory 实例
         *
         * @param factory BodyConverter.Factory 实例
         * @return builder
         */
        public Builder bodyConverterFactory(BodyConverter.Factory factory) {
            this.bodyConverterFactory = factory;
            return this;
        }

        /**
         * 配置 StringConverter.Factory 实例
         *
         * @param factory StringConverter.Factory 实例
         * @return builder
         */
        public Builder stringConverterFactory(StringConverter.Factory factory) {
            this.stringConverterFactory = factory;
            return this;
        }

        /**
         * 配置 FormFieldConverter.Factory 实例
         *
         * @param factory FormFieldConverter.Factory 实例
         * @return builder
         */
        public Builder formFieldConverterFactory(FormFieldConverter.Factory factory) {
            this.formFieldConverterFactory = factory;
            return this;
        }

        /**
         * 配置 SSLSocketFactory 实例
         *
         * @param factory  SSLSocketFactory 实例
         * @param manager  X509TrustManager 实例
         * @param verifier HostnameVerifier 实例
         * @return builder
         */
        public Builder https(SSLSocketFactory factory, X509TrustManager manager, HostnameVerifier verifier) {
            this.sslSocketFactory = factory;
            this.sslTrustManager = manager;
            this.sslHostnameVerifier = verifier;
            return this;
        }

        /**
         * 配置 OkHttpClient.Builder 实例
         *
         * @param client OkHttpClient.Builder 实例
         * @return builder
         */
        public Builder clientBuilder(OkHttpClient.Builder client) {
            this.clientBuilder = client;
            return this;
        }

        /**
         * 配置超时时间
         *
         * @param millis 超时时间
         * @return builder
         */
        public Builder timeout(long millis) {
            this.timeout = Duration.ofMillis(millis);
            return this;
        }

        /**
         * 配置超时时间
         *
         * @param timeout 超时时间
         * @return builder
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 配置日志拦截器
         *
         * @param enabled 是否启用日志拦截器
         * @return builder
         */
        public Builder logEnabled(boolean enabled) {
            this.logEnabled = enabled;
            return this;
        }

        /**
         * 配置日志拦截器
         *
         * @param interceptor 日志拦截器
         * @return builder
         */
        public Builder loggerInterceptor(Interceptor interceptor) {
            this.loggerInterceptor = interceptor;
            return this;
        }

        /**
         * 构建 Flare 实例
         *
         * @return Flare 实例
         */
        public Flare build() {
            Assert.notNull(baseUrl, "baseUrl cannot be null");

            callAdapterFactory = Opt.ofNullable(callAdapterFactory).orElse(new GuavaCallAdapter());
            bodyConverterFactory = Opt.ofNullable(bodyConverterFactory).orElse(new JacksonConverterFactory());
            stringConverterFactory = Opt.ofNullable(stringConverterFactory).orElse(new StringConverterFactory());
            formFieldConverterFactory = Opt.ofNullable(formFieldConverterFactory).orElse(new FormFieldConverterFactory());

            dynamicHeaderDelegate = Opt.ofNullable(dynamicHeaderDelegate).orElse(ConstructorDynamicHeaderDelegate.create());
            interceptorDelegate = Opt.ofNullable(interceptorDelegate).orElse(ConstructorInterceptorDelegate.create());
            methodAnnotationDelegate = Opt.ofNullable(methodAnnotationDelegate).orElse(ConstructorMethodAnnotationDelegate.create());
            dispatcherProviderDelegate = Opt.ofNullable(dispatcherProviderDelegate).orElse(ConstructorDispatcherProviderDelegate.create());

            // 默认的请求分发器提供者和请求分发器
            dispatcherProviderClass = null != dispatcherProviderClass ? dispatcherProviderClass : VirtualThreadDispatcherProvider.class;
            dispatcherProvider = Opt.ofNullable(dispatcherProvider).orElse(dispatcherProviderDelegate.apply(dispatcherProviderClass));
            dispatcher = Opt.ofNullable(dispatcher).orElse(dispatcherProvider.provide());

            // 开始创建 OkHttpClient.Builder
            clientBuilder = Opt.ofNullable(clientBuilder).orElse(new OkHttpClient.Builder());

            // 配置分发器
            clientBuilder.dispatcher(dispatcher);

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
    }
}
