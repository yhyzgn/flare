package com.yhy.http.flare.such.convert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.BodyConverter;
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
 * 基于 Jackson 实现的 BodyConverter
 * <p>
 * Created on 2025-09-11 09:47
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class JacksonConverterFactory implements BodyConverter.Factory {
    private final ObjectMapper mapper;

    public JacksonConverterFactory() {
        this(new ObjectMapper());
    }

    public JacksonConverterFactory(ObjectMapper mapper) {
        // 排除json字符串中实体类没有的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.findAndRegisterModules();
        this.mapper = mapper;
    }

    @Override
    public @Nullable BodyConverter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Flare flare) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonRequestBodyBodyConverter<>(mapper, javaType, flare);
    }

    @Override
    public @Nullable BodyConverter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonResponseBodyBodyConverter<>(mapper, javaType, annotations, flare);
    }

    private record JacksonRequestBodyBodyConverter<T>(ObjectMapper mapper, JavaType type, Flare flare) implements BodyConverter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        @Override
        public Class<?> resultType() {
            return type.getRawClass();
        }

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

    private record JacksonResponseBodyBodyConverter<T>(ObjectMapper mapper, JavaType type, Annotation[] annotations, Flare flare) implements BodyConverter<ResponseBody, T> {

        @Override
        public Class<?> resultType() {
            return type.getRawClass();
        }

        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            return responseBodyResolve(from, annotations, fm -> mapper.readValue(fm.byteStream(), type), flare.stringConverter());
        }
    }
}
