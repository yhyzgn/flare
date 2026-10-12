package com.yhy.http.flare.such.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.utils.Opt;

/**
 * 默认的字符串转换器工厂。
 * <p>
 * 产出的转换器直接调用 {@code Object#toString()} 把任意值转换为字符串，
 * {@code null} 转换为空字符串。
 * <p>
 * Created on 2025-09-15 14:54
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringConverterFactory implements StringConverter.Factory {

    /**
     * 获取字符串转换器。
     *
     * @param flare 当前 Flare 客户端实例
     * @return 基于 {@code toString()} 的字符串转换器
     */
    @Override
    public StringConverter<?> converter(Flare flare) {
        return new ToStringConverter<>();
    }

    private record ToStringConverter<T>() implements StringConverter<T> {

        /**
         * 把任意值转换为字符串。
         *
         * @param value 待转换值，可为 {@code null}
         * @return {@code value.toString()}，{@code null} 时返回空字符串
         */
        @Override
        public String convert(T value) {
            return Opt.ofNullable(value).map(Object::toString).orElse("");
        }
    }
}
