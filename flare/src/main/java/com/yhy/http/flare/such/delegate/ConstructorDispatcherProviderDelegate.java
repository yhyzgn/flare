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
 * @deprecated since 2.0.2; use {@link ConstructorInstanceFactory} instead. This API is planned for removal in 3.0.0.
 */
@Deprecated(since = "2.0.2")
public class ConstructorDispatcherProviderDelegate implements DispatcherProviderDelegate {

    /**
     * 通过无参构造方法创建请求分发器提供者实例。
     *
     * @param clazz 目标类型
     * @return 创建的实例
     */
    @Override
    public <T extends DispatcherProvider> T apply(Class<T> clazz) {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     * @deprecated since 2.0.2; use {@link ConstructorInstanceFactory#create()} instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
    public static ConstructorDispatcherProviderDelegate create() {
        return new ConstructorDispatcherProviderDelegate();
    }
}
