package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 请求头键值对注解。
 * <p>
 * 只能作为其他注解（{@code ANNOTATION_TYPE}）的元注解使用，用于组合注解中声明
 * 固定的请求头键值对，例如自定义的认证头注解。
 * <p>
 * Created on 2025-09-10 14:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HeaderPair {

    /**
     * 请求头的名称。
     *
     * @return 请求头的名称
     */
    String name();

    /**
     * 请求头的值。
     *
     * @return 请求头的值
     */
    String value();
}
