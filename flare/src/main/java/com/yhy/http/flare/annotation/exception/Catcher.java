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
@Repeatable(Catchers.class)
public @interface Catcher {

    /**
     * 捕获的异常类型
     * <p>
     * 默认捕获所有异常
     *
     * @return 异常类型
     */
    Class<? extends Throwable> throwable() default Throwable.class;

    /**
     * 异常处理器类
     * <p>
     * 默认使用 {@link Resolver} 接口
     *
     * @return 异常处理器类
     */
    Class<? extends Resolver> resolver() default Resolver.class;

    /**
     * 异常处理器接口
     * <p>
     * 用于处理捕获的异常
     */
    interface Resolver {

        /**
         * 处理捕获的异常
         *
         * @param throwable 捕获的异常
         * @throws Throwable 异常
         */
        void resolve(Throwable throwable) throws Throwable;
    }
}
