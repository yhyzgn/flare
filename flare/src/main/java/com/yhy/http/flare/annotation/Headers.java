package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 多请求头容器注解。
 * <p>
 * {@link Header} 的 {@code @Repeatable} 容器，用于在同方法上承载多个重复标注的请求头，
 * 无需直接使用。
 * <p>
 * Created on 2025-09-10 14:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Headers {

    /**
     * 请求头注解数组。
     *
     * @return 请求头注解数组
     */
    Header[] value() default {};
}
