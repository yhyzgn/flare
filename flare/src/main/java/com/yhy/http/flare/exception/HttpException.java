package com.yhy.http.flare.exception;

import com.yhy.http.flare.model.InternalResponse;
import com.yhy.http.flare.utils.Assert;
import lombok.Getter;

/**
 * 网络请求异常
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
