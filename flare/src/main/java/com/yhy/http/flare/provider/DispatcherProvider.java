package com.yhy.http.flare.provider;

import okhttp3.Dispatcher;

/**
 * OkHttp {@link Dispatcher} 提供者接口。
 * <p>
 * 通过实现该接口自定义异步请求的执行策略，例如虚拟线程执行器（见 {@code VirtualThreadDispatcherProvider}）。
 * <p>
 * Created on 2025-09-23 10:43
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface DispatcherProvider {

    /**
     * 提供 Dispatcher 对象。
     *
     * @return 用于异步调度的 OkHttp Dispatcher 实例
     */
    Dispatcher provide();
}
