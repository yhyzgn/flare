package com.yhy.http.flare.annotation;

import com.yhy.http.flare.model.HttpHeader;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * 用于标注请求头的注解
 * <p>
 * Created on 2025-09-10 14:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(Headers.class)
public @interface Header {

    /**
     * 请求头名称
     *
     * @return 请求头名称
     */
    String value() default "";

    /**
     * 请求头名称
     *
     * @return 请求头名称
     */
    String pairName() default "";

    /**
     * 请求头值
     *
     * @return 请求头值
     */
    String pairValue() default "";

    /**
     * 动态构造请求头接口
     *
     * @return 请求头
     */
    Class<? extends Dynamic> dynamic() default Dynamic.class;

    /**
     * 动态构造请求头接口
     */
    interface Dynamic {

        /**
         * 动态构造请求头
         *
         * @param method 动态代理类中的接口方法反射对象
         * @return 请求头
         */
        HttpHeader header(Method method);
    }
}
