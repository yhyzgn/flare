package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.provider.DispatcherProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * Spring 实现的请求分发器提供者实例委托类
 * <p>
 * Created on 2025-09-23 11:08
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
public class SpringDispatcherProviderDelegate implements DispatcherProviderDelegate, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public <T extends DispatcherProvider> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringDispatcherProviderDelegate initialized");
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
