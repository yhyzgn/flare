package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.utils.ReflectUtils;

/**
 * 通过构造方法创建请求分发器实例委托类接口实现
 * <p>
 * Created on 2025-09-23 10:49
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorDispatcherProviderDelegate implements DispatcherProviderDelegate {

    @Override
    public <T extends DispatcherProvider> T apply(Class<T> clazz) {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     */
    public static ConstructorDispatcherProviderDelegate create() {
        return new ConstructorDispatcherProviderDelegate();
    }
}
