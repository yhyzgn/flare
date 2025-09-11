package com.yhyzgn.http.flare.model;

import com.yhyzgn.http.flare.utils.Assert;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 内置的响应体
 * <p>
 * Created on 2025-09-10 15:18
 *
 * @param <T>         响应体类型
 * @param rawResponse 原始响应体
 * @param body        响应体
 * @param errorBody   错误响应体
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record InternalResponse<T>(Response rawResponse, T body, ResponseBody errorBody) {

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getStatusCode() {
        return rawResponse.code();
    }

    /**
     * 获取响应信息
     *
     * @return 响应信息
     */
    public String getMessage() {
        return rawResponse.message();
    }

    /**
     * 构造成功的响应体
     *
     * @param rawResponse 原始响应体
     * @param body        响应体
     * @param <T>         响应体类型
     * @return 成功的响应体
     */
    public static <T> InternalResponse<T> success(Response rawResponse, T body) {
        Assert.notNull(rawResponse, "rawResponse cannot be null");
        Assert.isTrue(rawResponse.isSuccessful(), "rawResponse is not successful");
        return new InternalResponse<>(rawResponse, body, null);
    }

    /**
     * 构造失败的响应体
     *
     * @param rawResponse 原始响应体
     * @param errorBody   错误响应体
     * @param <T>         响应体类型
     * @return 失败的响应体
     */
    public static <T> InternalResponse<T> error(Response rawResponse, ResponseBody errorBody) {
        Assert.notNull(errorBody, "ResponseBody cannot be null");
        Assert.notNull(rawResponse, "rawResponse cannot be null");
        Assert.isFalse(rawResponse.isSuccessful(), "rawResponse is not failure");
        return new InternalResponse<>(rawResponse, null, errorBody);
    }
}
