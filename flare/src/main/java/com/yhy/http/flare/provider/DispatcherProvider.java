package com.yhy.http.flare.provider;

import okhttp3.Dispatcher;

/**
 * Dispatcher 提供者接口
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
     * 提供 Dispatcher 对象
     *
     * @return Dispatcher 对象
     */
    Dispatcher provide();
}
