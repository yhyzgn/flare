package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * 请求体参数注解。
 * <p>
 * 标注在方法参数上，声明该参数整体序列化为 JSON 请求体（{@code application/json; charset=utf-8}），
 * 由 {@link com.yhy.http.flare.convert.BodyConverter} 转换；不能与表单编码同时使用。
 *
 * Created on 2025-09-13 15:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {
}
