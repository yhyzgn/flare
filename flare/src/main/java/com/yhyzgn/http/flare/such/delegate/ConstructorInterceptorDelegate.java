package com.yhyzgn.http.flare.such.delegate;

import com.yhyzgn.http.flare.delegate.InterceptorDelegate;
import com.yhyzgn.http.flare.utils.ReflectUtils;
import okhttp3.Interceptor;

/**
 * 通过构造方法创建拦截器类实例的代理接口实现
 * <p>
 * Created on 2025-09-10 18:09
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorInterceptorDelegate implements InterceptorDelegate {

    @Override
    public <T extends Interceptor> T apply(Class<T> clazz) throws Exception {
        return ReflectUtils.newInstance(clazz);
    }

    /**
     * 创建实例
     *
     * @return 实例
     */
    public static ConstructorInterceptorDelegate create() {
        return new ConstructorInterceptorDelegate();
    }
}
