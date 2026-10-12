package com.yhy.http.flare.model;

import com.yhy.http.flare.utils.Assert;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Flare 内部统一响应对象。
 * <p>
 * 包装 OkHttp 原始 {@link Response} 与转换后的业务响应体；请求失败时 body 为 {@code null}，
 * 错误响应体保存在 {@code errorBody} 中。
 *
 * @param <T>         转换后的响应体类型
 * <p>
 * Created on 2025-09-10 15:18
 *
 * @param rawResponse OkHttp 原始响应
 * @param body        转换后的响应体，失败响应为 {@code null}
 * @param errorBody   错误响应体，成功响应为 {@code null}
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record InternalResponse<T>(Response rawResponse, T body, ResponseBody errorBody) {

    /**
     * 判断 HTTP 状态是否成功（2xx）。
     *
     * @return 成功返回 {@code true}
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * 获取 HTTP 状态码。
     *
     * @return 状态码数值
     */
    public int getStatusCode() {
        return rawResponse.code();
    }

    /**
     * 获取 HTTP 响应消息（如 {@code OK}）。
     *
     * @return 状态行中的 reason phrase
     */
    public String getMessage() {
        return rawResponse.message();
    }

    /**
     * 构造成功的响应对象。
     *
     * @param rawResponse      OkHttp 原始响应
     * @param body             转换后的响应体
     * @param ignoreHttpStatus 是否忽略 HTTP 状态码检查（为 {@code false} 时要求响应必须是 2xx）
     * @param <T>              响应体类型
     * @return 成功的响应对象
     */
    public static <T> InternalResponse<T> success(Response rawResponse, T body, boolean ignoreHttpStatus) {
        Assert.notNull(rawResponse, "rawResponse cannot be null");
        Assert.isTrue(ignoreHttpStatus || rawResponse.isSuccessful(), "rawResponse is not successful");
        return new InternalResponse<>(rawResponse, body, null);
    }

    /**
     * 构造失败的响应对象。
     *
     * @param rawResponse OkHttp 原始响应，必须为非 2xx
     * @param errorBody   已缓冲的错误响应体，不能为 {@code null}
     * @param <T>         响应体类型
     * @return 失败的响应对象
     */
    public static <T> InternalResponse<T> error(Response rawResponse, ResponseBody errorBody) {
        Assert.notNull(errorBody, "ResponseBody cannot be null");
        Assert.notNull(rawResponse, "rawResponse cannot be null");
        Assert.isFalse(rawResponse.isSuccessful(), "rawResponse is not failure");
        return new InternalResponse<>(rawResponse, null, errorBody);
    }
}
