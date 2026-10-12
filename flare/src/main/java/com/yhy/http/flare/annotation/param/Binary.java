package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * 二进制请求体参数注解。
 * <p>
 * 标注在方法参数上，声明该参数作为原始二进制流（{@code application/octet-stream}）发送，
 * 不能与 {@link com.yhy.http.flare.annotation.FormData} / {@link com.yhy.http.flare.annotation.X3WFormUrlEncoded}
 * 表单编码同时使用。
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
public @interface Binary {
}
