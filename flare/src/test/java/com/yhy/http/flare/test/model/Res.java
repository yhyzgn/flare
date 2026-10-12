package com.yhy.http.flare.test.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果测试模型，与 mock-server 约定的响应结构对应。
 * <p>
 * Created on 2025-09-16 13:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @param <T> 数据类型
 * @param code 响应码，0 表示成功
 * @param message 响应消息
 * @param data 响应数据
 */
public record Res<T>(Integer code, String message, T data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public boolean ok() {
        return code == 0;
    }
}
