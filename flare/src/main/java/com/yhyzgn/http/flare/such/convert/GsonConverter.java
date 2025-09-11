package com.yhyzgn.http.flare.such.convert;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.Strictness;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.yhyzgn.http.flare.Flare;
import com.yhyzgn.http.flare.convert.Converter;
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
 * 基于 Gson 实现的 Converter
 * <p>
 * Created on 2025-09-11 09:41
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class GsonConverter implements Converter.Factory {
    private final Gson gson;

    public GsonConverter() {
        this(new Gson());
    }

    public GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter<>(gson, adapter);
    }

    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Flare flare) {
        return new StringConverter<>();
    }

    private record GsonRequestBodyConverter<T>(Gson gson, TypeAdapter<T> adapter) implements Converter<T, RequestBody> {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = StandardCharsets.UTF_8;

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

    private record GsonResponseBodyConverter<T>(Gson gson, TypeAdapter<T> adapter) implements Converter<ResponseBody, T> {

        @Nullable
        @Override
        public T convert(ResponseBody from) throws IOException {
            JsonReader jsonReader = gson.newJsonReader(from.charStream());
            jsonReader.setStrictness(Strictness.LENIENT);
            try (from) {
                T result = adapter.read(jsonReader);
                if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                    throw new JsonIOException("JSON document was not fully consumed.");
                }
                return result;
            }
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
