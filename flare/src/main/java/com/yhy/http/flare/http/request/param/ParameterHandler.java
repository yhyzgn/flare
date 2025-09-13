package com.yhy.http.flare.http.request.param;

import com.yhy.http.flare.convert.Converter;
import com.yhy.http.flare.http.request.RequestBuilder;
import com.yhy.http.flare.utils.Assert;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.ReflectUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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

    public abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

    public final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<>() {
            @Override
            public void apply(RequestBuilder builder, @Nullable Iterable<T> values) throws IOException {
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

    public final ParameterHandler<Object> array() {
        return new ParameterHandler<>() {
            @Override
            public void apply(RequestBuilder builder, @Nullable Object values) throws IOException {
                if (values == null) return; // Skip null values.
                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    // noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    // --- inner classes ---

    public static class RelativeUrl extends ParameterHandler<Object> {
        private final Method method;
        private final int index;

        public RelativeUrl(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable Object value) {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "@Url parameter is null."));
            builder.setRelativeUrl(value);
        }
    }

    public static class Path<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final String name;
        private final String defaultValue;
        private final Converter<T, String> converter;
        private final boolean encoded;

        public Path(Method method, int index, String name, String defaultValue, boolean encoded, Converter<T, String> converter) {
            this.method = method;
            this.index = index;
            this.name = Objects.requireNonNull(name, "Path param name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.converter = converter;
            this.encoded = encoded;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            String pathValue = defaultValue;
            if (null != value) {
                pathValue = converter.convert(value);
            }
            Assert.hasText(pathValue, ReflectUtils.parameterError(method, index, "Path parameter \"" + name + "\" value must not be null."));
            builder.addPathParam(name, pathValue, encoded);
        }
    }

    public static class Query<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final Converter<T, String> converter;
        private final boolean encoded;

        public Query(String name, String defaultValue, boolean encoded, Converter<T, String> converter) {
            this.name = Objects.requireNonNull(name, "Query param name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.encoded = encoded;
            this.converter = converter;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            String queryValue = defaultValue;
            if (null != value) {
                queryValue = converter.convert(value);
            }
            builder.addQueryParam(name, Opt.of(queryValue).orElse(""), encoded);
        }
    }

    public static class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final Converter<T, String> converter;
        private final boolean encoded;

        public QueryMap(Method method, int index, Converter<T, String> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                value = new HashMap<>();
            }
            for (Map.Entry<String, T> et : value.entrySet()) {
                String etKey = et.getKey();
                Assert.notNull(etKey, ReflectUtils.parameterError(method, index, "Query map contained null key."));
                T etValue = et.getValue();
                Assert.notNull(etValue, ReflectUtils.parameterError(method, index, "Query map contained null value for key '" + etKey + "'."));
                String convertedValue = converter.convert(etValue);
                Assert.notNull(convertedValue, ReflectUtils.parameterError(method, index, "Query map value '"
                        + etValue
                        + "' converted to null by "
                        + converter.getClass().getName()
                        + " for key '"
                        + etKey
                        + "'."));
                builder.addQueryParam(etKey, convertedValue, encoded);
            }
        }
    }

    public static class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final String defaultValue;
        private final Converter<T, String> converter;
        private final boolean encoded;

        public Field(String name, String defaultValue, boolean encoded, Converter<T, String> converter) {
            this.name = Objects.requireNonNull(name, "Filed name can not be null.");
            this.defaultValue = "".equals(defaultValue) ? null : defaultValue;
            this.converter = converter;
            this.encoded = encoded;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            String fieldValue = defaultValue;
            if (null != value) {
                fieldValue = converter.convert(value);
            }
            builder.addFiled(name, Opt.ofNullable(fieldValue).orElse(""), encoded);
        }
    }

    public static class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final Converter<T, String> converter;
        private final boolean encoded;

        public FieldMap(Method method, int index, Converter<T, String> converter, boolean encoded) {
            this.method = method;
            this.index = index;
            this.converter = converter;
            this.encoded = encoded;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                value = new HashMap<>();
            }

            for (Map.Entry<String, T> et : value.entrySet()) {
                String etKey = et.getKey();
                Assert.notNull(etKey, ReflectUtils.parameterError(method, index, "Field map contained null key."));
                T etValue = et.getValue();
                Assert.notNull(etValue, ReflectUtils.parameterError(method, index, "Field map contained null value for key '" + etKey + "'."));
                String fieldEntry = converter.convert(etValue);
                Assert.notNull(fieldEntry, ReflectUtils.parameterError(method, index, "Field map value '"
                        + etValue
                        + "' converted to null by "
                        + converter.getClass().getName()
                        + " for key '"
                        + etKey
                        + "'."));

                builder.addFiled(etKey, fieldEntry, encoded);
            }
        }
    }

    public static class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> converter;

        public Header(String name, Converter<T, String> converter) {
            this.name = name;
            this.converter = converter;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            String headerValue = null == value ? null : converter.convert(value);
            builder.addHeader(name, Opt.ofNullable(headerValue).orElse(""));
        }
    }

    public static class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final Converter<T, String> converter;

        public HeaderMap(Method method, int index, Converter<T, String> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            if (value == null) {
                value = new HashMap<>();
            }

            for (Map.Entry<String, T> et : value.entrySet()) {
                String headerName = et.getKey();
                Assert.hasText(headerName, ReflectUtils.parameterError(method, index, "Header map contained empty key."));
                T headerValue = et.getValue();
                builder.addHeader(headerName, null == headerValue ? "" : converter.convert(headerValue));
            }
        }
    }

    public static final class Headers extends ParameterHandler<okhttp3.Headers> {
        private final Method method;
        private final int index;

        public Headers(Method method, int index) {
            this.method = method;
            this.index = index;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable okhttp3.Headers headers) {
            Assert.notNull(headers, ReflectUtils.parameterError(method, index, "Headers parameter must not be null."));
            builder.addHeaders(headers);
        }
    }

    public static final class Part<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final okhttp3.Headers headers;
        private final Converter<T, RequestBody> converter;

        public Part(Method method, int index, okhttp3.Headers headers, Converter<T, RequestBody> converter) {
            this.method = method;
            this.index = index;
            this.headers = headers;
            this.converter = converter;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                return;
            }
            RequestBody body = Assert.wrap(() -> converter.convert(value), e -> ReflectUtils.parameterError(method, index, "Unable to convert " + value + " to RequestBody", e));
            builder.addPart(headers, body);
        }
    }

    public static final class RawPart extends ParameterHandler<MultipartBody.Part> {
        public static final RawPart INSTANCE = new RawPart();

        private RawPart() {
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable MultipartBody.Part value) {
            if (value != null) {
                builder.addPart(value);
            }
        }
    }

    public static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        private final Method method;
        private final int index;
        private final Converter<T, RequestBody> valueConverter;
        private final String transferEncoding;

        public PartMap(Method method, int index, Converter<T, RequestBody> valueConverter, String transferEncoding) {
            this.method = method;
            this.index = index;
            this.valueConverter = valueConverter;
            this.transferEncoding = transferEncoding;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable Map<String, T> value) throws IOException {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "Part map was null."));
            for (Map.Entry<String, T> entry : value.entrySet()) {
                String etKey = entry.getKey();
                Assert.hasText(etKey, ReflectUtils.parameterError(method, index, "Part map contained empty key."));
                T etValue = entry.getValue();
                Assert.notNull(etValue, ReflectUtils.parameterError(method, index, "Part map contained null value for key '" + etKey + "'."));
                okhttp3.Headers headers = okhttp3.Headers.of("Content-Disposition", "form-data; name=\"" + etKey + "\"", "Content-Transfer-Encoding", transferEncoding);
                builder.addPart(headers, valueConverter.convert(etValue));
            }
        }
    }

    public static class Body<T> extends ParameterHandler<T> {
        private final Method method;
        private final int index;
        private final Converter<T, RequestBody> converter;

        public Body(Method method, int index, Converter<T, RequestBody> converter) {
            this.method = method;
            this.index = index;
            this.converter = converter;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            Assert.notNull(value, ReflectUtils.parameterError(method, index, "Body parameter value must not be null."));
            RequestBody body = value instanceof RequestBody requestBody ? requestBody : converter.convert(value);
            builder.body(body);
        }
    }

    public static final class Tag<T> extends ParameterHandler<T> {
        public final Class<T> clazz;

        public Tag(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void apply(RequestBuilder builder, @Nullable T value) {
            builder.addTag(clazz, value);
        }
    }
}
