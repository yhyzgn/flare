package com.yhy.http.flare.annotation.exception;

import java.lang.annotation.*;

/**
 * 方法级异常捕获注解。
 * <p>
 * 标注在被代理接口的方法上，声明当方法调用抛出指定类型的异常时，
 * 由指定的 {@link Resolver} 实例处理该异常。可重复标注（容器为 {@link Catchers}）。
 * <p>
 * 异常被处理后方法返回 {@code null}；未声明匹配的捕获器时异常继续向上抛出。
 * 与 {@link com.yhy.http.flare.dispatcher.ThrowableDispatcher} 配合生效。
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
     * 捕获的异常类型。
     * <p>
     * 只有抛出的异常是该类型（或其子类型）时才交给 {@link #resolver()} 处理。
     * 默认 {@code Throwable.class} 表示捕获所有异常。
     *
     * @return 捕获的异常类型，默认捕获所有异常
     */
    Class<? extends Throwable> throwable() default Throwable.class;

    /**
     * 异常处理器类。
     * <p>
     * 异常发生时通过 {@link com.yhy.http.flare.delegate.InstanceFactory} 实例化并调用。
     *
     * @return 异常处理器类
     */
    Class<? extends Resolver> resolver();

    /**
     * 异常处理器接口。
     * <p>
     * 实现类处理捕获到的异常，可在 {@code resolve} 中记录日志、降级或执行补偿逻辑。
     */
    interface Resolver {

        /**
         * 处理捕获的异常。
         *
         * @param throwable 捕获到的异常
         * @throws Throwable 处理过程自身抛出的异常
         */
        void resolve(Throwable throwable) throws Throwable;
    }
}
