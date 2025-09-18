package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.delegate.InterceptorDelegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Spring 实现的拦截器注入代理
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
public class SpringInterceptorDelegate implements InterceptorDelegate, InitializingBean {
    private final ApplicationContext context;

    @Override
    public <T extends Interceptor> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("SpringInterceptorDelegate initialized");
    }
}
