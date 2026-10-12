package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.MethodAnnotationDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Spring 实现的方法注解提取器委托。
 * <p>
 * 基于 Spring {@link MergedAnnotations}（{@code TYPE_HIERARCHY} 搜索策略）解析方法上的注解，
 * 支持组合注解与元注解的合并语义；注册为基础设施 Bean
 * （{@code @ConditionalOnMissingBean(MethodAnnotationDelegate.class)}），容器中存在自定义实现时让位。
 * <p>
 * Created on 2025-09-17 17:29
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(MethodAnnotationDelegate.class)
public class SpringMethodAnnotationDelegate implements MethodAnnotationDelegate, InitializingBean {

    /**
     * 提取方法上指定类型的注解列表。
     * <p>
     * 使用 Spring 合并注解语义（{@code TYPE_HIERARCHY} 搜索策略）解析，
     * 合并结果为空时回退到 JDK 直接注解。
     *
     * @param method 目标方法
     * @param annotationClass 注解类型
     * @param <T> 注解类型
     * @return 方法上该类型的注解列表，可能为空列表，不含 {@code null} 元素
     */
    @Override
    public <T extends Annotation> List<T> apply(Method method, Class<T> annotationClass) {
        MergedAnnotations annotations = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
        // 使用 stream() 获取所有 Header 注解
        List<T> result = annotations.stream(annotationClass)
                .map(MergedAnnotation::synthesize) // 合成每个注解实例
                .toList();
        return (CollectionUtils.isEmpty(result) ? Collections.singletonList(method.getAnnotation(annotationClass)) : result).stream().filter(Objects::nonNull).toList();
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 记录委托初始化完成日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringMethodAnnotationDelegate initialized");
    }
}
