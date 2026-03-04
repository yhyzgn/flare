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
 */
public class ConstructorExceptionResolverDelegate implements ExceptionResolverDelegate {

    @Override
    public <T extends Catcher.Resolver> T apply(Class<T> clazz) throws Exception {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     */
    public static ConstructorExceptionResolverDelegate create() {
        return new ConstructorExceptionResolverDelegate();
    }
}
