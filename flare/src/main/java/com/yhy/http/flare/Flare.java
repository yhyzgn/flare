package com.yhy.http.flare;

import tools.jackson.databind.json.JsonMapper;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.annotation.exception.Catchers;
import com.yhy.http.flare.annotation.exception.ErrorIgnored;
import com.yhy.http.flare.call.CallAdapter;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.*;
import com.yhy.http.flare.dispatcher.ThrowableDispatcher;
import com.yhy.http.flare.http.HttpHandler;
import com.yhy.http.flare.http.HttpHandlerAdapter;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.such.adapter.GuavaCallAdapter;
import com.yhy.http.flare.such.convert.FormFieldConverterFactory;
import com.yhy.http.flare.such.convert.JacksonConverterFactory;
import com.yhy.http.flare.such.convert.StringConverterFactory;
import com.yhy.http.flare.such.delegate.*;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.provider.VirtualThreadDispatcherProvider;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import com.yhy.http.flare.utils.Assert;
import com.yhy.http.flare.utils.Opt;
import lombok.extern.slf4j.Slf4j;
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
 * Flare HTTP 请求客户端。
 * <p>
 * 通过 {@link #create(Class)} 将使用 Flare 注解声明的 Java 接口转换为 OkHttp 动态代理，
 * 由代理完成请求构建、发送、响应转换与异常分发。
 * <p>
 * 典型用法：
 * <pre>{@code
 * Flare flare = new Flare.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .build();
 * Api api = flare.create(Api.class);
 * }</pre>
 * <p>
 * Created on 2025-09-10 14:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
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
    private final ExceptionResolverDelegate exceptionResolverDelegate;
    private final InstanceFactory instanceFactory;
    private final OkHttpClient.Builder clientBuilder;
    private final CallAdapter.Factory callAdapterFactory;
    private final BodyConverter.Factory bodyConverterFactory;
    private final StringConverter.Factory stringConverterFactory;
    private final FormFieldConverter.Factory formFieldConverterFactory;
    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager sslTrustManager;
    private final HostnameVerifier sslHostnameVerifier;
    private final boolean ignoreHttpStatus;

    private Flare(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;
        this.headers = builder.headers;
        this.dynamicHeaders = builder.dynamicHeaders;
        this.dynamicHeaderDelegate = builder.dynamicHeaderDelegate;
        this.interceptorDelegate = builder.interceptorDelegate;
        this.methodAnnotationDelegate = builder.methodAnnotationDelegate;
        this.exceptionResolverDelegate = builder.exceptionResolverDelegate;
        this.instanceFactory = builder.instanceFactory;
        this.clientBuilder = builder.clientBuilder;
        this.callAdapterFactory = builder.callAdapterFactory;
        this.bodyConverterFactory = builder.bodyConverterFactory;
        this.stringConverterFactory = builder.stringConverterFactory;
        this.formFieldConverterFactory = builder.formFieldConverterFactory;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.sslTrustManager = builder.sslTrustManager;
        this.sslHostnameVerifier = builder.sslHostnameVerifier;
        this.ignoreHttpStatus = builder.ignoreHttpStatus;
    }

    /**
     * 基础 URL。
     * <p>
     * 所有相对路径的接口请求都以该 URL 为前缀拼接。
     *
     * @return 基础 URL
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
     * @deprecated since 2.0.2; use {@link #instanceFactory()} instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
    public DynamicHeaderDelegate headerDelegate() {
        return dynamicHeaderDelegate;
    }

    /**
     * 拦截器处理器委托实例
     *
     * @return 拦截器处理器委托实例
     * @deprecated since 2.0.2; use {@link #instanceFactory()} instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
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
     * 实例创建工厂。
     *
     * @return 实例创建工厂
     * @since 2.0.2
     */
    public InstanceFactory instanceFactory() {
        return instanceFactory;
    }

    /**
     * SSL Socket 工厂。
     * <p>
     * 未显式配置时返回空 {@link Opt}，此时客户端使用默认的信任所有实现。
     *
     * @return SSL Socket 工厂，未配置时为空
     */
    public Opt<SSLSocketFactory> sslSocketFactory() {
        return Opt.ofNullable(sslSocketFactory);
    }

    /**
     * SSL 信任管理器。
     * <p>
     * 未显式配置时返回空 {@link Opt}，此时客户端使用默认的信任所有实现。
     *
     * @return X509 信任管理器，未配置时为空
     */
    public Opt<X509TrustManager> sslTrustManager() {
        return Opt.ofNullable(sslTrustManager);
    }

    /**
     * SSL 主机名校验器。
     * <p>
     * 未显式配置时返回空 {@link Opt}，此时客户端使用默认的信任所有实现。
     *
     * @return 主机名校验器，未配置时为空
     */
    public Opt<HostnameVerifier> sslHostnameVerifier() {
        return Opt.ofNullable(sslHostnameVerifier);
    }

    /**
     * 是否忽略 HTTP 状态码检查
     *
     * @return 是否忽略 HTTP 状态码检查
     */
    public boolean ignoreHttpStatus() {
        return ignoreHttpStatus;
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
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, this::proxyWithCatcher);
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
     * 加载方法对应的 HttpHandler
     *
     * @param method 方法
     * @return HttpHandler
     */
    private HttpHandler<?> loadHttpMethod(Method method) {
        return HttpHandlerAdapter.parseAnnotations(this, method);
    }

    /**
     * 代理方法，添加异常捕获功能
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return 方法返回值
     * @throws Throwable 异常
     */
    private Object proxyWithCatcher(Object proxy, Method method, Object[] args) throws Throwable {
        method.setAccessible(true);

        // 是否已经忽略异常
        if (method.isAnnotationPresent(ErrorIgnored.class)) {
            try {
                return proxyInvoke(proxy, method, args);
            } catch (Throwable e) {
                log.error("Error occurred but ignored when trying to invoke the method {}", method.getName(), e);
            }
            return null;
        }

        // 获取异常处理器注解
        List<Catcher> catchers = new ArrayList<>();
        Catchers catcher = method.getAnnotation(Catchers.class);
        if (null != catcher && catcher.value().length > 0) {
            catchers.addAll(Arrays.asList(catcher.value()));
        }
        if (method.isAnnotationPresent(Catcher.class)) {
            catchers.add(method.getAnnotation(Catcher.class));
        }
        // 执行方法
        try {
            return proxyInvoke(proxy, method, args);
        } catch (Throwable e) {
            if (ThrowableDispatcher.dispatch(method, catchers, e, instanceFactory)) {
                // 异常已被处理
                return null;
            }
            // 未被处理的异常，继续抛出
            throw e;
        }
    }

    /**
     * 执行代理方法
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return 方法返回值
     * @throws Throwable 异常
     */
    private Object proxyInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        return loadHttpMethod(method).invoke(null != args ? args : new Object[0]);
    }

    /**
     * Flare 客户端构建器。
     * <p>
     * 以链式调用的方式配置基础 URL、拦截器、分发器、转换器、SSL 组件等，
     * 最终通过 {@link #build()} 生成不可变的 {@link Flare} 实例。
     */
    public static class Builder {
        private final List<Interceptor> netInterceptors = new ArrayList<>();
        private final List<Interceptor> interceptors = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();
        private final List<Header.Dynamic> dynamicHeaders = new ArrayList<>();

        private HttpUrl baseUrl;
        private DynamicHeaderDelegate dynamicHeaderDelegate;
        private InterceptorDelegate interceptorDelegate;
        private MethodAnnotationDelegate methodAnnotationDelegate;
        private InstanceFactory instanceFactory;

        /**
         * exception Resolver Delegate。
         *
         * @deprecated since 2.0.2; use {@link #instanceFactory(InstanceFactory)} instead. This API is planned for removal in 3.0.0.
         */
        @Deprecated(since = "2.0.2")
        public ExceptionResolverDelegate exceptionResolverDelegate;
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
        private boolean ignoreHttpStatus;
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
         * @deprecated since 2.0.2; use {@link #instanceFactory(InstanceFactory)} instead. This API is planned for removal in 3.0.0.
         */
        @Deprecated(since = "2.0.2")
        public Builder dynamicHeaderDelegate(DynamicHeaderDelegate delegate) {
            this.dynamicHeaderDelegate = delegate;
            return this;
        }

        /**
         * 配置拦截器处理器委托实例
         *
         * @param delegate 拦截器处理器委托实例
         * @return builder
         * @deprecated since 2.0.2; use {@link #instanceFactory(InstanceFactory)} instead. This API is planned for removal in 3.0.0.
         */
        @Deprecated(since = "2.0.2")
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
         * 配置异常处理器委托实例
         *
         * @param delegate 异常处理器委托实例
         * @return builder
         * @deprecated since 2.0.2; use {@link #instanceFactory(InstanceFactory)} instead. This API is planned for removal in 3.0.0.
         */
        @Deprecated(since = "2.0.2")
        public Builder exceptionResolverDelegate(ExceptionResolverDelegate delegate) {
            this.exceptionResolverDelegate = delegate;
            return this;
        }

        /**
         * 配置请求分发器提供者委托实例
         *
         * @param delegate 请求分发器提供者委托实例
         * @return builder
         * @deprecated since 2.0.2; use {@link #instanceFactory(InstanceFactory)} instead. This API is planned for removal in 3.0.0.
         */
        @Deprecated(since = "2.0.2")
        public Builder dispatcherProviderDelegate(DispatcherProviderDelegate delegate) {
            this.dispatcherProviderDelegate = delegate;
            return this;
        }

        /**
         * 配置统一实例创建工厂。
         * <p>
         * 配置后，旧 Delegate 入口会桥接到该工厂；若同时配置旧 Delegate，统一工厂优先。
         *
         * @param factory 实例创建工厂
         * @return builder
         * @since 2.0.2
         */
        public Builder instanceFactory(InstanceFactory factory) {
            this.instanceFactory = factory;
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
         * 配置是否忽略 HTTP 状态码检查
         *
         * @param ignore 是否忽略 HTTP 状态码检查
         * @return builder
         */
        public Builder ignoreHttpStatus(boolean ignore) {
            this.ignoreHttpStatus = ignore;
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
            bodyConverterFactory = Opt.ofNullable(bodyConverterFactory).orElse(new JacksonConverterFactory(JsonMapper.builderWithJackson2Defaults().build()));
            stringConverterFactory = Opt.ofNullable(stringConverterFactory).orElse(new StringConverterFactory());
            formFieldConverterFactory = Opt.ofNullable(formFieldConverterFactory).orElse(new FormFieldConverterFactory());

            methodAnnotationDelegate = Opt.ofNullable(methodAnnotationDelegate).orElse(ConstructorMethodAnnotationDelegate.create());
            if (instanceFactory == null) {
                boolean hasLegacyDelegate = dynamicHeaderDelegate != null
                    || interceptorDelegate != null
                    || exceptionResolverDelegate != null
                    || dispatcherProviderDelegate != null;
                instanceFactory = hasLegacyDelegate
                    ? new LegacyDelegateInstanceFactory(dynamicHeaderDelegate, interceptorDelegate, exceptionResolverDelegate, dispatcherProviderDelegate)
                    : ConstructorInstanceFactory.create();
            }
            // 统一工厂是唯一真实的实例创建来源；旧 Delegate 仅保留为兼容入口。
            InstanceFactory configuredFactory = instanceFactory;
            dynamicHeaderDelegate = configuredFactory::createTyped;
            interceptorDelegate = configuredFactory::createTyped;
            exceptionResolverDelegate = configuredFactory::createTyped;
            dispatcherProviderDelegate = configuredFactory::createTyped;

            // 默认的请求分发器提供者和请求分发器
            dispatcherProviderClass = null != dispatcherProviderClass ? dispatcherProviderClass : VirtualThreadDispatcherProvider.class;
            dispatcherProvider = Opt.ofNullable(dispatcherProvider).orElse(instanceFactory.createTyped(dispatcherProviderClass));
            dispatcher = Opt.ofNullable(dispatcher).orElse(dispatcherProvider.provide());

            boolean customClientBuilder = clientBuilder != null;
            boolean customSslConfiguration = sslSocketFactory != null || sslTrustManager != null || sslHostnameVerifier != null;
            if (customSslConfiguration && (sslSocketFactory == null || sslTrustManager == null || sslHostnameVerifier == null)) {
                throw new IllegalArgumentException("sslSocketFactory, sslTrustManager and sslHostnameVerifier must be configured together");
            }
            // 开始创建 OkHttpClient.Builder
            clientBuilder = Opt.ofNullable(clientBuilder).orElse(new OkHttpClient.Builder());

            // 配置分发器
            clientBuilder.dispatcher(dispatcher);

            // 调用方显式提供 OkHttpClient.Builder 时，优先保留其 SSL 配置；否则使用 trust-all 默认值。
            if (!customSslConfiguration && customClientBuilder) {
                OkHttpClient configuredClient = clientBuilder.build();
                sslSocketFactory = configuredClient.sslSocketFactory();
                sslTrustManager = configuredClient.x509TrustManager();
                sslHostnameVerifier = configuredClient.hostnameVerifier();
            }
            // 默认忽略证书链和主机名校验；显式配置的 SSL 组件优先。
            sslSocketFactory = Opt.ofNullable(sslSocketFactory).orElse(new VoidSSLSocketFactory());
            sslTrustManager = Opt.ofNullable(sslTrustManager).orElse(new VoidSSLX509TrustManager());
            sslHostnameVerifier = Opt.ofNullable(sslHostnameVerifier).orElse(new VoidSSLHostnameVerifier());
            clientBuilder.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);

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
