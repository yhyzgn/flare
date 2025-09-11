package com.yhyzgn.http.flare.convert;

import com.yhyzgn.http.flare.Flare;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 转换器接口
 * <p>
 * Created on 2025-09-10 15:51
 *
 * @param <F> 源对象类型
 * @param <T> 目标对象类型
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Converter<F, T> {

    /**
     * 转换
     *
     * @param from 源对象
     * @return 目标对象
     * @throws IOException 转换异常
     */
    T convert(F from) throws IOException;

    /**
     * 转换工厂接口
     */
    interface Factory {

        /**
         * 请求体转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare        Flare
         * @return 请求体转换器
         */
        Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations, Flare flare);

        /**
         * 响应体转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare        Flare
         * @return 响应体转换器
         */
        Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare);

        /**
         * 字符串转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare        Flare
         * @return 字符串转换器
         */
        Converter<?, String> stringConverter(Type type, Annotation[] annotations, Flare flare);
    }
}
