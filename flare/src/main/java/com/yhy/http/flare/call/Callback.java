package com.yhy.http.flare.call;

import com.yhy.http.flare.model.InternalResponse;

/**
 * 异步请求回调接口。
 * <p>
 * 与 {@link Caller#enqueue(Callback)} 配合使用，在请求完成或失败时由调用线程回调。
 *
 * @param <T> 响应体类型
 * <p>
 * Created on 2025-09-10 15:16
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Callback<T> {

    /**
     * 请求成功（含 HTTP 非 2xx 被记录为失败响应的场景）后的回调。
     *
     * @param caller   发起本次请求的调用器
     * @param response 已转换的响应对象
     */
    void onResponse(Caller<T> caller, InternalResponse<T> response);

    /**
     * 请求失败（网络错误或响应转换异常）后的回调。
     *
     * @param caller 发起本次请求的调用器
     * @param t      失败原因
     */
    void onFailure(Caller<T> caller, Throwable t);
}
