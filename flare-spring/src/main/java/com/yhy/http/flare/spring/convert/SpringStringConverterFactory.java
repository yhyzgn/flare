package com.yhy.http.flare.spring.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

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
@RequiredArgsConstructor
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Component
public class SpringStringConverterFactory implements StringConverter.Factory, InitializingBean {
    private final Environment environment;

    @Override
    public StringConverter<?> converter(Type type, Annotation[] annotations, Flare flare) {
        return new SpringStringConverter<>(environment);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("SpringStringConverterFactory initialized");
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
