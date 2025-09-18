package com.yhy.http.flare.such.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.utils.Opt;

/**
 * 字符串转换器工厂
 * <p>
 * Created on 2025-09-15 14:54
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringConverterFactory implements StringConverter.Factory {

    @Override
    public StringConverter<?> converter(Flare flare) {
        return new ToStringConverter<>();
    }

    private record ToStringConverter<T>() implements StringConverter<T> {

        @Override
        public String convert(T value) {
            return Opt.ofNullable(value).map(Object::toString).orElse("");
        }
    }
}
