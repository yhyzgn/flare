package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.InstanceFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * 通过 Spring 容器创建实例的统一实例工厂。
 * <p>
 * 注册为 Spring 基础设施 Bean（{@code @ConditionalOnMissingBean(InstanceFactory.class)}），
 * 当容器中不存在自定义 {@link InstanceFactory} 时生效；下游可通过声明自定义
 * {@link InstanceFactory} Bean 覆盖默认实现。实例通过
 * {@link ApplicationContext#getBean(Class)} 按类型从容器获取。
 * <p>
 * Created on 2026-10-12 14:25
 *
 * @author 颜洪毅
 * @version 2.0.2
 * @since 2.0.2
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(InstanceFactory.class)
public class SpringInstanceFactory implements InstanceFactory, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    /**
     * 从 Spring 容器中获取指定类型的 Bean。
     *
     * @param type 目标类型
     * @return 容器中该类型的 Bean 实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 容器中不存在该类型的 Bean
     */
    @Override
    public Object create(Class<?> type) {
        return context.getBean(type);
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 记录工厂初始化完成日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringInstanceFactory initialized");
    }

    /**
     * 设置应用上下文。
     *
     * @param context Spring 应用上下文
     * @throws BeansException 上下文处理异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
