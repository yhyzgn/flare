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
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Multipart {

    /**
     * 请求参数名，空则取签名中的参数名
     *
     * @return 请求参数名
     */
    String value() default "";

    /**
     * 文件名
     *
     * @return 文件名
     */
    String filename() default "";
}
