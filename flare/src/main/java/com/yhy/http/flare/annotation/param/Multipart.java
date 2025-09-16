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

    String value() default "";

    String filename() default "";
}
