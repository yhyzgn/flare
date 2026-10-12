package com.yhy.http.flare.spring.starter.annotation;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import com.yhy.http.flare.such.ssl.VoidSSLHostnameVerifier;
import com.yhy.http.flare.such.ssl.VoidSSLSocketFactory;
import com.yhy.http.flare.such.ssl.VoidSSLX509TrustManager;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.annotation.*;

/**
 * Flare 远程接口声明注解。
 * <p>
 * 标注在接口上，由 {@link EnableFlare} 触发的扫描注册器识别；
 * 每个标注本注解的接口都会注册为一个 {@code FlareFactoryBean} 代理 Bean，
 * Bean 别名按 {@link #qualifier()}、{@link #name()}、接口类名依次回退。
 * <p>
 * 配置优先级"越靠近代理方法越优先"：本注解的显式配置覆盖 {@link EnableFlare} 全局值，
 * 方法级与参数级请求头再覆盖本层配置；{@link #timeout()} 与 {@link #logEnabled()}
 * 为空字符串时表示继承全局配置。本注解只能标注在接口上。
 * <p>
 * Created on 2025-09-18 11:17
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface Flare {

    /**
     * Bean 名称快捷配置。
     *
     * @return Bean 名称
     */
    @AliasFor("name")
    String value() default "";

    /**
     * Bean 名称。
     *
     * @return Bean 名称
     */
    @AliasFor("value")
    String name() default "";

    /**
     * Bean 限定符。
     *
     * @return Bean 限定符
     */
    String qualifier() default "";

    /**
     * 基础请求地址。
     *
     * @return 基础请求地址
     */
    String baseUrl() default "";

    /**
     * 请求头配置。
     *
     * @return 请求头配置
     */
    Header[] header() default {};

    /**
     * 拦截器配置。
     *
     * @return 拦截器配置
     */
    Interceptor[] interceptor() default {};

    /**
     * 超时时间，单位毫秒；空字符串表示继承 {@link EnableFlare} 的全局配置。
     *
     * @return 超时时间
     */
    String timeout() default "";

    /**
     * 是否启用请求日志；空字符串表示继承 {@link EnableFlare} 的全局配置。
     *
     * @return 是否启用请求日志
     */
    String logEnabled() default "";

    /**
     * 是否注册为主 Bean。
     *
     * @return 是否注册为主 Bean
     */
    boolean primary() default true;

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

    /**
     * 是否忽略 HTTP 状态码错误。
     *
     * @return 是否忽略 HTTP 状态码错误
     */
    boolean ignoreHttpStatus() default false;
}
