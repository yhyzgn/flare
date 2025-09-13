package com.yhyzgn.http.flare.annotation.method;

import java.lang.annotation.*;

/**
 * options 请求
 * <p>
 * Created on 2025-09-13 15:09
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Options {

    /**
     * api子路径
     *
     * @return api子路径
     */
    String value() default "";
}
