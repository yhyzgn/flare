package com.yhy.http.flare.spring.delegate;

import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.delegate.ExceptionResolverDelegate;
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
 * 通过 Spring 容器创建异常处理器类实例的代理接口实现
 * <p>
 * Created on 2026-03-04 17:28
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(ExceptionResolverDelegate.class)
public class SpringExceptionResolverDelegate implements ExceptionResolverDelegate, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    /**
     * 应用委托。
     *
     * @param clazz 类型
     * @return 处理结果
     */
    @Override
    public <T extends Catcher.Resolver> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 属性设置完成回调。
     *
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringExceptionResolverDelegate initialized");
    }

    /**
     * 设置应用上下文。
     *
     * @param context 值
     * @throws Exception 调用异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
