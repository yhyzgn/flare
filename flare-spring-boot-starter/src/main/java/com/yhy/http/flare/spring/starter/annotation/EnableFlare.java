package com.yhy.http.flare.spring.starter.annotation;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.spring.convert.JsonMapperConverterFactory;
import com.yhy.http.flare.spring.convert.SpringStringConverterFactory;
import com.yhy.http.flare.spring.delegate.*;
import com.yhy.http.flare.spring.provider.SpringDispatcherProvider;
import com.yhy.http.flare.spring.starter.config.FlareStarterAutoConfiguration;
import com.yhy.http.flare.spring.starter.register.FlareAutoRegister;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * 启用 Flare 组件。
 * <p>
 * 标注在配置类上后导入 {@link FlareAutoRegister}、{@link FlareStarterAutoConfiguration}
 * 及默认基础设施 Bean（转换器工厂、{@link SpringInstanceFactory}、
 * {@link SpringMethodAnnotationDelegate}、{@link SpringDispatcherProvider}），
 * 并在配置的扫描包下查找标注 {@link Flare} 的接口，为每个接口注册代理 Bean。
 * <p>
 * 本注解上的全局配置仅作兜底：接口级 {@code @Flare} 的显式配置优先于全局值；
 * 字符串配置支持 {@code ${...}} 占位符。
 * <p>
 * Created on 2025-09-18 11:11
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
    FlareAutoRegister.class,
    FlareStarterAutoConfiguration.class,
    JsonMapperConverterFactory.class,
    SpringStringConverterFactory.class,
    SpringInstanceFactory.class,
    SpringMethodAnnotationDelegate.class,
    SpringDispatcherProvider.class
})
public @interface EnableFlare {

    /**
     * 扫描基础包路径，等同于 {@link #basePackages()}。
     *
     * @return 扫描基础包路径
     */
    @AliasFor("basePackages")
    String[] value() default "";

    /**
     * 扫描基础包路径。
     *
     * @return 扫描基础包路径
     */
    @AliasFor("value")
    String[] basePackages() default "";

    /**
     * 用于推断扫描基础包的类型。
     *
     * @return 基础包标记类型
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 全局基础请求地址。
     *
     * @return 全局基础请求地址
     */
    String baseUrl() default "";

    /**
     * 全局请求头配置。
     *
     * @return 全局请求头配置
     */
    Header[] header() default {};

    /**
     * 全局拦截器配置。
     *
     * @return 全局拦截器配置
     */
    Interceptor[] interceptor() default {};

    /**
     * 全局超时时间，单位毫秒。
     *
     * @return 全局超时时间
     */
    String timeout() default "6000";

    /**
     * 是否启用请求日志。
     *
     * @return 是否启用请求日志
     */
    String logEnabled() default "true";

    /**
     * 请求日志拦截器类型。
     *
     * @return 请求日志拦截器类型
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
