package com.yhy.http.flare.delegate;

import com.yhy.http.flare.annotation.exception.Catcher;

/**
 * 异常处理器代理接口
 * <p>
 * Created on 2026-03-04 17:27
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface ExceptionResolverDelegate {

    /**
     * 从下层逻辑获取动态创建异常处理器的类实例
     *
     * @param clazz 异常处理器类
     * @param <T>   异常处理器类型
     * @return 异常处理器实例
     * @throws Exception 异常
     */
    <T extends Catcher.Resolver> T apply(Class<T> clazz) throws Exception;
}
