package com.yhy.http.flare.delegate;

import com.yhy.http.flare.annotation.Header;

/**
 * 动态请求头代理接口
 * <p>
 * Created on 2025-09-10 16:20
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface DynamicHeaderDelegate {

    /**
     * 从下层逻辑获取动态创建请求头的类实例
     *
     * @param clazz 动态创建请求头的类
     * @param <T>   动态创建请求头的类类型
     * @return 动态创建请求头的类实例
     * @throws Exception IO异常
     */
    <T extends Header.Dynamic> T apply(Class<T> clazz) throws Exception;
}
