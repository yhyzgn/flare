package com.yhy.http.flare.annotation.exception;

import java.lang.annotation.*;

/**
 * 忽略方法异常注解。
 * <p>
 * 标注在被代理接口的方法上，声明该方法调用抛出的任何异常都直接忽略并返回 {@code null}，
 * 异常仅记录错误日志，不向上传播。优先级低于 {@link Catcher} 声明的定向捕获。
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
public @interface ErrorIgnored {
}
