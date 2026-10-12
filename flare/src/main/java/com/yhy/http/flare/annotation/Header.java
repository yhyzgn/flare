package com.yhy.http.flare.annotation;

import com.yhy.http.flare.model.HttpHeader;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * 请求头注解。
 * <p>
 * 标注在接口方法或方法参数上，为请求添加请求头。方法上可重复标注（容器为 {@link Headers}）；
 * 标注在参数上时，将参数值作为请求头的值。
 * <p>
 * 支持三种形式：
 * <ul>
 *     <li>方法级静态头：{@code @Header("token: 123456")} 或 {@code value}/{@code pairValue} 键值形式</li>
 *     <li>方法级动态头：通过 {@link #dynamic()} 指定 {@link Dynamic} 实现类，调用时按方法反射构造请求头</li>
 *     <li>参数级头：标注在参数上，配合 {@code value} 指定请求头名称</li>
 * </ul>
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
     * 请求头名称。
     * <p>
     * 方法级使用时也可写成 {@code "name: value"} 冒号分隔形式，一次声明完整键值对；
     * 默认空字符串表示使用 {@link #pairName()} / {@link #pairValue()} 键值对形式。
     *
     * @return 请求头名称，或 "name: value" 形式的完整键值对
     */
    String value() default "";

    /**
     * 请求头名称，与 {@link #pairValue()} 配合使用的键值对形式。
     *
     * @return 请求头名称
     */
    String pairName() default "";

    /**
     * 请求头值，与 {@link #pairName()} 配合使用的键值对形式。
     *
     * @return 请求头值
     */
    String pairValue() default "";

    /**
     * 动态构造请求头的实现类。
     * <p>
     * 默认 {@code Dynamic.class} 表示不启用动态头；指定具体实现类后，
     * 请求发起时通过 {@link Dynamic#header(Method)} 构造请求头。
     *
     * @return 动态请求头实现类，默认不启用
     */
    Class<? extends Dynamic> dynamic() default Dynamic.class;

    /**
     * 动态构造请求头接口。
     * <p>
     * 实现类会在每次请求发起时被实例化并调用，用于根据当前方法信息构造请求头。
     */
    interface Dynamic {

        /**
         * 动态构造请求头。
         *
         * @param method 被代理接口方法的反射对象
         * @return 构造出的请求头
         */
        HttpHeader header(Method method);
    }
}
