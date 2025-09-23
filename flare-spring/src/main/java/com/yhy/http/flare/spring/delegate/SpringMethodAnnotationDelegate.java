package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.MethodAnnotationDelegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Spring 实现的方法注解提取器注入代理
 * <p>
 * Created on 2025-09-17 17:29
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class SpringMethodAnnotationDelegate implements MethodAnnotationDelegate, InitializingBean {

    @Override
    public <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass) {
        MergedAnnotations annotations = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
        // 使用 stream() 获取所有 Header 注解
        List<T> result = annotations.stream(annotationClass)
                .map(MergedAnnotation::synthesize) // 合成每个注解实例
                .toList();
        return (CollectionUtils.isEmpty(result) ? Collections.singletonList(method.getAnnotation(annotationClass)) : result).stream().filter(Objects::nonNull).toList();
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringMethodAnnotationDelegate initialized");
    }
}
