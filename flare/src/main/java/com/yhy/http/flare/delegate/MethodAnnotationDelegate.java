package com.yhy.http.flare.delegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法注解提取代理接口
 * <p>
 * Created on 2025-09-10 16:27
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MethodAnnotationDelegate {

    /**
     * 获取方法上的指定类型注解。
     * <p>
     * 返回的列表可能包含合并语义（如 Spring 的元注解合并）下的多个实例，无匹配时返回空列表。
     *
     * @param method          目标方法
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 方法上的注解实例列表，无匹配时为空列表
     */
    <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass);
}
