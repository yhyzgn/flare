package com.yhyzgn.http.flare.call;

import com.yhyzgn.http.flare.model.InternalResponse;

/**
 * 回调接口
 * <p>
 * Created on 2025-09-10 15:16
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Callback<T> {

    /**
     * 成功回调
     *
     * @param caller   caller
     * @param response 响应
     */
    void onResponse(Caller<T> caller, InternalResponse<T> response);

    /**
     * 失败回调
     *
     * @param caller caller
     * @param t      异常
     */
    void onFailure(Caller<T> caller, Throwable t);
}
