package com.yhy.http.flare.such.convert;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.convert.BodyConverter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 基于 Gson 实现的 {@link BodyConverter.Factory} 默认实现。
 * <p>
 * 请求方向把对象序列化为 UTF-8 JSON 请求体；响应方向以宽松模式读取 JSON 并转换为声明类型。
 * <p>
 * Created on 2025-09-11 09:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class GsonConverterFactory implements BodyConverter.Factory {
    private final Gson gson;

    /**
     * 创建使用默认 {@link Gson} 实例的工厂。
     */
    public GsonConverterFactory() {
        this(new Gson());
    }

    /**
     * 创建 GsonConverterFactory 实例。
     *
     * @param gson 自定义的 Gson 实例
     */
    public GsonConverterFactory(Gson gson) {
        this.gson = gson;
    }

    /**
     * 获取请求体转换器。
     * <p>
     * 字符串、字节数组与 {@link RequestBody} 原样传递，其余类型序列化为 JSON。
     *
     * @param type        请求体目标类型
     * @param annotations 参数上的注解
     * @param flare        当前 Flare 客户端实例
     * @return 基于 Gson TypeAdapter 的请求体转换器
     */
    @Override
    public BodyConverter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyBodyConverter<>(gson, adapter, type, flare);
    }

    /**
     * 获取响应体转换器。
     * <p>
     * 以宽松（LENIENT）模式解析响应 JSON，文档未消费完时抛出异常。
     *
     * @param type        响应体目标类型
     * @param annotations 方法上的注解
     * @param flare        当前 Flare 客户端实例
     * @return 基于 Gson TypeAdapter 的响应体转换器
     */
    @Override
    public BodyConverter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyBodyConverter<>(gson, adapter, type, annotations, flare);
    }

    private record GsonRequestBodyBodyConverter<T>(Gson gson, TypeAdapter<T> adapter, Type type, Flare flare) implements BodyConverter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

        /**
         * 请求体的原始类型。
         *
         * @return 构造工厂时传入的目标类型
         */
        @Override
        public Class<?> resultType() {
            return (Class<?>) type;
        }

        /**
         * 把请求对象转换为 JSON 请求体。
         * <p>
         * {@code null} 转为空请求体；字符串与字节数组按原内容发出；{@link RequestBody} 直接透传；
         * 其余类型经 Gson 序列化为 UTF-8 JSON。
         *
         * @param from 请求对象
         * @return JSON 请求体
         * @throws IOException 序列化写入失败时抛出
         */
        @Override
        public RequestBody convert(T from) throws IOException {
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
                    JsonWriter jsonWriter = gson.newJsonWriter(writer);
                    adapter.write(jsonWriter, from);
                    jsonWriter.close();
                    return RequestBody.create(buffer.readByteArray(), MEDIA_TYPE);
                }
            }
        }
    }

    private record GsonResponseBodyBodyConverter<T>(Gson gson, TypeAdapter<T> adapter, Type type, Annotation[] annotations, Flare flare) implements BodyConverter<ResponseBody, T> {

        /**
         * 响应体的目标类型。
         *
         * @return 构造工厂时传入的目标类型
         */
        @Override
        public Class<?> resultType() {
            return (Class<?>) type;
        }

        /**
         * 把 JSON 响应体反序列化为声明类型。
         * <p>
         * 以宽松模式读取 JSON；文档存在剩余内容时视为非法响应抛出 {@code JsonIOException}。
         *
         * @param from 原始响应体
         * @return 反序列化后的对象，可能为 {@code null}
         * @throws IOException 读取或解析响应体失败时抛出
         */
        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            return responseBodyResolve(from, annotations, fm -> {
                JsonReader jsonReader = gson.newJsonReader(from.charStream());
                jsonReader.setStrictness(Strictness.LENIENT);
                try (from) {
                    T result = adapter.read(jsonReader);
                    if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                        throw new JsonIOException("JSON document was not fully consumed.");
                    }
                    return result;
                }
            }, flare.stringConverter());
        }
    }
}
