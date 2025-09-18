package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;

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
         * @param flare Flare
         * @return 字符串转换器
         */
        StringConverter<?> converter(Flare flare);
    }
}
