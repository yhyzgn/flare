package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 方法级基础 URL 注解。
 * <p>
 * 标注在被代理接口的方法上，声明该方法的所有请求都以指定的 URL 为基础地址，
 * 覆盖 {@link com.yhy.http.flare.Flare.Builder#baseUrl(String)} 配置的全局基础 URL。
 * <p>
 * Created on 2025-09-13 15:15
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BaseUrl {

    /**
     * 当前方法使用的基础 URL。
     *
     * @return 基础 URL
     */
    String value();
}
