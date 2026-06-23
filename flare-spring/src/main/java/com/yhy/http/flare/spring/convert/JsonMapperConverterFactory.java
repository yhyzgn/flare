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
 * JsonMapper 实现的 Body 转换器
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
     * 创建 JsonMapperConverterFactory 实例。
     *
     * @param mapper 映射
     */
    public JsonMapperConverterFactory(JsonMapper mapper) {
        super(mapper);
    }

    /**
     * 属性设置完成回调。
     *
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("JsonMapperConverterFactory initialized");
    }
}
