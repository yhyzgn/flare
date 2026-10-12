package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 方法级拦截器注解。
 * <p>
 * 标注在被代理接口的方法上，为该方法请求声明额外的 OkHttp 拦截器。
 * 可重复标注（容器为 {@link Interceptors}）。
 * <p>
 * Created on 2025-09-13 15:03
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Interceptors.class)
public @interface Interceptor {

    /**
     * 拦截器实现类。
     * <p>
     * 请求发起时通过 {@link com.yhy.http.flare.delegate.InstanceFactory} 实例化。
     *
     * @return 拦截器实现类
     */
    Class<? extends okhttp3.Interceptor> value();

    /**
     * 是否网络请求拦截器。
     * <p>
     * {@code true} 注册为 OkHttp 网络拦截器（network interceptor），
     * {@code false} 注册为应用拦截器（application interceptor）。
     *
     * @return 是否网络请求拦截器，默认 {@code true}
     */
    boolean net() default true;
}
