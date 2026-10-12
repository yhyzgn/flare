package com.yhy.http.flare.spring.starter.register;

import tools.jackson.databind.json.JsonMapper;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.*;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.spring.provider.SpringDispatcherProvider;
import com.yhy.http.flare.such.delegate.ConstructorInstanceFactory;
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
 * Flare 的 Spring {@link FactoryBean} 实现。
 * <p>
 * 由 {@link AbstractFlareAutoRegister} 为每个 Flare 接口注册，负责组装
 * {@link Flare} 客户端并生成接口的动态代理 Bean。基础设施依赖
 * （转换器工厂、{@link InstanceFactory}、方法注解解析器、{@link DispatcherProvider}）
 * 通过 {@code getBeanProvider().getIfAvailable()} 按接口查找容器 Bean，缺失时返回 {@code null}
 * 并由后续默认值兜底，因此下游可声明同类型 Bean 覆盖默认实现。
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
     * Spring 应用上下文，用于按接口查找基础设施 Bean。
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
     * 静态请求头配置，键为头名称，值为头值列表。
     */
    @Setter
    protected Map<String, List<String>> headers;

    /**
     * 动态请求头处理器类型列表。
     */
    @Setter
    protected List<Class<? extends Header.Dynamic>> dynamicHeaderList;

    /**
     * 应用级拦截器类型列表。
     */
    @Setter
    protected List<Class<? extends Interceptor>> interceptors;

    /**
     * 网络级拦截器类型列表。
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
    private InstanceFactory instanceFactory;
    private MethodAnnotationDelegate methodAnnotationDelegate;

    private DispatcherProvider dispatcherProvider;

    /**
     * 获取由本 FactoryBean 产出的代理 Bean 实例。
     *
     * @return Flare 接口的动态代理实例
     */
    @Override
    public Object getObject() {
        return getTarget();
    }

    /**
     * 获取代理 Bean 的目标接口类型。
     *
     * @return Flare 接口类型
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
            .instanceFactory(instanceFactory)
            .methodAnnotationDelegate(methodAnnotationDelegate)
            .dispatcherProviderClass(SpringDispatcherProvider.class)
            .dispatcherProvider(dispatcherProvider)
            ;

        if (!CollectionUtils.isEmpty(dynamicHeaderList)) {
            dynamicHeaderList.forEach(item -> {
                try {
                    builder.header(instanceFactory.createTyped(item));
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        if (null != loggerInterceptor && loggerInterceptor != HttpLoggerInterceptor.class) {
            try {
                builder.loggerInterceptor(instanceFactory.createTyped(loggerInterceptor));
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if (!CollectionUtils.isEmpty(headers)) {
            headers.entrySet().stream().filter(e -> StringUtils.hasText(e.getKey()) && !CollectionUtils.isEmpty(e.getValue())).forEach(e -> e.getValue().forEach(item -> builder.header(e.getKey(), item)));
        }

        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(item -> builder.interceptor(instanceFactory.createTyped(item)));
        }
        if (!CollectionUtils.isEmpty(netInterceptors)) {
            netInterceptors.forEach(item -> builder.netInterceptor(instanceFactory.createTyped(item)));
        }
        if (timeout > 0) {
            builder.timeout(timeout);
        }
        if (sslSocketFactory != null && sslTrustManager != null && sslHostnameVerifier != null) {
            builder.https(instanceFactory.createTyped(sslSocketFactory), instanceFactory.createTyped(sslTrustManager), instanceFactory.createTyped(sslHostnameVerifier));
        }

        return (T) builder.build().create(flareInterface);
    }

    /**
     * 加载延迟初始化的基础设施 Bean。
     * <p>
     * 按 {@link JsonMapper}、转换器工厂、{@link InstanceFactory}、
     * {@link MethodAnnotationDelegate}、{@link DispatcherProvider} 的顺序，
     * 从容器按接口查找并缓存实例，供后续构建 {@link Flare} 使用。
     */
    private void initDelayedBeans() {
        // 初始化各个 Bean
        jsonMapper = Opt.ofNullable(jsonMapper).orElse(getInstance(JsonMapper.class));
        stringConverterFactory = Opt.ofNullable(stringConverterFactory).orElse(getInstance(StringConverter.Factory.class));
        bodyConverterFactory = Opt.ofNullable(bodyConverterFactory).orElse(getInstance(BodyConverter.Factory.class));
        instanceFactory = Opt.ofNullable(instanceFactory).orElse(getInstance(InstanceFactory.class));
        instanceFactory = Opt.ofNullable(instanceFactory).orElse(ConstructorInstanceFactory.create());
        methodAnnotationDelegate = Opt.ofNullable(methodAnnotationDelegate).orElse(getInstance(MethodAnnotationDelegate.class));

        dispatcherProvider = Opt.ofNullable(dispatcherProvider).orElse(getInstance(DispatcherProvider.class));
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 校验启用注解与基础地址配置是否完整。
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
     * <p>
     * 设置完成后回调 {@link #afterSetContext()} 供子类扩展。
     *
     * @param context Spring 应用上下文
     * @throws BeansException 上下文处理异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;

        afterSetContext();
    }

    /**
     * 按接口类型从容器查找 Bean 实例。
     * <p>
     * 使用 {@code getBeanProvider().getIfAvailable()}，容器中不存在该类型 Bean 时返回 {@code null}，
     * 因此不会抛出查找失败异常，也不会绕过下游自定义 Bean。
     *
     * @param clazz 目标接口或类型
     * @param <B> 目标类型
     * @return 容器中该类型的 Bean，不存在时为 {@code null}
     */
    protected <B> B getInstance(Class<B> clazz) {
        return this.context.getBeanProvider(clazz).getIfAvailable();
    }

    /**
     * 子类可重写的扩展点，在 {@link #setApplicationContext(ApplicationContext)} 之后执行。
     */
    protected void afterSetContext() {
    }

    /**
     * 子类可重写的扩展点，在 {@link #getTarget()} 之前执行。
     */
    protected void beforeCreateFlare() {
    }
}
