package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Spring 实现的动态请求头注入代理
 * <p>
 * Created on 2025-09-17 17:27
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class SpringDynamicHeaderDelegate implements DynamicHeaderDelegate, InitializingBean {
    private final ApplicationContext context;

    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringDynamicHeaderDelegate initialized");
    }
}
