package com.yhyzgn.http.flare.such.delegate;

import com.yhyzgn.http.flare.delegate.MethodAnnotationDelegate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 通过构造方法创建类实例的代理接口实现
 * <p>
 * Created on 2025-09-10 18:11
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConstructorMethodAnnotationDelegate implements MethodAnnotationDelegate {

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
