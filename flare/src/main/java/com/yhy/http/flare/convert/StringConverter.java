package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 字符串转换器
 * <p>
 * Created on 2025-09-15 14:43
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface StringConverter<T> {

    /**
     * 转换字符串
     *
     * @param value 原始值
     * @return 转换后的值
     * @throws Exception 转换异常
     */
    String convert(T value) throws Exception;

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
        StringConverter<?> converter(Type type, Annotation[] annotations, Flare flare);
    }
}
