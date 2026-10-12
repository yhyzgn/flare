package com.yhy.http.flare.call;

import com.yhy.http.flare.model.InternalResponse;
import okhttp3.Request;

import java.io.IOException;

/**
 * 请求调用器接口。
 * <p>
 * 代表一次尚未执行或正在执行的接口方法调用，由 OkHttp Call 封装而来，
 * 提供同步执行、异步排队、取消与克隆能力。
 *
 * @param <T> 响应体类型
 * <p>
 * Created on 2025-09-10 15:00
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Caller<T> extends Cloneable {

    /**
     * 同步执行请求。
     *
     * @return 已解析的响应
     * @throws IOException 网络请求或响应体读取失败
     */
    InternalResponse<T> execute() throws IOException;

    /**
     * 获取本次调用将发送的 {@link Request}，必要时先创建底层调用。
     *
     * @return 待发送的请求
     */
    Request request();

    /**
     * 将请求加入异步队列执行。
     *
     * @param callback 请求完成或失败时回调的回调对象
     */
    void enqueue(Callback<T> callback);

    /**
     * 当前调用是否已经执行（同步或异步均算）。
     *
     * @return 已执行返回 {@code true}
     */
    boolean isExecuted();

    /**
     * 取消请求。
     * <p>
     * 已发出但未完成的请求会被中断；尚未创建的调用会在执行时被取消。
     */
    void cancel();

    /**
     * 当前调用是否已被取消。
     *
     * @return 已取消返回 {@code true}
     */
    boolean isCanceled();

    /**
     * 克隆出一个尚未执行的新调用器。
     *
     * @return 未执行状态的新调用器副本
     */
    Caller<T> clone();
}
