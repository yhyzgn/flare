package com.yhy.http.flare.model;

import com.yhy.http.flare.utils.StringUtils;

/**
 * HTTP 头键值对。
 * <p>
 * 主要用于 {@code Header.Dynamic} 动态构造请求头时的传递载体。
 *
 * @param name  请求头名称
 * @param value 请求头值
 * <p>
 * Created on 2025-09-10 14:52
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record HttpHeader(String name, String value) {

    /**
     * 判断该请求头是否有效。
     * <p>
     * 名称与值均非空白时才有效。
     *
     * @return 有效返回 {@code true}，名称或值为空白返回 {@code false}
     */
    public boolean isValid() {
        return StringUtils.hasText(name) && StringUtils.hasText(value);
    }

    /**
     * 静态构造器。
     *
     * @param name  请求头名称
     * @param value 请求头值
     * @return 请求头对象
     */
    public static HttpHeader of(String name, String value) {
        return new HttpHeader(name, value);
    }
}
