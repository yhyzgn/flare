package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * 表单字段参数注解。
 * <p>
 * 标注在方法参数上，声明该参数作为表单字段提交。仅在
 * {@link com.yhy.http.flare.annotation.FormData} 或
 * {@link com.yhy.http.flare.annotation.X3WFormUrlEncoded} 编码的方法下有效。
 *
 * Created on 2025-09-13 15:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Field {

    /**
     * 表单字段名称。
 * <p>
     * 为空字符串时回退到方法签名中的参数名。仅在
     * {@link com.yhy.http.flare.annotation.FormData} 或
     * {@link com.yhy.http.flare.annotation.X3WFormUrlEncoded} 表单方法下有效。
     *
     * @return 表单字段名称，为空字符串时回退到参数名
     */
    String value() default "";

    /**
     * 参数值是否已经过 URL 编码。
     * <p>
     * 为 {@code true} 时跳过编码直接使用原始值。
     *
     * @return 参数值是否已编码，默认 {@code false}
     */
    boolean encoded() default false;

    /**
     * 参数值为 {@code null} 时使用的默认值。
     * <p>
     * 空字符串视为未设置。
     *
     * @return 默认值，空字符串视为未设置
     */
    String defaultValue() default "";
}
