package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.model.FormField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 表单字段转换器接口。
 * <p>
 * 把 {@code @Field} / {@code @Query} / {@code @Multipart} 参数的值转换为一组
 * {@link FormField}，供请求构建器进一步编码为查询参数或表单数据。
 *
 * @param <T> 待转换的参数值类型
 * <p>
 * Created on 2025-09-15 14:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FormFieldConverter<T> {

    /**
     * 把参数值转换为一组表单字段。
     *
     * @param name         参数名或字段名前缀（容器类型展开时用于拼装字段名）
     * @param value        待转换的参数值
     * @param encoded      值是否已完成 URL 编码
     * @param defaultValue 参数值为 {@code null} 时使用的默认值
     * @return 转换后的表单字段列表，可能包含多个字段
     */
    List<FormField<?>> convert(String name, T value, boolean encoded, String defaultValue);

    /**
     * 转换器工厂接口。
     * <p>
     * 按参数类型和注解挑选合适的表单字段转换器。
     */
    interface Factory {

        /**
         * 创建表单字段转换器。
         *
         * @param type        参数类型
         * @param annotations 参数上的注解
         * @param flare       当前 Flare 实例
         * @return 与类型匹配的转换器实例
         */
        FormFieldConverter<?> converter(Type type, Annotation[] annotations, Flare flare);
    }
}
