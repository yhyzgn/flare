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
 * 参数处理器。
 * <p>
 * 每个接口方法参数对应一个或多个 {@code ParameterHandler}，在 {@code RequestFactory}
 * 解析阶段根据参数注解（{@code @Url} / {@code @Path} / {@code @Query} / {@code @Field} /
 * {@code @Multipart} / {@code @Header} / {@code @Body} / {@code @Tag} / {@code @Binary}）
 * 创建，请求期把参数值写入 {@code RequestBuilder}。
 *
 * @param <T> 参数值类型
 * <p>
 * Created on 2025-09-11 06:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ParameterHandler<T> {

    /**
     * 把参数值应用到请求构建器。
     *
     * @param builder 请求构建器
     * @param value   参数值，可为 {@code null}（由实现决定处理方式）
     * @throws Exception 转换或应用失败
     */
    public abstract void apply(RequestBuilder builder, @Nullable T value) throws Exception;

    /**
     * 包装为逐元素应用的容器处理器。
     * <p>
     * 传入 {@code Iterable} 时对其每个元素分别应用当前处理器，{@code null} 集合跳过。
     *
     * @return 处理 Iterable 元素的包装处理器
     */
    public final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<>() {
            /**
             * 逐元素应用底层处理器。
             *
             * @param builder 请求构建器
             * @param values   待展开的元素集合，{@code null} 时跳过
             * @throws Exception 转换或应用失败
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
     * 包装为数组逐元素应用的容器处理器。
     * <p>
     * 传入数组时对其每个元素分别应用当前处理器，{@code null} 数组跳过。
     *
     * @return 处理数组元素的包装处理器
     */
    public final ParameterHandler<Object> array() {
        return new ParameterHandler<>() {
            /**
             * 逐元素应用底层处理器。
             *
             * @param builder 请求构建器
             * @param values   待展开的数组，{@code null} 时跳过
             * @throws Exception 转换或应用失败
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
     * {@code @Url} 参数处理器。
     * <p>
     * 用参数值覆盖本次请求的绝对 URL。
     */
    public static class RelativeUrl extends ParameterHandler<Object> {
        private final Method method;
        private final int index;

        /**
         * 创建 RelativeUrl 实例。
         *
         * @param method 代理方法，用于异常信息
         * @param index  参数下标，用于异常信息
         */
        public RelativeUrl(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 把参数值设为请求的绝对 URL。
         *
         * @param builder 请求构建器
         * @param value    绝对 URL 字符串或 HttpUrl/URI 对象，不能为 {@code null}
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable Object value) {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "@Url parameter is null."));
            builder.setAbsoluteUrl(value.toString());
        }
    }

    /**
     * {@code @Path} 参数处理器。
     * <p>
     * 把参数值替换进相对 URL 模板的同名占位符。
     *
     * @param <T> 参数值类型
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
         * @param method       代理方法，用于异常信息
         * @param index        参数下标，用于异常信息
         * @param name         路径占位符名称
         * @param defaultValue 值为 {@code null} 时使用的默认值，空串视为无默认值
         * @param encoded      值是否已完成 URL 编码
         * @param converter    值转字符串的转换器
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
         * 把路径参数值写入请求构建器。
         *
         * @param builder 请求构建器
         * @param value    参数值，{@code null} 时使用默认值
         * @throws Exception 值转字符串失败
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
     * {@code @Query} 及无注解参数处理器。
     * <p>
     * 把参数值转为查询参数；无注解参数按参数名作为查询参数名。
     *
     * @param <T> 参数值类型
     */
    public static class Query<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 Query 实例。
         *
         * @param name         查询参数名
         * @param defaultValue 值为 {@code null} 时使用的默认值，空串视为无默认值
         * @param encoded      值是否已完成 URL 编码
         * @param converter    值转表单字段的转换器
         */
        public Query(String name, String defaultValue, boolean encoded, FormFieldConverter<T> converter) {
            this.name = Objects.requireNonNull(name, "Query param name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.encoded = encoded;
            this.converter = converter;
        }

        /**
         * 把查询参数值写入请求构建器。
         * <p>
         * 基础类型或 String 会把参数名传给转换器；其他类型以空名展开为多个字段。
         *
         * @param builder 请求构建器
         * @param value    参数值，不能为 {@code null}
         * @throws Exception 转换失败
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
     * {@code @Query} Map 容器处理器。
     * <p>
     * 把 Map 的每个键值对展开为独立的查询参数，{@code null} 值跳过。
     *
     * @param <T> Map 值类型
     */
    public static class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 QueryMap 实例。
         *
         * @param method    代理方法，用于异常信息
         * @param index     参数下标，用于异常信息
         * @param converter 值转表单字段的转换器
         * @param encoded   值是否已完成 URL 编码
         */
        public QueryMap(Method method, int index, FormFieldConverter<T> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 逐键值对写入查询参数。
         * <p>
         * Map 为 {@code null} 时按空 Map 处理；{@code null} 键或值跳过。
         *
         * @param builder 请求构建器
         * @param value   查询参数 Map
         * @throws Exception 转换失败
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
     * {@code @Field} 参数处理器。
     * <p>
     * 把参数值写入表单字段（{@code @FormData} / {@code @X3WFormUrlEncoded} 语义）。
     *
     * @param <T> 参数值类型
     */
    public static class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 Field 实例。
         *
         * @param name         表单字段名
         * @param defaultValue 值为 {@code null} 时使用的默认值，空串视为无默认值
         * @param encoded      值是否已完成 URL 编码
         * @param converter    值转表单字段的转换器
         */
        public Field(String name, String defaultValue, boolean encoded, FormFieldConverter<T> converter) {
            this.name = Objects.requireNonNull(name, "Filed name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 把表单字段值写入请求构建器。
         * <p>
         * 基础类型或 String 会把字段名传给转换器；其他类型以空名展开为多个字段。
         *
         * @param builder 请求构建器
         * @param value    参数值，不能为 {@code null}
         * @throws Exception 转换失败
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
     * {@code @Field} Map 容器处理器。
     * <p>
     * 把 Map 的每个键值对展开为独立的表单字段，{@code null} 值跳过。
     *
     * @param <T> Map 值类型
     */
    public static class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final FormFieldConverter<T> converter;
        private final boolean encoded;

        /**
         * 创建 FieldMap 实例。
         *
         * @param method    代理方法，用于异常信息
         * @param index     参数下标，用于异常信息
         * @param converter 值转表单字段的转换器
         * @param encoded   值是否已完成 URL 编码
         */
        public FieldMap(Method method, int index, FormFieldConverter<T> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        /**
         * 逐键值对写入表单字段。
         * <p>
         * Map 为 {@code null} 时按空 Map 处理；{@code null} 键或值跳过。
         *
         * @param builder 请求构建器
         * @param value   表单字段 Map
         * @throws Exception 转换失败
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
     * {@code @Multipart} 参数处理器。
     * <p>
     * 支持 {@code File}、{@code byte[]}、{@code InputStream} 三种值类型，
     * 编码为 multipart 文件字段。
     *
     * @param <T> 参数值类型
     */
    public static class Multipart<T> extends ParameterHandler<T> {
        private final String name;
        private final String filename;

        /**
         * 创建 Multipart 实例。
         *
         * @param name     multipart 字段名
         * @param filename 上传文件名
         */
        public Multipart(String name, String filename) {
            this.name = Objects.requireNonNull(name, "Filed name can not be null.");
            this.filename = filename;
        }

        /**
         * 按实际值类型编码为对应的 multipart 文件字段。
         *
         * @param builder 请求构建器
         * @param value    File / byte[] / InputStream 类型的参数值，不能为 {@code null}
         * @throws Exception 值类型不受支持时抛出 {@code IllegalArgumentException}
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
     * {@code @Header} 参数处理器。
     * <p>
     * 把参数值转换为字符串写入请求头。
     *
     * @param <T> 参数值类型
     */
    public static class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final StringConverter<T> converter;

        /**
         * 创建 Header 实例。
         *
         * @param name      请求头名称
         * @param converter 值转字符串的转换器
         */
        public Header(String name, StringConverter<T> converter) {
            this.name = name;
            this.converter = converter;
        }

        /**
         * 把参数值写入请求头。
         * <p>
         * 值为 {@code null} 时写入空串。
         *
         * @param builder 请求构建器
         * @param value    请求头值
         * @throws Exception 值转字符串失败
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws Exception {
            String headerValue = null == value ? null : converter.convert(value);
            builder.addHeader(name, Opt.ofNullable(headerValue).orElse(""));
        }
    }

    /**
     * {@code @Header} Map 容器处理器。
     * <p>
     * 把 Map 的每个键值对写入同名请求头，{@code null} 值跳过。
     *
     * @param <T> Map 值类型
     */
    public static class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final StringConverter<T> converter;

        /**
         * 创建 HeaderMap 实例。
         *
         * @param method    代理方法，用于异常信息
         * @param index     参数下标，用于异常信息
         * @param converter 值转字符串的转换器
         */
        public HeaderMap(Method method, int index, StringConverter<T> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        /**
         * 逐键值对写入请求头。
         * <p>
         * Map 为 {@code null} 时按空 Map 处理；空键或 {@code null} 值跳过。
         *
         * @param builder 请求构建器
         * @param value   请求头 Map
         * @throws Exception 值转字符串失败
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
     * {@code @Header} okhttp3.Headers 容器处理器。
     * <p>
     * 把整个 {@code okhttp3.Headers} 对象批量写入请求头。
     */
    public static final class Headers extends ParameterHandler<okhttp3.Headers> {
        private final Method method;
        private final int index;

        /**
         * 创建 Headers 实例。
         *
         * @param method 代理方法，用于异常信息
         * @param index  参数下标，用于异常信息
         */
        public Headers(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 批量写入请求头。
         *
         * @param builder 请求构建器
         * @param headers 待批量写入的 OkHttp 请求头集合，不能为 {@code null}
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable okhttp3.Headers headers) {
            Assert.notNull(headers, ReflectUtils.parameterError(method, index, "Headers parameter must not be null."));
            builder.addHeaders(headers);
        }
    }

    /**
     * {@code @Body} 参数处理器。
     * <p>
     * 把业务对象经 {@code BodyConverter} 转换为请求体写入。
     *
     * @param <T> 参数值类型
     */
    public static class Body<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final BodyConverter<T, RequestBody> converter;

        /**
         * 创建 Body 实例。
         *
         * @param method    代理方法，用于异常信息
         * @param index     参数下标，用于异常信息
         * @param converter 业务对象转请求体的转换器
         */
        public Body(Method method, int index, BodyConverter<T, RequestBody> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        /**
         * 把参数值转换为请求体写入。
         * <p>
         * 值本身是 {@code RequestBody} 时直接使用，不再转换。
         *
         * @param builder 请求构建器
         * @param value    业务对象，不能为 {@code null}
         * @throws IOException 转换为请求体失败
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "Body parameter value must not be null."));
            RequestBody body = value instanceof RequestBody requestBody ? requestBody : converter.convert(value);
            builder.body(body);
        }
    }

    /**
     * {@code @Tag} 参数处理器。
     * <p>
     * 以参数类型为键为请求附加类型化 tag，供拦截器读取。
     *
     * @param <T> tag 值类型
     */
    public static final class Tag<T> extends ParameterHandler<T> {
        /**
         * tag 的类型键。
         */
        public final Class<T> clazz;

        /**
         * 创建 Tag 实例。
         *
         * @param clazz tag 类型
         */
        public Tag(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * 把 tag 值附加到请求上。
         *
         * @param builder 请求构建器
         * @param value    tag 值，可为 {@code null}
         */
        @Override
        public void apply(RequestBuilder builder, @Nullable T value) {
            builder.addTag(clazz, value);
        }
    }

    /**
     * {@code @Binary} 参数处理器。
     * <p>
     * 把 {@code File} / {@code byte[]} / {@code InputStream} 类型的参数值
     * 直接编码为 {@code application/octet-stream} 请求体。
     *
     * @param <T> 参数值类型
     */
    public static final class Binary<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;

        /**
         * 创建 Binary 实例。
         *
         * @param method 代理方法，用于异常信息
         * @param index  参数下标，用于异常信息
         */
        public Binary(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        /**
         * 按实际值类型编码为二进制请求体。
         * <p>
         * {@code InputStream} 会包装为一次性（one-shot）流式请求体。
         *
         * @param builder 请求构建器
         * @param value    File / byte[] / InputStream 类型的参数值，不能为 {@code null}
         * @throws Exception 值类型不受支持时抛出 {@code IllegalArgumentException}
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
                         * 流式请求体的媒体类型，固定为二进制流。
                         *
                         * @return octet-stream 媒体类型
                         */
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse("application/octet-stream");
                        }

                        /**
                         * 把底层输入流按 8KB 块写入请求体。
                         *
                         * @param sink OkHttp 输出缓冲区
                         * @throws IOException 读取流或写出的 I/O 错误
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
