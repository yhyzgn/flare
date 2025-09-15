package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.model.FormField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 对象字段转换器
 * <p>
 * Created on 2025-09-15 14:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FormFieldConverter<T> {

    /**
     * 转换对象字段为 Map
     *
     * @param name         字段名称或者前缀
     * @param value        对象
     * @param encoded      是否已编码
     * @param defaultValue 默认值
     * @return Result
     */
    List<FormField<?>> convert(String name, T value, boolean encoded, String defaultValue);

    /**
     * 转换工厂接口
     */
    interface Factory {

        /**
         * 字符串转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare       Flare
         * @return 字符串转换器
         */
        FormFieldConverter<?> converter(Type type, Annotation[] annotations, Flare flare);
    }
}
