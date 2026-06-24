package com.yhy.http.flare.http.request.param;

import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.http.request.RequestBuilder;
import com.yhy.http.flare.model.FormField;
import com.yhy.http.flare.utils.Assert;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.ReflectUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * <p>
 * Created on 2025-09-11 06:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ParameterHandler<T> {

    /**
     * 应用委托。
     *
     * @param builder 值
     * @param value 值
     * @throws Exception 调用异常
     */
    public abstract void apply(RequestBuilder builder, @Nullable T value) throws Exception;

    /**
     * iterable。
     *
     * @return 处理结果
     */
    public final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<>() {
            /**
             * 应用委托。
             *
             * @param builder 值
             * @param values 值
             * @throws Exception 调用异常
             */
            @Override
            public void apply(RequestBuilder builder, @Nullable Iterable<T> values) throws Exception {
                if (values == null) {
                    // Skip null values.
                    return;
                }
                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    /**
     * array。
     *
     * @return 处理结果
     */
    public final ParameterHandler<Object> array() {
        return new ParameterHandler<>() {
            /**
             * 应用委托。
             *
             * @param builder 值
             * @param values 对象
             * @throws Exception 调用异常
             */
            @Override
            public void apply(RequestBuilder builder, @Nullable Object values) throws Exception {
                if (values == null) return; // Skip null values.
                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    // noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    // --- inner classes ---

    /**
     * Relative Url类。
     *
     */
    public static class RelativeUrl extends ParameterHandler<Object> {
        private final Method method;
        private final int index;

        /**
         * 创建 RelativeUrl 实例。
         *
         * @param method 方法
         * @param index 值
         */
        public RelativeUrl(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 对象
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable Object value) {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "@Url parameter is null."));
            builder.setAbsoluteUrl(value.toString());
        }
    }

    /**
     * Path类。
     *
     */
    public static class Path<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final String name;
        private final String defaultValue;
        private final StringConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 Path 实例。
         *
         * @param method 方法
         * @param index 值
         * @param name 字符串
         * @param defaultValue 字符串
         * @param encoded 值
         * @param converter 字符串
         */
        public Path(Method method, int index, String name, String defaultValue, boolean encoded, StringConverter<T> converter) {
            this.method = method;
            this.index = index;
            this.name = Objects.requireNonNull(name, "Path param name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            String pathValue = defaultValue;
            if (null != value) {
                pathValue = converter.convert(value);
            }
            Assert.hasText(pathValue, ReflectUtils.parameterError(method, index, "Path parameter \"" + name + "\" value must not be null."));
            builder.addPathParam(name, pathValue, encoded);
        }
    }

    /**
     * Query类。
     *
     */
    public static class Query<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 Query 实例。
         *
         * @param name 字符串
         * @param defaultValue 字符串
         * @param encoded 值
         * @param converter 值
         */
        public Query(String name, String defaultValue, boolean encoded, FormFieldConverter<T> converter) {
            this.name = Objects.requireNonNull(name, "Query param name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.encoded = encoded;
            this.converter = converter;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            Assert.notNull(value, "Query parameter value must not be null.");
            boolean isPrimitiveOrString = ReflectUtils.isPrimitiveOrString(value.getClass());
            // 如果是基础类型或者String类型，则需要把 name 传入 converter.convert 方法，否则就省去 name 传入
            Opt.ofNullable(converter.convert(isPrimitiveOrString ? name : "", value, encoded, defaultValue))
                .ifValid(fieldList ->
                    fieldList.forEach(field -> {
                        if (field instanceof FormField.ValueFormField valueFormField) {
                            builder.addQueryParam(field.getName(), valueFormField);
                        }
                    })
                );
        }
    }

    /**
     * Query Map类。
     *
     */
    public static class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 QueryMap 实例。
         *
         * @param method 方法
         * @param index 值
         * @param converter 值
         * @param encoded 值
         */
        public QueryMap(Method method, int index, FormFieldConverter<T> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 字符串
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws Exception {
            if (value == null) {
                value = new HashMap<>();
            }
            for (Map.Entry<String, T> et : value.entrySet()) {
                String etKey = et.getKey();
                Assert.notNull(etKey, ReflectUtils.parameterError(method, index, "Query map contained null key."));
                T etValue = et.getValue();
                if (null == etValue) {
                    // Skip null values.
                    continue;
                }
                Opt.ofNullable(converter.convert(etKey, etValue, encoded, null))
                    .ifValid(fieldList ->
                        fieldList.forEach(field -> {
                            if (field instanceof FormField.ValueFormField valueFormField) {
                                builder.addQueryParam(field.getName(), valueFormField);
                            }
                        })
                    );
            }
        }
    }

    /**
     * Field类。
     *
     */
    public static class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 Field 实例。
         *
         * @param name 字符串
         * @param defaultValue 字符串
         * @param encoded 值
         * @param converter 值
         */
        public Field(String name, String defaultValue, boolean encoded, FormFieldConverter<T> converter) {
            this.name = Objects.requireNonNull(name, "Filed name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            Assert.notNull(value, "Field parameter value must not be null.");
            boolean isPrimitiveOrString = ReflectUtils.isPrimitiveOrString(value.getClass());
            // 如果是基础类型或者String类型，则需要把 name 传入 converter.convert 方法，否则就省去 name 传入
            Opt.ofNullable(converter.convert(isPrimitiveOrString ? name : "", value, encoded, defaultValue))
                .ifValid(fieldList ->
                    fieldList.forEach(field ->
                        builder.addFiled(field.getName(), field)
                    )
                );
        }
    }

    /**
     * Field Map类。
     *
     */
    public static class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 FieldMap 实例。
         *
         * @param method 方法
         * @param index 值
         * @param converter 值
         * @param encoded 值
         */
        public FieldMap(Method method, int index, FormFieldConverter<T> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 字符串
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws Exception {
            if (value == null) {
                value = new HashMap<>();
            }

            for (Map.Entry<String, T> et : value.entrySet()) {
                String etKey = et.getKey();
                Assert.notNull(etKey, ReflectUtils.parameterError(method, index, "Field map contained null key."));
                T etValue = et.getValue();
                if (null == etValue) {
                    // Skip null values.
                    continue;
                }
                Opt.ofNullable(converter.convert(etKey, etValue, encoded, null))
                    .ifValid(fieldList ->
                        fieldList.forEach(field ->
                            builder.addFiled(field.getName(), field)
                        )
                    );
            }
        }
    }

    /**
     * Multipart类。
     *
     */
    public static class Multipart<T> extends ParameterHandler<T> {
        private final String name;
        private final String filename;

        /**
         * 创建 Multipart 实例。
         *
         * @param name 字符串
         * @param filename 字符串
         */
        public Multipart(String name, String filename) {
            this.name = Objects.requireNonNull(name, "Filed name can not be null.");
            this.filename = filename;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            Assert.notNull(value, "Field parameter value must not be null.");
            Class<?> clazz = value.getClass();
            // 支持 File, byte[], InputStream 类型的字段
            if (clazz == File.class) {
                builder.addFiled(name, new FormField.FileFormField(name, (File) value, filename));
                return;
            }

            if (clazz == byte[].class) {
                builder.addFiled(name, new FormField.BytesFormField(name, (byte[]) value, filename));
                return;
            }

            if (InputStream.class.isAssignableFrom(clazz)) {
                builder.addFiled(name, new FormField.InputStreamFormField(name, (InputStream) value, filename));
                return;
            }

            throw new IllegalArgumentException(clazz.getName() + " is not a valid multipart field");
        }
    }

    /**
     * Header类。
     *
     */
    public static class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final StringConverter<T> converter;

        /**
         * 创建 Header 实例。
         *
         * @param name 字符串
         * @param converter 字符串
         */
        public Header(String name, StringConverter<T> converter) {
            this.name = name;
            this.converter = converter;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            String headerValue = null == value ? null : converter.convert(value);
            builder.addHeader(name, Opt.ofNullable(headerValue).orElse(""));
        }
    }

    /**
     * Header Map类。
     *
     */
    public static class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final StringConverter<T> converter;

        /**
         * 创建 HeaderMap 实例。
         *
         * @param method 方法
         * @param index 值
         * @param converter 字符串
         */
        public HeaderMap(Method method, int index, StringConverter<T> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 字符串
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws Exception {
            if (value == null) {
                value = new HashMap<>();
            }

            for (Map.Entry<String, T> et : value.entrySet()) {
                String headerName = et.getKey();
                Assert.hasText(headerName, ReflectUtils.parameterError(method, index, "Header map contained empty key."));
                T headerValue = et.getValue();
                if (null == headerValue) {
                    // Skip null values.
                    continue;
                }
                builder.addHeader(headerName, converter.convert(headerValue));
            }
        }
    }

    /**
     * Headers类。
     *
     */
    public static final class Headers extends ParameterHandler<okhttp3.Headers> {
        private final Method method;
        private final int index;

        /**
         * 创建 Headers 实例。
         *
         * @param method 方法
         * @param index 值
         */
        public Headers(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param headers 值
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable okhttp3.Headers headers) {
            Assert.notNull(headers, ReflectUtils.parameterError(method, index, "Headers parameter must not be null."));
            builder.addHeaders(headers);
        }
    }

    /**
     * Body类。
     *
     */
    public static class Body<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final BodyConverter<T, RequestBody> converter;

        /**
         * 创建 Body 实例。
         *
         * @param method 方法
         * @param index 值
         * @param converter 请求体
         */
        public Body(Method method, int index, BodyConverter<T, RequestBody> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws IOException 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "Body parameter value must not be null."));
            RequestBody body = value instanceof RequestBody requestBody ? requestBody : converter.convert(value);
            builder.body(body);
        }
    }

    /**
     * Tag类。
     *
     */
    public static final class Tag<T> extends ParameterHandler<T> {
        /**
         * clazz。
         *
         */
        public final Class<T> clazz;

        /**
         * 创建 Tag 实例。
         *
         * @param clazz 类型
         */
        public Tag(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) {
            builder.addTag(clazz, value);
        }
    }

    /**
     * Binary类。
     *
     */
    public static final class Binary<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;

        /**
         * 创建 Binary 实例。
         *
         * @param method 方法
         * @param index 值
         */
        public Binary(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 应用委托。
         *
         * @param builder 值
         * @param value 值
         * @throws Exception 调用异常
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "Binary parameter value must not be null."));
            switch (value) {
                case File file -> builder.body(RequestBody.create(file, MediaType.parse("application/octet-stream")));
                case byte[] bytes -> builder.body(RequestBody.create(bytes, MediaType.parse("application/octet-stream")));
                case InputStream inputStream -> {
                    RequestBody streamBody = new RequestBody() {
                        /**
                         * content Type。
                         *
                         * @return 处理结果
                         */
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse("application/octet-stream");
                        }

                        /**
                         * write To。
                         *
                         * @param sink 值
                         * @throws IOException 调用异常
                         */
                        @Override
                        public void writeTo(@NotNull BufferedSink sink) throws IOException {
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = inputStream.read(buffer)) != -1) {
                                sink.write(buffer, 0, len);
                            }
                        }

                        /**
                         * 判断请求体是否只能写入一次。
                         *
                         * @return 固定返回 true，InputStream 无法安全重复读取
                         */
                        @Override
                        public boolean isOneShot() {
                            return true;
                        }
                    };
                    builder.body(streamBody);
                }
                default -> throw new IllegalArgumentException(ReflectUtils.parameterError(method, index, "Body parameter must be File/InputStream/byte[] for now."));
            }
        }
    }
}
