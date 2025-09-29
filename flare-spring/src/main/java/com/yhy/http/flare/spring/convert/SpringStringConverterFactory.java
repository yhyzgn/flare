package com.yhy.http.flare.spring.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring 实现的 StringConverter
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
public class SpringStringConverterFactory implements StringConverter.Factory, InitializingBean, EnvironmentAware {
    private Environment environment;

    @Override
    public StringConverter<?> converter(Flare flare) {
        return new SpringStringConverter<>(environment);
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("SpringStringConverterFactory initialized");
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    private record SpringStringConverter<T>(Environment environment) implements StringConverter<T> {

        @Override
        public String convert(T from) {
            String text = from.toString();
            // 判断处理 Spring 配置变量 ${xxx.xxx}
            if (StringUtils.isPlaceholdersPresent(text)) {
                return environment.resolvePlaceholders(text);
            }
            return text;
        }
    }
}
