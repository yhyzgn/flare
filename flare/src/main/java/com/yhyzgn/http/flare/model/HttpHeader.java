package com.yhyzgn.http.flare.model;

/**
 * 用于传递 HTTP 头信息的类
 * <p>
 * Created on 2025-09-10 14:52
 *
 * @param name  名称
 * @param value 值
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record HttpHeader(String name, String value) {

    /**
     * 静态构造器
     *
     * @param name  名称
     * @param value 值
     * @return HttpHeader 对象
     */
    public static HttpHeader of(String name, String value) {
        return new HttpHeader(name, value);
    }
}
