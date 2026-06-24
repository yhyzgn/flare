package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
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
 * Spring 实现的动态请求头注入代理
 * <p>
 * Created on 2025-09-17 17:27
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(DynamicHeaderDelegate.class)
public class SpringDynamicHeaderDelegate implements DynamicHeaderDelegate, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    /**
     * 应用委托。
     *
     * @param clazz 类型
     * @return 处理结果
     */
    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 属性设置完成回调。
     *
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringDynamicHeaderDelegate initialized");
    }

    /**
     * 设置应用上下文。
     *
     * @param context 值
     * @throws BeansException 调用异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
