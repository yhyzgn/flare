package com.yhy.http.flare.spring.boot.sample.model;

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
 */
@Getter
public class Res<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final String message;
    private final T data;

    private Res(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Res<T> success() {
        return new Res<>(0, "OK", null);
    }

    public static <T> Res<T> success(T data) {
        return new Res<>(0, "OK", data);
    }

    public static <T> Res<T> fail(String message) {
        return new Res<>(-1, message, null);
    }

    public static <T> Res<T> fail(int code, String message) {
        return new Res<>(code, message, null);
    }
}
