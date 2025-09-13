package com.yhyzgn.http.flare.http;

/**
 *
 * <p>
 * Created on 2025-09-11 06:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface HttpHandler<T> {

    T invoke(Object[] args) throws Exception;
}
