package com.yhyzgn.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 请求头键值对
 * <p>
 * Created on 2025-09-10 14:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HeaderPair {

    /**
     * 请求头的名称
     *
     * @return 请求头的名称
     */
    String name();

    /**
     * 请求头的值
     *
     * @return 请求头的值
     */
    String value();
}
