package com.yhyzgn.http.lyra.delegate;

import okhttp3.Interceptor;

/**
 * 拦截器代理接口
 * <p>
 * Created on 2025-09-10 16:25
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
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
