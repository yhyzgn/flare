package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 拦截器注解
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
     * 拦截器类
     *
     * @return 拦截器类
     */
    Class<? extends okhttp3.Interceptor> value();

    /**
     * 是否网络请求拦截器
     *
     * @return 是否网络请求拦截器
     */
    boolean net() default true;
}
