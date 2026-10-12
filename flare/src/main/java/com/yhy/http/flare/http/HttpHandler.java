package com.yhy.http.flare.http;

/**
 * 已解析的接口方法调用处理器。
 * <p>
 * 每个带 HTTP 注解的代理方法对应一个 {@link HttpHandler}，负责把方法实参
 * 转换为请求并返回适配后的结果。
 *
 * @param <T> 代理方法返回类型
 * <p>
 * Created on 2025-09-11 06:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpHandler<T> {

    /**
     * 以给定实参执行对应的 HTTP 请求。
     *
     * @param args 代理方法实参数组
     * @return 适配后的方法返回值
     * @throws Exception 请求执行或响应转换失败
     */
    T invoke(Object[] args) throws Exception;
}
