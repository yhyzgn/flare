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
 * Spring 实现的动态请求头处理器委托。
 * <p>
 * 按类型从 Spring 容器获取 {@link Header.Dynamic} 实例；
 * 注册为基础设施 Bean（{@code @ConditionalOnMissingBean(DynamicHeaderDelegate.class)}），
 * 容器中存在自定义实现时让位。
 * <p>
 * Created on 2025-09-17 17:27
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @deprecated since 2.0.2; use {@link SpringInstanceFactory} instead. This API is planned for removal in 3.0.0.
 */
@Deprecated(since = "2.0.2")
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(DynamicHeaderDelegate.class)
public class SpringDynamicHeaderDelegate implements DynamicHeaderDelegate, InitializingBean, ApplicationContextAware {
    private ApplicationContext context;

    /**
     * 按类型从 Spring 容器获取动态请求头处理器实例。
     *
     * @param clazz 动态请求头处理器类型
     * @param <T> 动态请求头处理器类型
     * @return 容器中该类型的处理器实例
     */
    @Override
    public <T extends Header.Dynamic> T apply(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 记录委托初始化完成日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringDynamicHeaderDelegate initialized");
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
