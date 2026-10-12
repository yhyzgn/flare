package com.yhy.http.flare.annotation.exception;

import java.lang.annotation.*;

/**
 * 多异常捕获器容器注解。
 * <p>
 * {@link Catcher} 的 {@code @Repeatable} 容器，用于在同方法上承载多个重复标注的异常捕获器，
 * 无需直接使用。
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
     * 异常捕获器注解数组。
     *
     * @return 异常捕获器注解数组
     */
    Catcher[] value();
}

