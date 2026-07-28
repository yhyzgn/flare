package com.yhy.http.flare.spring.starter.register;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Spring 自动注册配置优先级测试。
 *
 * @author Neo
 * @version 1.0.0
 * @since 2.0.2
 */
public class AbstractFlareAutoRegisterPriorityTest {

    /**
     * 接口级配置应覆盖启用注解中的全局配置，全局配置只做兜底。
     */
    @Test
    public void localFlareConfigurationOverridesEnableDefaults() {
        BeanDefinition definition = beanDefinition(PriorityApi.class);

        assertEquals("http://flare.example", property(definition, "baseUrl"));
        assertEquals(6000L, property(definition, "timeout"));
        assertEquals(Boolean.TRUE, property(definition, "logEnabled"));
        assertEquals(
            Map.of("X-Scope", List.of("flare"), "X-Enable-Only", List.of("enable-only")),
            property(definition, "headers")
        );
        assertEquals(List.of(FlarePriorityInterceptor.class, EnablePriorityInterceptor.class), property(definition, "netInterceptors"));
    }

    /**
     * 接口级未配置字段时，应使用启用注解中的全局兜底值。
     */
    @Test
    public void enableConfigurationProvidesFallbackWhenLocalOmitsValue() {
        BeanDefinition definition = beanDefinition(FallbackApi.class);

        assertEquals("http://enable.example", property(definition, "baseUrl"));
        assertEquals(12345L, property(definition, "timeout"));
        assertEquals(Boolean.FALSE, property(definition, "logEnabled"));
        assertEquals(
            Map.of("X-Scope", List.of("enable"), "X-Enable-Only", List.of("enable-only")),
            property(definition, "headers")
        );
        assertEquals(List.of(EnablePriorityInterceptor.class), property(definition, "netInterceptors"));
    }

    private BeanDefinition beanDefinition(Class<?> apiType) {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        PriorityRegister registrar = new PriorityRegister();
        registrar.setEnvironment(new StandardEnvironment());
        registrar.setResourceLoader(new DefaultResourceLoader());
        registrar.registerBeanDefinitions(AnnotationMetadata.introspect(PriorityConfig.class), registry);
        return registry.getBeanDefinition(apiType.getName());
    }

    private Object property(BeanDefinition definition, String name) {
        PropertyValue propertyValue = definition.getPropertyValues().getPropertyValue(name);
        return propertyValue.getValue();
    }
}

/**
 * 测试注册器。
 */
class PriorityRegister extends AbstractFlareAutoRegister {

    /**
     * 获取启用注解类型。
     *
     * @return 启用注解类型
     */
    @Override
    public Class<? extends Annotation> enableAnnotation() {
        return PriorityEnable.class;
    }

    /**
     * 获取接口注解类型。
     *
     * @return 接口注解类型
     */
    @Override
    public Class<? extends Annotation> flareAnnotation() {
        return PriorityFlare.class;
    }
}

/**
 * 全局配置注解。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface PriorityEnable {

    /**
     * 扫描基础包路径快捷配置。
     *
     * @return 扫描基础包路径
     */
    String[] value() default "";

    /**
     * 扫描基础包路径。
     *
     * @return 扫描基础包路径
     */
    String[] basePackages() default "";

    /**
     * 用于推断扫描基础包的类型。
     *
     * @return 基础包标记类型
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 全局基础地址。
     *
     * @return 全局基础地址
     */
    String baseUrl() default "";

    /**
     * 全局请求头。
     *
     * @return 全局请求头
     */
    Header[] header() default {};

    /**
     * 全局拦截器。
     *
     * @return 全局拦截器
     */
    Interceptor[] interceptor() default {};

    /**
     * 全局超时。
     *
     * @return 全局超时
     */
    String timeout() default "6000";

    /**
     * 全局日志开关。
     *
     * @return 全局日志开关
     */
    String logEnabled() default "true";

    /**
     * 日志拦截器类型。
     *
     * @return 日志拦截器类型
     */
    Class<? extends okhttp3.Interceptor> loggerInterceptor() default HttpLoggerInterceptor.class;

    /**
     * SSL Socket 工厂类型。
     *
     * @return SSL Socket 工厂类型
     */
    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    /**
     * SSL 信任管理器类型。
     *
     * @return SSL 信任管理器类型
     */
    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    /**
     * SSL 主机名校验器类型。
     *
     * @return SSL 主机名校验器类型
     */
    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;
}

/**
 * 接口级配置注解。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface PriorityFlare {

    /**
     * Bean 名称快捷配置。
     *
     * @return Bean 名称
     */
    String value() default "";

    /**
     * Bean 名称。
     *
     * @return Bean 名称
     */
    String name() default "";

    /**
     * Bean 限定符。
     *
     * @return Bean 限定符
     */
    String qualifier() default "";

    /**
     * 基础地址。
     *
     * @return 基础地址
     */
    String baseUrl() default "";

    /**
     * 请求头。
     *
     * @return 请求头
     */
    Header[] header() default {};

    /**
     * 拦截器。
     *
     * @return 拦截器
     */
    Interceptor[] interceptor() default {};

    /**
     * 超时。
     *
     * @return 超时
     */
    String timeout() default "";

    /**
     * 日志开关；空字符串表示继承全局配置。
     *
     * @return 日志开关
     */
    String logEnabled() default "";

    /**
     * 是否注册为主 Bean。
     *
     * @return 是否注册为主 Bean
     */
    boolean primary() default true;

    /**
     * 日志拦截器类型。
     *
     * @return 日志拦截器类型
     */
    Class<? extends okhttp3.Interceptor> loggerInterceptor() default HttpLoggerInterceptor.class;

    /**
     * SSL Socket 工厂类型。
     *
     * @return SSL Socket 工厂类型
     */
    Class<? extends SSLSocketFactory> sslSocketFactory() default VoidSSLSocketFactory.class;

    /**
     * SSL 信任管理器类型。
     *
     * @return SSL 信任管理器类型
     */
    Class<? extends X509TrustManager> sslTrustManager() default VoidSSLX509TrustManager.class;

    /**
     * SSL 主机名校验器类型。
     *
     * @return SSL 主机名校验器类型
     */
    Class<? extends HostnameVerifier> sslHostnameVerifier() default VoidSSLHostnameVerifier.class;

    /**
     * 是否忽略 HTTP 状态码错误。
     *
     * @return 是否忽略 HTTP 状态码错误
     */
    boolean ignoreHttpStatus() default false;
}

/**
 * 优先级测试配置。
 */
@PriorityEnable(
    basePackages = "com.yhy.http.flare.spring.starter.register",
    baseUrl = "http://enable.example",
    timeout = "12345",
    logEnabled = "false",
    header = {
        @Header(pairName = "X-Scope", pairValue = "enable"),
        @Header(pairName = "X-Enable-Only", pairValue = "enable-only")
    },
    interceptor = {
        @Interceptor(EnablePriorityInterceptor.class)
    }
)
class PriorityConfig {
}

/**
 * 显式局部配置接口。
 */
@PriorityFlare(
    baseUrl = "http://flare.example",
    timeout = "6000",
    logEnabled = "true",
    header = {
        @Header(pairName = "X-Scope", pairValue = "flare")
    },
    interceptor = {
        @Interceptor(FlarePriorityInterceptor.class)
    }
)
interface PriorityApi {

    /**
     * 首页。
     *
     * @return 响应
     */
    @Get("/index")
    String index();
}

/**
 * 兜底配置接口。
 */
@PriorityFlare
interface FallbackApi {

    /**
     * 首页。
     *
     * @return 响应
     */
    @Get("/index")
    String index();
}

/**
 * 全局拦截器。
 */
class EnablePriorityInterceptor implements okhttp3.Interceptor {

    /**
     * 拦截请求。
     *
     * @param chain 拦截器链
     * @return 响应
     * @throws IOException IO 异常
     */
    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}

/**
 * 局部拦截器。
 */
class FlarePriorityInterceptor implements okhttp3.Interceptor {

    /**
     * 拦截请求。
     *
     * @param chain 拦截器链
     * @return 响应
     * @throws IOException IO 异常
     */
    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}
