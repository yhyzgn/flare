package com.yhy.http.flare.spring.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Flare starter 模块自动配置类。
 * <p>
 * 作为 starter 的配置入口随 {@link com.yhy.http.flare.spring.starter.annotation.EnableFlare}
 * 导入，注册为基础设施角色，加载时输出标记日志。
 * <p>
 * Created on 2025-09-18 11:18
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
public class FlareStarterAutoConfiguration implements InitializingBean {

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 输出自动配置加载完成的标记日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Flare starter auto configuration is loaded.");
    }
}
