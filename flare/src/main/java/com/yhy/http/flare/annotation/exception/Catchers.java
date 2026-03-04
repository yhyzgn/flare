package com.yhy.http.flare.annotation.exception;

import java.lang.annotation.*;

/**
 * 异常捕获注解
 * <p>
 * Created on 2026-03-04 16:40
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Catchers {

    /**
     * 异常捕获器数组
     *
     * @return 异常捕获器数组
     */
    Catcher[] value();
}

