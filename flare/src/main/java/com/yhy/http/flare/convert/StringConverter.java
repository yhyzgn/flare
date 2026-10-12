package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;

/**
 * 字符串转换器接口。
 * <p>
 * 把任意值转换为字符串，典型用途包括请求头值、路径参数、下载文件路径；
 * Spring 环境下实现可支持解析 placeholder（见 {@code SpringStringConverterFactory}）。
 *
 * @param <T> 待转换的值类型
 * <p>
 * Created on 2025-09-15 14:43
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface StringConverter<T> {

    /**
     * 把值转换为字符串。
     *
     * @param value 原始值
     * @return 转换后的字符串
     * @throws Exception 转换失败时抛出
     */
    String convert(T value) throws Exception;

    /**
     * 转换器工厂接口。
     * <p>
     * 为指定 Flare 实例创建字符串转换器。
     */
    interface Factory {

        /**
         * 创建字符串转换器。
         *
         * @param flare 当前 Flare 实例
         * @return 字符串转换器实例
         */
        StringConverter<?> converter(Flare flare);
    }
}
