package com.yhy.http.flare.spring.starter.register;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.spring.convert.ObjectMapperConverterFactory;
import com.yhy.http.flare.spring.convert.SpringStringConverterFactory;
import com.yhy.http.flare.spring.delegate.SpringDynamicHeaderDelegate;
import com.yhy.http.flare.spring.delegate.SpringInterceptorDelegate;
import com.yhy.http.flare.spring.delegate.SpringMethodAnnotationDelegate;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
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
    private ApplicationContext context;

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
    private Map<String, List<String>> headers;
    @Setter
    private List<Class<? extends Header.Dynamic>> dynamicHeaderList;
    @Setter
    private List<Class<? extends Interceptor>> interceptors;
    @Setter
    private List<Class<? extends Interceptor>> netInterceptors;
    @Setter
    private Class<? extends SSLSocketFactory> sslSocketFactory;
    @Setter
    private Class<? extends X509TrustManager> sslTrustManager;
    @Setter
    private Class<? extends HostnameVerifier> sslHostnameVerifier;
    @Setter
    private Class<? extends Interceptor> loggerInterceptor;

    private SpringStringConverterFactory stringConverterFactory;
    private ObjectMapperConverterFactory bodyConverterFactory;
    private SpringDynamicHeaderDelegate dynamicHeaderDelegate;
    private SpringInterceptorDelegate interceptorDelegate;
    private SpringMethodAnnotationDelegate methodAnnotationDelegate;

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
        Flare.Builder builder = new Flare.Builder()
                .baseUrl(baseUrl)
                .logEnabled(logEnabled)
                .timeout(timeout)
                .stringConverterFactory(stringConverterFactory)
                .bodyConverterFactory(bodyConverterFactory)
                .dynamicHeaderDelegate(dynamicHeaderDelegate)
                .interceptorDelegate(interceptorDelegate)
                .methodAnnotationDelegate(methodAnnotationDelegate);

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

        // 初始化各个 Bean
        this.stringConverterFactory = getInstance(SpringStringConverterFactory.class);
        this.bodyConverterFactory = getInstance(ObjectMapperConverterFactory.class);
        this.dynamicHeaderDelegate = getInstance(SpringDynamicHeaderDelegate.class);
        this.interceptorDelegate = getInstance(SpringInterceptorDelegate.class);
        this.methodAnnotationDelegate = getInstance(SpringMethodAnnotationDelegate.class);
    }

    private <B> B getInstance(Class<B> clazz) {
        try {
            return this.context.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("", e);
            return null;
        }
    }
}
