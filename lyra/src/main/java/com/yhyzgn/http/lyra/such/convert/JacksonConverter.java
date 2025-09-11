package com.yhyzgn.http.lyra.such.convert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhyzgn.http.lyra.Lyra;
import com.yhyzgn.http.lyra.convert.Converter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于 Jackson 实现的 Converter
 * <p>
 * Created on 2025-09-11 09:47
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class JacksonConverter implements Converter.Factory {
    private final ObjectMapper mapper;

    public JacksonConverter() {
        this(new ObjectMapper());
    }

    public JacksonConverter(ObjectMapper mapper) {
        // 排除json字符串中实体类没有的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.findAndRegisterModules();
        this.mapper = mapper;
    }

    @Override
    public @Nullable Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Lyra lyra) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonRequestBodyConverter<>(mapper, javaType);
    }

    @Override
    public @Nullable Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Lyra lyra) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonResponseBodyConverter<>(mapper, javaType);
    }

    @Override
    public @Nullable Converter<?, String> stringConverter(Type type, Annotation[] annotations, Lyra lyra) {
        return new StringConverter<>();
    }

    private record JacksonRequestBodyConverter<T>(ObjectMapper mapper, JavaType type) implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        @Override
        public @NotNull RequestBody convert(T from) throws IOException {
            switch (from) {
                case null -> {
                    return RequestBody.create("", MEDIA_TYPE);
                }
                case String text -> {
                    return RequestBody.create(text, MEDIA_TYPE);
                }
                case byte[] bytes -> {
                    return RequestBody.create(bytes, MEDIA_TYPE);
                }
                case RequestBody requestBody -> {
                    return requestBody;
                }
                default -> {
                    // 其他类型，序列化为json
                    Buffer buffer = new Buffer();
                    Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
                    JsonGenerator gen = mapper.writer().forType(type).createGenerator(writer);
                    mapper.writeValue(gen, from);
                    gen.close();
                    return RequestBody.create(buffer.readByteArray(), MEDIA_TYPE);
                }
            }
        }
    }

    private record JacksonResponseBodyConverter<T>(ObjectMapper mapper, JavaType type) implements Converter<ResponseBody, T> {

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            // 如果目标类型是String，则直接返回，避免jackson出现不识别无双引号的字符串类型
            if (type.getRawClass() == String.class) {
                return (T) from.string();
            }
            return mapper.readValue(from.byteStream(), type);
        }
    }

    private static final class StringConverter<T> implements Converter<T, String> {

        @Nullable
        @Override
        public String convert(T from) {
            return from.toString();
        }
    }
}
