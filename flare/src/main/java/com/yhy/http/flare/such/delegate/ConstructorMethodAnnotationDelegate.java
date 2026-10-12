package com.yhy.http.flare.such.delegate;

import com.yhy.http.flare.delegate.MethodAnnotationDelegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * {@link MethodAnnotationDelegate} 的默认实现。
 * <p>
 * 仅读取方法自身直接声明的注解（不含参数注解与合并注解语义），
 * 未命中时返回空列表。
 * <p>
 * Created on 2025-09-10 18:11
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorMethodAnnotationDelegate implements MethodAnnotationDelegate {

    /**
     * 读取方法上指定类型的注解。
     * <p>
     * 注意：此处调用的是 {@code Method#getAnnotation}，只返回方法自身直接声明的注解；
     * 若方法未标注，返回空列表。
     *
     * @param method          目标方法
     * @param annotationClass 注解类型
     * @return 命中的注解列表，最多一个元素
     */
    @Override
    public <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass) {
        return Stream.of(method.getAnnotation(annotationClass)).filter(Objects::nonNull).toList();
    }

    /**
     * 创建实例
     *
     * @return 实例
     */
    public static ConstructorMethodAnnotationDelegate create() {
        return new ConstructorMethodAnnotationDelegate();
    }
}
