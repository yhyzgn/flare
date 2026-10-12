package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import com.yhy.http.flare.utils.ReflectUtils;

/**
 * 通过构造方法创建动态请求头类实例的代理接口实现
 * <p>
 * Created on 2025-09-10 16:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @deprecated since 2.0.2; use {@link com.yhy.http.flare.such.delegate.ConstructorInstanceFactory} instead. This API is planned for removal in 3.0.0.
 */
@Deprecated(since = "2.0.2")
public class ConstructorDynamicHeaderDelegate implements DynamicHeaderDelegate {

    /**
     * 通过无参构造方法创建动态请求头实例。
     *
     * @param clazz 目标类型
     * @return 创建的实例
     * @throws Exception 构造方法调用失败时抛出
     */
    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) throws Exception {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     * @deprecated since 2.0.2; use {@link ConstructorInstanceFactory#create()} instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
    public static ConstructorDynamicHeaderDelegate create() {
        return new ConstructorDynamicHeaderDelegate();
    }
}
