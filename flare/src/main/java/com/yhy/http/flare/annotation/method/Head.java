package com.yhy.http.flare.annotation.method;

import java.lang.annotation.*;

/**
 * 标记方法为 HEAD 请求。
 * <p>
 * Created on 2025-09-13 15:09
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Head {

    /**
     * 请求子路径。
     * <p>
     * 相对基础 URL 解析；默认空字符串表示直接使用基础 URL。
     *
     * @return 请求子路径，默认空字符串
     */
    String value() default "";
}
