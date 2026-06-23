package com.yhy.http.flare.spring.starter.register;

import tools.jackson.databind.json.JsonMapper;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.*;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.spring.convert.JsonMapperConverterFactory;
import com.yhy.http.flare.spring.convert.SpringStringConverterFactory;
import com.yhy.http.flare.spring.delegate.*;
import com.yhy.http.flare.spring.provider.SpringDispatcherProvider;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Flare 的 Spring FactoryBean 实现
 * <p>
 * Created on 2025-09-17 17:36
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class FlareFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {
    /**
     * context。
     *
     */
    protected ApplicationContext context;

    @Setter
    private Class<? extends Annotation> flareAnnotation;
    @Setter
    private Class<?> flareInterface;
    @Setter
    private String baseUrl;
    @Setter
    private Boolean logEnabled;
    @Setter
    private long timeout;

    /**
     * headers。
     *
     */
    @Setter
    protected Map<String, List<String>> headers;

    /**
     * dynamic Header List。
     *
     */
    @Setter
    protected List<Class<? extends Header.Dynamic>> dynamicHeaderList;

    /**
     * interceptors。
     *
     */
    @Setter
    protected List<Class<? extends Interceptor>> interceptors;

    /**
     * net Interceptors。
     *
     */
    @Setter
    protected List<Class<? extends Interceptor>> netInterceptors;
    @Setter
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    @Setter
    private Class<? extends X509TrustManager> sslTrustManager;
    @Setter
    private Class<? extends HostnameVerifier> sslHostnameVerifier;
    @Setter
    private Class<? extends Interceptor> loggerInterceptor;
    @Setter
    private boolean ignoreHttpStatus;

    private JsonMapper jsonMapper;
    private StringConverter.Factory stringConverterFactory;
    private BodyConverter.Factory bodyConverterFactory;
    private DynamicHeaderDelegate dynamicHeaderDelegate;
    private InterceptorDelegate interceptorDelegate;
    private MethodAnnotationDelegate methodAnnotationDelegate;
    private DispatcherProviderDelegate dispatcherProviderDelegate;
    private ExceptionResolverDelegate exceptionResolverDelegate;

    private DispatcherProvider dispatcherProvider;

    /**
     * 获取对象。
     *
     * @return 处理结果
     */
    @Override
    public Object getObject() {
        return getTarget();
    }

    /**
     * 获取对象类型。
     *
     * @return 处理结果
     */
    @Override
    public Class<?> getObjectType() {
        return flareInterface;
    }

    @SuppressWarnings("unchecked")
    <T> T getTarget() {
        beforeCreateFlare();

        // 这些 Bean 可能需要延迟初始化，因此在这里初始化
        initDelayedBeans();

        Flare.Builder builder = new Flare.Builder()
            .baseUrl(baseUrl)
            .logEnabled(logEnabled)
            .timeout(timeout)
            .ignoreHttpStatus(ignoreHttpStatus)
            .stringConverterFactory(stringConverterFactory)
            .bodyConverterFactory(bodyConverterFactory)
            .dynamicHeaderDelegate(dynamicHeaderDelegate)
            .interceptorDelegate(interceptorDelegate)
            .methodAnnotationDelegate(methodAnnotationDelegate)
            .dispatcherProviderDelegate(dispatcherProviderDelegate)
            .dispatcherProviderClass(SpringDispatcherProvider.class)
            .dispatcherProvider(dispatcherProvider)
            .exceptionResolverDelegate(exceptionResolverDelegate);

        if (!CollectionUtils.isEmpty(dynamicHeaderList)) {
            dynamicHeaderList.forEach(item -> {
                try {
                    builder.header(dynamicHeaderDelegate.apply(item));
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        if (null != loggerInterceptor && loggerInterceptor != HttpLoggerInterceptor.class) {
            try {
                builder.loggerInterceptor(interceptorDelegate.apply(loggerInterceptor));
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if (!CollectionUtils.isEmpty(headers)) {
            headers.entrySet().stream().filter(e -> StringUtils.hasText(e.getKey()) && !CollectionUtils.isEmpty(e.getValue())).forEach(e -> e.getValue().forEach(item -> builder.header(e.getKey(), item)));
        }

        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(item -> builder.interceptor(getInstance(item)));
        }
        if (!CollectionUtils.isEmpty(netInterceptors)) {
            netInterceptors.forEach(item -> builder.netInterceptor(getInstance(item)));
        }
        if (timeout > 0) {
            builder.timeout(timeout);
        }
        if (sslSocketFactory != null && sslTrustManager != null && sslHostnameVerifier != null) {
            builder.https(getInstance(sslSocketFactory), getInstance(sslTrustManager), getInstance(sslHostnameVerifier));
        }

        return (T) builder.build().create(flareInterface);
    }

    /**

     * * 加载延迟初始化的 Bean

     */
    private void initDelayedBeans() {
        // 初始化各个 Bean
        jsonMapper = Opt.ofNullable(jsonMapper).orElse(getInstance(JsonMapper.class));
        stringConverterFactory = Opt.ofNullable(stringConverterFactory).orElse(getInstance(SpringStringConverterFactory.class));
        bodyConverterFactory = Opt.ofNullable(bodyConverterFactory).orElse(getInstance(JsonMapperConverterFactory.class));
        dynamicHeaderDelegate = Opt.ofNullable(dynamicHeaderDelegate).orElse(getInstance(SpringDynamicHeaderDelegate.class));
        interceptorDelegate = Opt.ofNullable(interceptorDelegate).orElse(getInstance(SpringInterceptorDelegate.class));
        methodAnnotationDelegate = Opt.ofNullable(methodAnnotationDelegate).orElse(getInstance(SpringMethodAnnotationDelegate.class));
        dispatcherProviderDelegate = Opt.ofNullable(dispatcherProviderDelegate).orElse(getInstance(SpringDispatcherProviderDelegate.class));
        exceptionResolverDelegate = Opt.ofNullable(exceptionResolverDelegate).orElse(getInstance(SpringExceptionResolverDelegate.class));

        dispatcherProvider = Opt.ofNullable(dispatcherProvider).orElse(getInstance(SpringDispatcherProvider.class));
    }

    /**
     * 属性设置完成回调。
     *
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(flareAnnotation, "The returned value of AbstractFlareAutoRegister#enableAnnotation() can not be null");
        String annotationClassName = flareAnnotation.getSimpleName();
        Assert.hasText(baseUrl, "@" + annotationClassName + " [baseURL] can not be empty or null.");
        log.info("@{} properties for [{}] set complete.", annotationClassName, flareInterface);
    }

    /**
     * 设置应用上下文。
     *
     * @param context 值
     * @throws Exception 调用异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;

        afterSetContext();
    }

    /**
     * get Instance。
     *
     * @param clazz 类型
     * @return 处理结果
     */
    protected <B> B getInstance(Class<B> clazz) {
        try {
            return this.context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("", e);
            return null;
        }
    }

    /**

     * * 子类可重写此方法，在 {@link #setApplicationContext(ApplicationContext)} 之后执行

     */
    protected void afterSetContext() {
    }

    /**

     * * 子类可重写此方法，在 {@link #getTarget()} 之前执行

     */
    protected void beforeCreateFlare() {
    }
}
