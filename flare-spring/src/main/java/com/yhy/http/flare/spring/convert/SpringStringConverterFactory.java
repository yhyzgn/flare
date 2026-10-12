package com.yhy.http.flare.spring.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 基于 Spring {@link Environment} 的 {@link StringConverter.Factory} 实现，支持解析 placeholder。
 * <p>
 * 注册为 Spring 基础设施 Bean，当容器中不存在自定义的 {@link StringConverter.Factory} 时生效；
 * 下游可通过声明自定义 {@link StringConverter.Factory} Bean 覆盖默认实现。
 * <p>
 * Created on 2025-09-17 17:14
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(StringConverter.Factory.class)
public class SpringStringConverterFactory implements StringConverter.Factory, InitializingBean, EnvironmentAware {
    private Environment environment;

    /**
     * 为指定 {@link Flare} 代理实例创建字符串转换器。
     * <p>
     * 返回的转换器会将待转换值通过 Spring {@link Environment} 解析 placeholder。
     *
     * @param flare 当前 {@link Flare} 代理实例
     * @return 绑定了 {@link Environment} 的字符串转换器实例
     */
    @Override
    public StringConverter<?> converter(Flare flare) {
        return new SpringStringConverter<>(environment);
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 记录工厂初始化完成日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("SpringStringConverterFactory initialized");
    }

    /**
     * 设置环境。
     *
     * @param environment 值
     */
    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    private record SpringStringConverter<T>(Environment environment) implements StringConverter<T> {

        /**
         * 转换数据。
         *
         * @param from 值
         * @return 处理结果
         */
        @Override
        public String convert(T from) {
            return null == from ? null : environment.resolvePlaceholders(from.toString());
        }
    }
}
