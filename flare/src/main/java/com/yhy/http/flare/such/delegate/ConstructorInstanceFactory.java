package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.delegate.InstanceFactory;
import com.yhy.http.flare.utils.ReflectUtils;

/**
 * 通过无参构造方法创建实例的默认工厂。
 *
 * @since 2.0.2
 */
public final class ConstructorInstanceFactory implements InstanceFactory {

    @Override
    public Object create(Class<?> type) {
        return ReflectUtils.newInstance(type);
    }

    /**
     * 创建默认实例工厂。
     *
     * @return 实例工厂
     */
    public static ConstructorInstanceFactory create() {
        return new ConstructorInstanceFactory();
    }
}
