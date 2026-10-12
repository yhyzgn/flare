package com.yhy.http.flare.mock.server.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * mock 服务的统一响应结果模型。
 * <p>
 * 所有端点返回的响应信封：{@code code} 为 0 表示成功、-1 表示默认失败，
 * 业务数据承载在 {@code data} 中；字段为只读，通过静态工厂创建。
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
     * 创建不含数据的成功响应。
     *
     * @param <T> 数据类型
     * @return code 为 0、message 为 "OK"、data 为 {@code null} 的成功响应
     */
    public static <T> Res<T> success() {
        return new Res<>(0, "OK", null);
    }

    /**
     * 创建携带数据的成功响应。
     *
     * @param <T> 数据类型
     * @param data 响应数据
     * @return code 为 0、message 为 "OK" 的成功响应
     */
    public static <T> Res<T> success(T data) {
        return new Res<>(0, "OK", data);
    }

    /**
     * 创建默认失败响应。
     *
     * @param <T> 数据类型
     * @param message 失败消息
     * @return code 为 -1 的失败响应
     */
    public static <T> Res<T> fail(String message) {
        return new Res<>(-1, message, null);
    }

    /**
     * 创建指定失败码的失败响应。
     *
     * @param <T> 数据类型
     * @param code 失败码
     * @param message 失败消息
     * @return 指定 code 的失败响应
     */
    public static <T> Res<T> fail(int code, String message) {
        return new Res<>(code, message, null);
    }
}
