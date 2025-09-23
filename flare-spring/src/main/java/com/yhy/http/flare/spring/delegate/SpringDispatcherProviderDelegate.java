package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.DispatcherProviderDelegate;
import com.yhy.http.flare.provider.DispatcherProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

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
@RequiredArgsConstructor
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class SpringDispatcherProviderDelegate implements DispatcherProviderDelegate, InitializingBean {
    private final ApplicationContext context;

    @Override
    public <T extends DispatcherProvider> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringDispatcherProviderDelegate initialized");
    }
}
