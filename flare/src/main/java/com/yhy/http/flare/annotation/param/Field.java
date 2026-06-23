package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 *
 * <p>
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
     * 参数名称。
     *
     * @return 参数名称
     */
    String value() default "";

    /**
     * 是否已编码。
     *
     * @return 是否已编码
     */
    boolean encoded() default false;

    /**
     * 默认值。
     *
     * @return 默认值
     */
    String defaultValue() default "";
}
