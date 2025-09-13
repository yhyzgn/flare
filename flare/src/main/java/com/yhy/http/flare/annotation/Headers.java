package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 用于标注多个请求头的注解
 * <p>
 * Created on 2025-09-10 14:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Headers {

    /**
     * 请求头数组
     *
     * @return 请求头数组
     */
    Header[] value() default {};
}
