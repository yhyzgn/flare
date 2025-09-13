package com.yhyzgn.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 拦截器注解
 * <p>
 * Created on 2025-09-13 15:04
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptors {

    /**
     * Interceptor 数组
     *
     * @return Interceptor 数组
     */
    Interceptor[] value() default {};
}