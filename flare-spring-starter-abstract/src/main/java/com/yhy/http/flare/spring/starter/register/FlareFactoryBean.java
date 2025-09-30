package com.yhy.http.flare.spring.starter.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.spring.convert.ObjectMapperConverterFactory;
import com.yhy.http.flare.spring.convert.SpringStringConverterFactory;
import com.yhy.http.flare.spring.delegate.SpringDispatcherProviderDelegate;
import com.yhy.http.flare.spring.delegate.SpringDynamicHeaderDelegate;
import com.yhy.http.flare.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.flare.spring.delegate.SpringMethodAnnotationDelegate;
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
    @Setter
    protected Map<String, List<String>> headers;
    @Setter
    protected List<Class<? extends Header.Dynamic>> dynamicHeaderList;
    @Setter
    protected List<Class<? extends Interceptor>> interceptors;
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

    private ObjectMapper objectMapper;
    private SpringStringConverterFactory stringConverterFactory;
    private ObjectMapperConverterFactory bodyConverterFactory;
    private SpringDynamicHeaderDelegate dynamicHeaderDelegate;
    private SpringInterceptorDelegate interceptorDelegate;
    private SpringMethodAnnotationDelegate methodAnnotationDelegate;
    private DispatcherProviderDelegate dispatcherProviderDelegate;

    private DispatcherProvider dispatcherProvider;

    @Override
    public Object getObject() {
        return getTarget();
    }

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
                .stringConverterFactory(stringConverterFactory)
                .bodyConverterFactory(bodyConverterFactory)
                .dynamicHeaderDelegate(dynamicHeaderDelegate)
                .interceptorDelegate(interceptorDelegate)
                .methodAnnotationDelegate(methodAnnotationDelegate)
                .dispatcherProviderDelegate(dispatcherProviderDelegate)
                .dispatcherProviderClass(SpringDispatcherProvider.class)
                .dispatcherProvider(dispatcherProvider);

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
     * 加载延迟初始化的 Bean
     */
    private void initDelayedBeans() {
        // 初始化各个 Bean
        objectMapper = Opt.ofNullable(objectMapper).orElse(getInstance(ObjectMapper.class));
        stringConverterFactory = getInstance(SpringStringConverterFactory.class);
        bodyConverterFactory = getInstance(ObjectMapperConverterFactory.class);
        dynamicHeaderDelegate = getInstance(SpringDynamicHeaderDelegate.class);
        interceptorDelegate = getInstance(SpringInterceptorDelegate.class);
        methodAnnotationDelegate = getInstance(SpringMethodAnnotationDelegate.class);
        dispatcherProviderDelegate = getInstance(SpringDispatcherProviderDelegate.class);

        dispatcherProvider = getInstance(SpringDispatcherProvider.class);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(flareAnnotation, "The returned value of AbstractFlareAutoRegister#enableAnnotation() can not be null");
        String annotationClassName = flareAnnotation.getSimpleName();
        Assert.hasText(baseUrl, "@" + annotationClassName + " [baseURL] can not be empty or null.");
        log.info("@{} properties for [{}] set complete.", annotationClassName, flareInterface);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;

        afterSetContext();
    }

    protected <B> B getInstance(Class<B> clazz) {
        try {
            return this.context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 子类可重写此方法，在 {@link #setApplicationContext(ApplicationContext)} 之后执行
     */
    protected void afterSetContext() {
    }

    /**
     * 子类可重写此方法，在 {@link #getTarget()} 之前执行
     */
    protected void beforeCreateFlare() {
    }
}
