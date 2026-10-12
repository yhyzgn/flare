package com.yhy.http.flare.spring.convert;

import tools.jackson.databind.json.JsonMapper;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.such.convert.JacksonConverterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * 基于 {@code tools.jackson.databind} 的 {@link JsonMapper} 实现的请求体转换器工厂。
 * <p>
 * 注册为 Spring 基础设施 Bean，当容器中不存在自定义的 {@link BodyConverter.Factory} 时生效；
 * 下游可通过声明自定义 {@link BodyConverter.Factory} Bean 覆盖默认实现。
 * <p>
 * Created on 2025-09-17 17:26
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
@ConditionalOnMissingBean(BodyConverter.Factory.class)
public class JsonMapperConverterFactory extends JacksonConverterFactory implements InitializingBean {

    /**
     * 创建 {@link JsonMapperConverterFactory} 实例。
     *
     * @param mapper Jackson 3 的 {@link JsonMapper} 实例
     */
    public JsonMapperConverterFactory(JsonMapper mapper) {
        super(mapper);
    }

    /**
     * 所有 Bean 属性设置完成后由 Spring 容器调用的初始化回调。
     * <p>
     * 记录工厂初始化完成日志。
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("JsonMapperConverterFactory initialized");
    }
}
