package com.yhy.http.flare.spring.boot.sample.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应结果模型。
 * <p>
 * 示例应用与 mock-server 约定的统一响应结构，包含响应码、消息和数据。
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

    @JsonCreator
    private Res(
        @JsonProperty("code") int code,
        @JsonProperty("message") String message,
        @JsonProperty("data") T data
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应。
     *
     * @param <T> 数据类型
     * @return 响应码为 0、消息为 "OK"、数据为空的响应
     */
    public static <T> Res<T> success() {
        return new Res<>(0, "OK", null);
    }

    /**
     * 成功响应。
     *
     * @param <T> 数据类型
     * @param data 响应数据
     * @return 响应码为 0、消息为 "OK"、携带数据的响应
     */
    public static <T> Res<T> success(T data) {
        return new Res<>(0, "OK", data);
    }

    /**
     * 失败响应。
     *
     * @param <T> 数据类型
     * @param message 失败消息
     * @return 响应码为 -1、无数据的失败响应
     */
    public static <T> Res<T> fail(String message) {
        return new Res<>(-1, message, null);
    }

    /**
     * 失败响应。
     *
     * @param <T> 数据类型
     * @param code 自定义失败响应码
     * @param message 失败消息
     * @return 携带指定响应码、无数据的失败响应
     */
    public static <T> Res<T> fail(int code, String message) {
        return new Res<>(code, message, null);
    }
}
