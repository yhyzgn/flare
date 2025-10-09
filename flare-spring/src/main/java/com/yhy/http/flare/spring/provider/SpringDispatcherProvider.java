package com.yhy.http.flare.spring.provider;

import com.yhy.http.flare.provider.DispatcherProvider;
import com.yhy.http.flare.such.provider.VirtualThreadDispatcherProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * Spring Bean 实现的请求分发器提供者
 * <p>
 * Created on 2025-09-23 11:07
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(DispatcherProvider.class)
public class SpringDispatcherProvider extends VirtualThreadDispatcherProvider {
}

