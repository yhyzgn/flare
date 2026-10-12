package com.yhy.http.flare.such.convert;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
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
 * 基于 Jackson（{@code tools.jackson}）实现的 {@link BodyConverter.Factory} 默认实现。
 * <p>
 * 请求方向把对象序列化为 UTF-8 JSON 请求体；响应方向把 JSON 响应体反序列化为声明类型。
 * 这是 {@code Flare.Builder} 未显式配置转换器时的默认工厂。
 * <p>
 * Created on 2025-09-11 09:47
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class JacksonConverterFactory implements BodyConverter.Factory {
    private final JsonMapper mapper;

    /**
     * 创建 JacksonConverterFactory 实例。
     *
     * @param mapper Jackson 3 的 {@link JsonMapper} 实例
     */
    public JacksonConverterFactory(JsonMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 获取请求体转换器。
     * <p>
     * 字符串、字节数组与 {@link RequestBody} 原样传递，其余类型序列化为 JSON。
     *
     * @param type                请求体目标类型
     * @param parameterAnnotations 参数上的注解
     * @param flare                当前 Flare 客户端实例
     * @return 基于 Jackson 的请求体转换器
     */
    @Override
    public @Nullable BodyConverter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Flare flare) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonRequestBodyBodyConverter<>(mapper, javaType, flare);
    }

    /**
     * 获取响应体转换器。
     *
     * @param type        响应体目标类型
     * @param annotations 方法上的注解
     * @param flare        当前 Flare 客户端实例
     * @return 基于 Jackson 的响应体转换器
     */
    @Override
    public @Nullable BodyConverter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return new JacksonResponseBodyBodyConverter<>(mapper, javaType, annotations, flare);
    }

    private record JacksonRequestBodyBodyConverter<T>(JsonMapper mapper, JavaType type, Flare flare) implements BodyConverter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        /**
         * 请求体的原始类型。
         *
         * @return 目标类型的原始 Class
         */
        @Override
        public Class<?> resultType() {
            return type.getRawClass();
        }

        /**
         * 把请求对象转换为 JSON 请求体。
         * <p>
         * {@code null} 转为空请求体；字符串与字节数组按原内容发出；{@link RequestBody} 直接透传；
         * 其余类型经 Jackson 序列化为 UTF-8 JSON。
         *
         * @param from 请求对象
         * @return JSON 请求体
         * @throws IOException 序列化写入失败时抛出
         */
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

    private record JacksonResponseBodyBodyConverter<T>(JsonMapper mapper, JavaType type, Annotation[] annotations, Flare flare) implements BodyConverter<ResponseBody, T> {

        /**
         * 响应体的目标类型。
         *
         * @return 目标类型的原始 Class
         */
        @Override
        public Class<?> resultType() {
            return type.getRawClass();
        }

        /**
         * 把 JSON 响应体反序列化为声明类型。
         *
         * @param from 原始响应体
         * @return 反序列化后的对象，可能为 {@code null}
         * @throws IOException 读取或解析响应体失败时抛出
         */
        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            return responseBodyResolve(from, annotations, fm -> mapper.readValue(fm.byteStream(), type), flare.stringConverter());
        }
    }
}
