package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 多拦截器容器注解。
 * <p>
 * {@link Interceptor} 的 {@code @Repeatable} 容器，用于在同方法上承载多个重复标注的拦截器，
 * 无需直接使用。
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
     * 拦截器注解数组。
     *
     * @return 拦截器注解数组
     */
    Interceptor[] value() default {};
}
