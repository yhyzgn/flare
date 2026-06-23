package com.yhy.http.flare.mock.server.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应结果
 * <p>
 * Created on 2025-09-16 13:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> 数据类型
 */
@Getter
public class Res<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码。
     */
    private final int code;

    /**
     * 响应消息。
     */
    private final String message;

    /**
     * 响应数据。
     */
    private final T data;

    private Res(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应。
     *
     * @param <T> 数据类型
     * @return 处理结果
     */
    public static <T> Res<T> success() {
        return new Res<>(0, "OK", null);
    }

    /**
     * 成功响应。
     *
     * @param <T> 数据类型
     * @param data 值
     * @return 处理结果
     */
    public static <T> Res<T> success(T data) {
        return new Res<>(0, "OK", data);
    }

    /**
     * 失败响应。
     *
     * @param <T> 数据类型
     * @param message 字符串
     * @return 处理结果
     */
    public static <T> Res<T> fail(String message) {
        return new Res<>(-1, message, null);
    }

    /**
     * 失败响应。
     *
     * @param <T> 数据类型
     * @param code 值
     * @param message 字符串
     * @return 处理结果
     */
    public static <T> Res<T> fail(int code, String message) {
        return new Res<>(code, message, null);
    }
}
