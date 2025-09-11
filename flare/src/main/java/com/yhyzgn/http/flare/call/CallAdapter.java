package com.yhyzgn.http.flare.call;

import com.yhyzgn.http.flare.Flare;
import com.yhyzgn.http.flare.utils.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 请求发送适配器
 * <p>
 * Created on 2025-09-10 15:14
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CallAdapter<R, T> {

    /**
     * 获取返回类型
     *
     * @return 返回类型
     */
    Type returnType();

    /**
     * 适配请求
     *
     * @param caller 请求
     * @param args   参数
     * @return 适配结果
     * @throws Exception 异常
     */
    T adapt(Caller<R> caller, Object[] args) throws Exception;

    /**
     * 适配器工厂
     */
    interface Factory {

        /**
         * 获取适配器
         *
         * @param returnType  返回类型
         * @param annotations 注解
         * @param flare        Flare实例
         * @return 适配器
         */
        CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Flare flare);

        /**
         * 获取第一个参数的上限类型
         *
         * @param type 类型
         * @return 第一个参数的上限类型
         */
        default Type getFirstParameterUpperBound(ParameterizedType type) {
            return ReflectUtils.getParameterUpperBound(0, type);
        }

        /**
         * 获取原始类型
         *
         * @param type 类型
         * @return 原始类型
         */
        default Class<?> getRawType(Type type) {
            return ReflectUtils.getRawType(type);
        }
    }
}
