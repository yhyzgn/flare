package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * 动态相对 URL 参数注解。
 * <p>
 * 标注在方法参数上，声明该参数作为本次请求的 URL（覆盖方法注解的静态子路径）；
 * 参数类型必须为 {@code okhttp3.HttpUrl}、{@code String} 或 {@code java.net.URI}。
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
public @interface Url {
}
