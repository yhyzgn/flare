package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.delegate.ExceptionResolverDelegate;
import com.yhy.http.flare.utils.ReflectUtils;

/**
 * 通过构造方法创建异常处理器类实例的代理接口实现
 * <p>
 * Created on 2026-03-04 17:28
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @deprecated since 2.0.2; use {@link ConstructorInstanceFactory} instead. This API is planned for removal in 3.0.0.
 */
@Deprecated(since = "2.0.2")
public class ConstructorExceptionResolverDelegate implements ExceptionResolverDelegate {

    /**
     * 通过无参构造方法创建异常处理器实例。
     *
     * @param clazz 目标类型
     * @return 创建的实例
     * @throws Exception 构造方法调用失败时抛出
     */
    @Override
    public <T extends Catcher.Resolver> T apply(Class<T> clazz) throws Exception {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     * @deprecated since 2.0.2; use {@link ConstructorInstanceFactory#create()} instead. This API is planned for removal in 3.0.0.
     */
    @Deprecated(since = "2.0.2")
    public static ConstructorExceptionResolverDelegate create() {
        return new ConstructorExceptionResolverDelegate();
    }
}
