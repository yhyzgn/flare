package com.yhyzgn.http.lyra.exception;

import com.yhyzgn.http.lyra.model.InternalResponse;
import com.yhyzgn.http.lyra.utils.Assert;
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
