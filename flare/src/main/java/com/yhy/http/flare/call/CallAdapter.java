package com.yhy.http.flare.call;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.utils.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 请求调用适配器。
 * <p>
 * 负责把 {@link Caller} 的同步调用结果适配为代理方法声明的返回类型，
 * 例如 {@code T}、{@link com.yhy.http.flare.model.InternalResponse} 或 Guava 的 {@code ListenableFuture}。
 *
 * @param <R> 响应体转换后的类型
 * @param <T> 适配后的返回类型
 * <p>
 * Created on 2025-09-10 15:14
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CallAdapter<R, T> {

    /**
     * 获取响应体转换时使用的返回类型。
     * <p>
     * 该类型是剥去外层包装（如 Future）之后、真正交给响应转换器解析的类型。
     *
     * @return 响应体转换目标类型
     */
    Type responseType();

    /**
     * 将 {@link Caller} 适配为代理方法的最终返回值。
     *
     * @param caller 请求调用器
     * @param args   代理方法的实参数组
     * @return 适配后的返回值
     * @throws Exception 适配或执行请求过程中发生的异常
     */
    T adapt(Caller<R> caller, Object[] args) throws Exception;

    /**
     * 适配器工厂接口。
     * <p>
     * 根据代理方法的返回类型与方法注解挑选合适的 {@link CallAdapter}。
     */
    interface Factory {

        /**
         * 获取适配器。
         *
         * @param returnType  代理方法返回类型
         * @param annotations 代理方法上的注解
         * @param flare       当前 Flare 实例
         * @return 与返回类型匹配的适配器实例
         */
        CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Flare flare);

        /**
         * 获取参数化类型的第一个实际类型参数上限。
         *
         * @param type 参数化类型
         * @return 第一个类型参数的上限类型
         */
        default Type getFirstParameterUpperBound(ParameterizedType type) {
            return ReflectUtils.getParameterUpperBound(0, type);
        }

        /**
         * 获取 {@link Type} 对应的原始类型。
         *
         * @param type 类型
         * @return 原始类型（{@code Class} 形式）
         */
        default Class<?> getRawType(Type type) {
            return ReflectUtils.getRawType(type);
        }
    }
}
