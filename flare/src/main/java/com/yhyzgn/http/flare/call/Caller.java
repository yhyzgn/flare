package com.yhyzgn.http.flare.call;

import com.yhyzgn.http.flare.model.InternalResponse;
import okhttp3.Request;

import java.io.IOException;

/**
 * 请求调用者接口
 * <p>
 * Created on 2025-09-10 15:00
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Caller<T> extends Cloneable {

    /**
     * 执行请求
     *
     * @return 响应
     * @throws IOException IO异常
     */
    InternalResponse<T> execute() throws IOException;

    /**
     * 构造请求
     *
     * @return 请求
     */
    Request request();

    /**
     * 异步队列执行请求
     *
     * @param callback 回调
     */
    void enqueue(Callback<T> callback);

    /**
     * 是否已经执行
     *
     * @return 是否已经执行
     */
    boolean isExecuted();

    /**
     * 取消请求
     */
    void cancel();

    /**
     * 是否已经取消
     *
     * @return 是否已经取消
     */
    boolean isCanceled();

    /**
     * 克隆一个新的调用者
     *
     * @return 新的调用者
     */
    Caller<T> clone();
}
