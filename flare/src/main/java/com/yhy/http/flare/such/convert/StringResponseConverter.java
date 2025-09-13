package com.yhy.http.flare.such.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.Converter;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * StringResponseConverter
 * <p>
 * Created on 2025-09-11 09:49
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringResponseConverter extends JacksonConverter {

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        return new StringResponseBodyConverter();
    }

    private static class StringResponseBodyConverter implements Converter<ResponseBody, String> {
        @Nullable
        @Override
        public String convert(ResponseBody from) throws IOException {
            return from.string();
        }
    }
}
