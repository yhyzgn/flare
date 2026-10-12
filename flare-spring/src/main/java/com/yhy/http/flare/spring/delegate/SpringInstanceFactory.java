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
 * 通过 Spring 容器创建实例的工厂。
 *
 * @since 2.0.2
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(InstanceFactory.class)
public class SpringInstanceFactory implements InstanceFactory, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public Object create(Class<?> type) {
        return context.getBean(type);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringInstanceFactory initialized");
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
