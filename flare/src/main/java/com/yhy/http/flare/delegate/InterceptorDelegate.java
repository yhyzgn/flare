package com.yhy.http.flare.delegate;

import okhttp3.Interceptor;

/**
 * 拦截器代理接口
 * <p>
 * Created on 2025-09-10 16:25
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @deprecated since 2.0.2; use {@link InstanceFactory} instead. This API is planned for removal in 3.0.0.
 */
@Deprecated(since = "2.0.2")
@FunctionalInterface
public interface InterceptorDelegate {

    /**
     * 从下层逻辑获取动态创建拦截器的类实例
     *
     * @param clazz 拦截器类
     * @param <T>   拦截器类型
     * @return 拦截器实例
     * @throws Exception 异常
     */
    <T extends Interceptor> T apply(Class<T> clazz) throws Exception;
}
