package com.yhy.http.flare.exception;

import com.yhy.http.flare.model.InternalResponse;
import com.yhy.http.flare.utils.Assert;
import lombok.Getter;

/**
 * HTTP 请求失败异常。
 * <p>
 * 当响应状态码非 2xx、未启用 {@code ignoreHttpStatus} 且方法返回类型不是
 * {@link InternalResponse} 时抛出；携带状态码与原始响应便于调用方取回错误响应体。
 * <p>
 * Created on 2025-09-11 09:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class HttpException extends RuntimeException {
    private final int code;
    private final InternalResponse<?> response;

    /**
     * 创建 HttpException 实例。
     *
     * @param response 失败的原始响应，不能为 {@code null}
     */
    public HttpException(InternalResponse<?> response) {
        super(parseMessage(response));
        this.code = response.getStatusCode();
        this.response = response;
    }

    private static String parseMessage(InternalResponse<?> response) {
        Assert.notNull(response, "response cannot be null");
        return "HTTP " + response.getStatusCode() + " " + response.getMessage();
    }
}
