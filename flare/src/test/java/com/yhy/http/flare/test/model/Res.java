package com.yhy.http.flare.test.model;

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
public record Res<T>(Integer code, String message, T data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public boolean ok() {
        return code == 0;
    }
}
