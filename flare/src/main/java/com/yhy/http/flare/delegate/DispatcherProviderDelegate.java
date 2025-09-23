package com.yhy.http.flare.delegate;

import com.yhy.http.flare.provider.DispatcherProvider;

/**
 * 请求分发器实例委托类接口
 * <p>
 * Created on 2025-09-23 10:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DispatcherProviderDelegate {

    /**
     * 获取请求分发器实例
     *
     * @param clazz 请求分发器类
     * @param <T>   请求分发器类型
     * @return 请求分发器实例
     */
    <T extends DispatcherProvider> T apply(Class<T> clazz);
}
