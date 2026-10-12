package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.delegate.InstanceFactory;
import com.yhy.http.flare.utils.ReflectUtils;

/**
 * 通过无参构造方法创建实例的默认 {@link InstanceFactory} 实现。
 * <p>
 * 目标类型必须提供可访问的无参构造方法，否则创建失败。
 * <p>
 * Created on 2026-10-12 14:25
 *
 * @author 颜洪毅
 * @version 2.0.2
 * @since 2.0.2
 */
public final class ConstructorInstanceFactory implements InstanceFactory {

    /**
     * 通过反射调用目标类型的无参构造方法创建实例。
     *
     * @param type 目标类型
     * @return 创建的实例
     * @throws ReflectiveOperationException 目标类型缺少可访问的无参构造方法等创建失败场景
     */
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
