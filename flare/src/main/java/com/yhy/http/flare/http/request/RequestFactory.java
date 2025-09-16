package com.yhy.http.flare.http.request;

import com.google.common.collect.Lists;
import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.*;
import com.yhy.http.flare.annotation.Headers;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.method.*;
import com.yhy.http.flare.annotation.param.*;
import com.yhy.http.flare.convert.BodyConverter;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.convert.StringConverter;
import com.yhy.http.flare.delegate.DynamicHeaderDelegate;
import com.yhy.http.flare.delegate.InterceptorDelegate;
import com.yhy.http.flare.delegate.MethodAnnotationDelegate;
import com.yhy.http.flare.http.request.param.ParameterHandler;
import com.yhy.http.flare.model.HttpHeader;
import com.yhy.http.flare.model.Invocation;
import com.yhy.http.flare.such.delegate.ConstructorDynamicHeaderDelegate;
import com.yhy.http.flare.such.delegate.ConstructorInterceptorDelegate;
import com.yhy.http.flare.utils.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * <p>
 * Created on 2025-09-11 06:38
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class RequestFactory {
    private final Method method;
    private final HttpUrl host;
    private final String httpMethod;
    private final String relativeUrl;
    private final okhttp3.Headers headers;
    private final MediaType contentType;
    private final boolean isFormData;
    private final boolean isX3WFormUrlEncoded;
    private final List<List<ParameterHandler<?>>> parameterHandlers;
    private final List<okhttp3.Interceptor> netInterceptors;
    private final List<okhttp3.Interceptor> interceptors;
    private final Map<String, String> headerMap;
    private final List<Header.Dynamic> dynamicHeaders;
    private final MethodAnnotationDelegate methodAnnotationDelegate;

    private RequestFactory(Builder builder) {
        method = builder.method;
        httpMethod = builder.httpMethod;
        relativeUrl = builder.relativeUrl;
        headers = builder.headers;
        contentType = builder.contentType;
        isFormData = builder.isFormData;
        isX3WFormUrlEncoded = builder.isX3WFormUrlEncoded;
        parameterHandlers = builder.parameterHandlers;
        methodAnnotationDelegate = builder.methodAnnotationDelegate;

        // 合并全局配置和当前配置
        host = Optional.ofNullable(builder.baseUrl).orElse(builder.pigeon.baseUrl());
        headerMap = builder.pigeon.headers();

        // 拦截器按顺序合并，保证先执行局部拦截器，后执行全局拦截器
        netInterceptors = builder.netInterceptors;
        if (!builder.pigeon.netInterceptors().isEmpty()) {
            netInterceptors.addAll(builder.pigeon.netInterceptors().reversed());
        }
        interceptors = builder.interceptors;
        if (!builder.pigeon.interceptors().isEmpty()) {
            interceptors.addAll(builder.pigeon.interceptors().reversed());
        }
        // 优先使用局部 header，其次才是全局 header
        dynamicHeaders = builder.dynamicHeaders;
        if (!builder.pigeon.dynamicHeaders().isEmpty()) {
            dynamicHeaders.addAll(0, builder.pigeon.dynamicHeaders());
        }
    }

    public static RequestFactory parseAnnotations(Flare pigeon, Method method) {
        return new Builder(pigeon, method).build();
    }

    public Request create(OkHttpClient.Builder clientBuilder, Object[] args) throws Exception {
        // 自定义设置拦截器
        if (!netInterceptors.isEmpty()) {
            netInterceptors.forEach(clientBuilder::addNetworkInterceptor);
        }
        if (!interceptors.isEmpty()) {
            interceptors.forEach(clientBuilder::addInterceptor);
        }

        @SuppressWarnings("unchecked")
        List<List<ParameterHandler<Object>>> handlers = parameterHandlers.stream().filter(Objects::nonNull).map(it -> it.stream().filter(Objects::nonNull).map(dd -> (ParameterHandler<Object>) dd).collect(Collectors.toList())).toList();

        int argsCount = args.length;
        if (argsCount != handlers.size()) {
            throw new IllegalArgumentException("Argument count (" + argsCount + ") doesn't match expected count (" + handlers.size() + ")");
        }

        String relUrl = relativeUrl;
        if (host.uri().getPath().endsWith("/") && relUrl.startsWith("/")) {
            relUrl = relUrl.substring(1);
        }
        RequestBuilder builder = new RequestBuilder(httpMethod, host, relUrl, headers, contentType, isFormData, isX3WFormUrlEncoded);

        List<Object> argsList = new ArrayList<>(argsCount);
        for (int i = 0; i < argsCount; i++) {
            argsList.add(args[i]);
            for (ParameterHandler<Object> handler : handlers.get(i)) {
                handler.apply(builder, args[i]);
            }
        }

        Request.Builder bld = builder.get().tag(Invocation.class, Invocation.of(method, argsList));
        // 加上 User-Agent 信息
        bld.header("User-Agent", "Flare/" + Version.NAME);
        // 静态 header
        if (null != headerMap) {
            headerMap.forEach((k, v) -> {
                if (null != k && null != v) {
                    bld.header(k, v);
                }
            });
        }
        // 动态 header
        if (null != dynamicHeaders) {
            dynamicHeaders.forEach(dynamic -> {
                // 动态请求头
                HttpHeader hh = dynamic.header(method);
                if (null != hh && hh.isValid()) {
                    bld.header(hh.name(), hh.value());
                }
            });
        }
        return bld.build();
    }

    private static final class Builder {
        private static final String REGEX_PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        private static final Pattern REGEX_PARAM_URL = Pattern.compile("\\{(" + REGEX_PARAM + ")}.*?");
        private static final Pattern REGEX_PARAM_NAME = Pattern.compile(REGEX_PARAM);

        private final Flare pigeon;
        private final Method method;
        private final MethodAnnotationDelegate methodAnnotationDelegate;
        private final Parameter[] parameters;
        private String httpMethod;
        private final okhttp3.Headers.Builder headersBuilder;
        private okhttp3.Headers headers;
        private MediaType contentType;
        private boolean isFormData;
        private boolean isX3WFormUrlEncoded;
        private HttpUrl baseUrl;
        private String relativeUrl;
        private Set<String> relativeUrlParamNames;
        private List<List<ParameterHandler<?>>> parameterHandlers;
        private final List<okhttp3.Interceptor> netInterceptors;
        private final List<okhttp3.Interceptor> interceptors;
        private final List<Header.Dynamic> dynamicHeaders;

        Builder(Flare pigeon, Method method) {
            this.pigeon = pigeon;
            this.method = method;
            this.methodAnnotationDelegate = pigeon.methodAnnotationDelegate();
            this.parameters = method.getParameters();
            this.headersBuilder = new okhttp3.Headers.Builder();
            this.dynamicHeaders = new ArrayList<>();
            this.netInterceptors = new ArrayList<>();
            this.interceptors = new ArrayList<>();
        }

        RequestFactory build() {
            parseMethodAnnotation();

            Assert.hasText(httpMethod, ReflectUtils.methodError(method, "HTTP method annotation is required (e.g., @Get, @Post, etc.)."));

            int paramCount = parameters.length;
            parameterHandlers = new ArrayList<>(paramCount);
            for (int i = 0, last = paramCount - 1; i < paramCount; i++) {
                parameterHandlers.add(i, parseParameter(i));
            }

            return new RequestFactory(this);
        }

        @Nullable
        private List<ParameterHandler<?>> parseParameter(int paramIndex) {
            Parameter parameter = parameters[paramIndex];
            Annotation[] annotations = parameter.getAnnotations();
            Type type = parameter.getParameterizedType();
            if (annotations.length > 0) {
                return Arrays.stream(annotations).map(annotation -> parseParameterAnnotation(paramIndex, type, parameter, annotations, annotation)).collect(Collectors.toList());
            }
            return Lists.newArrayList(parseParameterParameter(paramIndex, type, parameter));
        }

        private ParameterHandler<?> parseParameterParameter(int paramIndex, Type type, Parameter parameter) {
            return parseParameterQuery(type, parameter.getName(), null, false, paramIndex, null);
        }

        private ParameterHandler<?> parseParameterAnnotation(int paramIndex, Type type, Parameter parameter, Annotation[] annotations, Annotation annotation) {
            validateResolvableType(paramIndex, type);

            if (annotation instanceof Url) {
                Assert.isNull(relativeUrl, ReflectUtils.parameterError(method, paramIndex, "@Url cannot be used with @%s URL", relativeUrl));
                if (type == String.class || type == HttpUrl.class || type == URI.class) {
                    return new ParameterHandler.RelativeUrl(method, paramIndex);
                } else {
                    throw ReflectUtils.parameterError(method, paramIndex, "@Url must be okhttp3.HttpUrl, String, java.net.URI type.");
                }
            } else if (annotation instanceof Path path) {
                Assert.hasText(relativeUrl, ReflectUtils.parameterError(method, paramIndex, "@Path can only be used with relative url not empty."));
                String name = Opt.ofNullable(path.value()).orElse(parameter.getName());
                validatePathName(paramIndex, name);
                StringConverter<?> converter = pigeon.stringConverter(type, annotations);
                return new ParameterHandler.Path<>(method, paramIndex, name, path.defaultValue(), path.encoded(), converter);
            } else if (annotation instanceof Query query) {
                String name = Opt.ofNullable(query.value()).orElse(parameter.getName());
                return parseParameterQuery(type, name, query.defaultValue(), query.encoded(), paramIndex, annotations);
            } else if (annotation instanceof Field field) {
                Assert.isTrue(isFormData || isX3WFormUrlEncoded, ReflectUtils.parameterError(method, paramIndex, "@Field can only be used with form encoding."));
                String name = Opt.ofNullable(field.value()).orElse(parameter.getName());
                boolean encoded = field.encoded();

                Class<?> rawType = ReflectUtils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawType)) {
                    if (!(type instanceof ParameterizedType parameterizedType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                    }
                    Type iterableType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                    FormFieldConverter<?> converter = pigeon.formFieldConverter(iterableType, annotations);
                    return new ParameterHandler.Field<>(name, field.defaultValue(), encoded, converter).iterable();
                } else if (rawType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                    FormFieldConverter<?> converter = pigeon.formFieldConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Field<>(name, field.defaultValue(), encoded, converter).array();
                } else if (Map.class.isAssignableFrom(rawType)) {
                    Class<?> rawParameterType = ReflectUtils.getRawType(type);
                    if (!Map.class.isAssignableFrom(rawParameterType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, "@Field Map parameter type must be Map.");
                    }
                    Type mapType = ReflectUtils.getSupertype(type, rawParameterType, Map.class);
                    if (!(mapType instanceof ParameterizedType parameterizedType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, "Map must include generic types (e.g., Map<String, Object>)");
                    }

                    Type keyType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                    if (String.class != keyType) {
                        throw ReflectUtils.parameterError(method, paramIndex, "@Field Map keys must be of type String: " + keyType);
                    }
                    Type valueType = ReflectUtils.getParameterUpperBound(1, parameterizedType);
                    FormFieldConverter<?> converter = pigeon.formFieldConverter(valueType, annotations);
                    return new ParameterHandler.FieldMap<>(method, paramIndex, converter, encoded);
                } else {
                    FormFieldConverter<?> converter = pigeon.formFieldConverter(type, annotations);
                    return new ParameterHandler.Field<>(name, field.defaultValue(), encoded, converter);
                }
            } else if (annotation instanceof Multipart multipart) {
                Assert.isTrue(isFormData, ReflectUtils.parameterError(method, paramIndex, "@Multipart parameters can only be used with @FormData."));
                String name = Opt.ofNullable(multipart.value()).orElse(parameter.getName());
                FormFieldConverter<?> converter = pigeon.formFieldConverter(type, annotations);
                return new ParameterHandler.Multipart<>(name, multipart.filename());
            } else if (annotation instanceof Header header) {
                String name = header.value();

                Class<?> rawType = ReflectUtils.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawType)) {
                    if (!(type instanceof ParameterizedType parameterizedType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                    }
                    Type iterableType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                    StringConverter<?> converter = pigeon.stringConverter(iterableType, annotations);
                    return new ParameterHandler.Header<>(name, converter).iterable();
                } else if (okhttp3.Headers.class.isAssignableFrom(rawType)) {
                    return new ParameterHandler.Headers(method, paramIndex);
                } else if (rawType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                    StringConverter<?> converter = pigeon.stringConverter(arrayComponentType, annotations);
                    return new ParameterHandler.Header<>(name, converter).array();
                } else if (Map.class.isAssignableFrom(rawType)) {
                    Class<?> rawParameterType = ReflectUtils.getRawType(type);
                    if (!Map.class.isAssignableFrom(rawParameterType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, "@Header Map parameter type must be Map.");
                    }
                    Type mapType = ReflectUtils.getSupertype(type, rawParameterType, Map.class);
                    if (!(mapType instanceof ParameterizedType parameterizedType)) {
                        throw ReflectUtils.parameterError(method, paramIndex, "Map must include generic types (e.g., Map<String, Object>)");
                    }

                    Type keyType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                    if (String.class != keyType) {
                        throw ReflectUtils.parameterError(method, paramIndex, "@Header Map keys must be of type String: " + keyType);
                    }
                    Type valueType = ReflectUtils.getParameterUpperBound(1, parameterizedType);
                    StringConverter<?> converter = pigeon.stringConverter(valueType, annotations);
                    return new ParameterHandler.HeaderMap<>(method, paramIndex, converter);
                } else {
                    StringConverter<?> converter = pigeon.stringConverter(type, annotations);
                    return new ParameterHandler.Header<>(name, converter);
                }
            } else if (annotation instanceof Binary) {
                Assert.isFalse(isFormData || isX3WFormUrlEncoded, ReflectUtils.parameterError(method, paramIndex, "@Binary parameters can only be used with multipart encoding."));
                contentType = MediaType.parse("application/octet-stream");
                return new ParameterHandler.Binary<>(method, paramIndex);
            } else if (annotation instanceof Body) {
                Assert.isFalse(isFormData || isX3WFormUrlEncoded, ReflectUtils.parameterError(method, paramIndex, "@Body parameters cannot be used with form or multi-multipart encoding."));
                contentType = MediaType.parse("application/json; charset=utf-8");
                BodyConverter<?, RequestBody> converter = pigeon.requestConverter(type, annotations);
                return new ParameterHandler.Body<>(method, paramIndex, converter);
            } else if (annotation instanceof Tag) {
                Class<?> tagType = ReflectUtils.getRawType(type);
                for (int i = paramIndex - 1; i >= 0; i--) {
                    int constI = i;
                    parameterHandlers.get(i).forEach(otherHandler -> {
                        if (otherHandler instanceof ParameterHandler.Tag<?> parameterTag && parameterTag.clazz.equals(tagType)) {
                            throw ReflectUtils.parameterError(method, paramIndex, "@Tag type " + tagType.getName() + " is duplicate of parameter #" + (constI + 1) + " and would always overwrite its value.");
                        }
                    });
                }
                return new ParameterHandler.Tag<>(tagType);
            }
            return null;
        }

        private ParameterHandler<?> parseParameterQuery(Type type, String name, String defaultValue, boolean encoded, int paramIndex, Annotation[] annotations) {
            Class<?> rawType = ReflectUtils.getRawType(type);
            if (Iterable.class.isAssignableFrom(rawType)) {
                if (!(type instanceof ParameterizedType parameterizedType)) {
                    throw ReflectUtils.parameterError(method, paramIndex, rawType.getSimpleName() + " must include generic type (e.g., " + rawType.getSimpleName() + "<String>)");
                }
                Type iterableType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                FormFieldConverter<?> converter = pigeon.formFieldConverter(iterableType, annotations);
                return new ParameterHandler.Query<>(name, defaultValue, encoded, converter).iterable();
            } else if (rawType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(rawType.getComponentType());
                FormFieldConverter<?> converter = pigeon.formFieldConverter(arrayComponentType, annotations);
                return new ParameterHandler.Query<>(name, defaultValue, encoded, converter).array();
            } else if (Map.class.isAssignableFrom(rawType)) {
                Type mapType = ReflectUtils.getSupertype(type, rawType, Map.class);
                if (!(mapType instanceof ParameterizedType parameterizedType)) {
                    throw ReflectUtils.parameterError(method, paramIndex, "Map must include generic types (e.g., Map<String, Object>)");
                }
                Type keyType = ReflectUtils.getParameterUpperBound(0, parameterizedType);
                if (String.class != keyType) {
                    throw ReflectUtils.parameterError(method, paramIndex, "@Query Map keys must be of type String: " + keyType);
                }
                Type valueType = ReflectUtils.getParameterUpperBound(1, parameterizedType);
                FormFieldConverter<?> converter = pigeon.formFieldConverter(valueType, annotations);
                return new ParameterHandler.QueryMap<>(method, paramIndex, converter, encoded);
            } else {
                FormFieldConverter<?> converter = pigeon.formFieldConverter(type, annotations);
                return new ParameterHandler.Query<>(name, defaultValue, encoded, converter);
            }
        }

        private void parseMethodAnnotation() {
            methodAnnotationDelegate.apply(method, Get.class).forEach(annotation -> {
                parseHttpMethodAndPath("GET", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Post.class).forEach(annotation -> {
                parseHttpMethodAndPath("POST", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Delete.class).forEach(annotation -> {
                parseHttpMethodAndPath("DELETE", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Head.class).forEach(annotation -> {
                parseHttpMethodAndPath("HEAD", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Options.class).forEach(annotation -> {
                parseHttpMethodAndPath("OPTIONS", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Patch.class).forEach(annotation -> {
                parseHttpMethodAndPath("PATCH", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Put.class).forEach(annotation -> {
                parseHttpMethodAndPath("PUT", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Trace.class).forEach(annotation -> {
                parseHttpMethodAndPath("TRACE", annotation.value());
            });

            methodAnnotationDelegate.apply(method, Header.class).forEach(this::parseHeader);
            methodAnnotationDelegate.apply(method, Headers.class).forEach(annotation -> {
                parseHeader(annotation.value());
            });

            methodAnnotationDelegate.apply(method, X3WFormUrlEncoded.class).forEach(annotation -> {
                // x-www-form-urlencoded 表单上传
                Assert.isFalse(isFormData, ReflectUtils.methodError(method, "Only one encoding annotation is allowed."));
                isX3WFormUrlEncoded = true;
            });

            methodAnnotationDelegate.apply(method, FormData.class).forEach(annotation -> {
                // form 表单上传
                Assert.isFalse(isX3WFormUrlEncoded, ReflectUtils.methodError(method, "Only one encoding annotation is allowed."));
                isFormData = true;
            });

            methodAnnotationDelegate.apply(method, BaseUrl.class).forEach(annotation -> {
                baseUrl = HttpUrl.get(annotation.value());
            });

            methodAnnotationDelegate.apply(method, Interceptor.class).forEach(this::parseInterceptors);
            methodAnnotationDelegate.apply(method, Interceptors.class).forEach(annotation -> {
                parseInterceptors(annotation.value());
            });
        }

        private void parseInterceptors(Interceptor... annotation) {
            if (null == annotation) return;
            for (Interceptor ano : annotation) {
                Class<? extends okhttp3.Interceptor> clazz = ano.value();
                InterceptorDelegate delegate = pigeon.interceptorDelegate();
                if (null == delegate) {
                    delegate = ConstructorInterceptorDelegate.create();
                }
                try {
                    // 获取空参数构造函数，并创建对象
                    okhttp3.Interceptor interceptor = delegate.apply(clazz);
                    if (ano.net()) {
                        netInterceptors.add(interceptor);
                    } else {
                        interceptors.add(interceptor);
                    }
                } catch (Exception e) {
                    Assert.thr(new IllegalArgumentException("The Interceptor class must implements okhttp3.Interceptor and provide a empty argument constructor or a InterceptorProvider.", e));
                }
            }
        }

        private void parseHeader(Header... annotation) {
            if (null == annotation) return;
            for (Header header : annotation) {
                String headerName = null;
                String headerValue = null;
                // 先检查value
                if (StringUtils.isNotEmpty(header.value())) {
                    int index = header.value().indexOf(":");
                    Assert.isTrue(index > 0, "Header value must be in the form \"Name: Value\". Found: \"%s\"", header.value());
                    headerName = header.value().substring(0, index).trim();
                    headerValue = header.value().substring(index + 1).trim();
                } else if (header.dynamic() != Header.Dynamic.class && Header.Dynamic.class.isAssignableFrom(header.dynamic())) {
                    Class<? extends Header.Dynamic> pairClass = header.dynamic();
                    DynamicHeaderDelegate delegate = pigeon.headerDelegate();
                    if (null == delegate) {
                        delegate = ConstructorDynamicHeaderDelegate.create();
                    }
                    try {
                        Header.Dynamic headerDynamic = delegate.apply(pairClass);
                        // 动态请求头
                        HttpHeader hh = headerDynamic.header(method);
                        if (null != hh && hh.isValid()) {
                            headersBuilder.add(hh.name(), hh.value());
                        }
                        continue;
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                } else {
                    // 如果 value 为空，再从 pair 中获取
                    Assert.notNull(header.pair(), ReflectUtils.methodError(method, "@Header pairName and pairValue can not be empty"));
                    Assert.hasText(header.pair().name(), ReflectUtils.methodError(method, "@Header pairName can not be empty"));
                    headerName = header.pair().name();
                    headerValue = Opt.ofNullable(header.pair().value()).orElse("");
                }

                if ("Content-Type".equalsIgnoreCase(headerName)) {
                    contentType = MediaType.get(headerValue);
                }

                StringConverter<String> converter = pigeon.stringConverter(String.class, new Annotation[]{});
                try {
                    headerValue = converter.convert(headerValue);
                } catch (Exception e) {
                    log.error("", e);
                }
                headersBuilder.add(headerName, Opt.ofNullable(headerValue).orElse(""));
            }
            headers = headersBuilder.build();
        }


        private void parseHttpMethodAndPath(String httpMethod, String url) {
            if (null == url) {
                return;
            }
            Assert.isNull(this.httpMethod, ReflectUtils.methodError(method, "Only one http method is allowed, but found : %s and %s", this.httpMethod, httpMethod));
            this.httpMethod = httpMethod.toUpperCase();

            // 如果地址中包含get参数，则参数部分不能含有RESTful参数
            int index = url.indexOf("?");
            if (index > 0 && index < url.length() - 1) {
                String queryParams = url.substring(index + 1);
                Assert.isFalse(REGEX_PARAM_URL.matcher(queryParams).find(), ReflectUtils.methodError(method, "URL query string \"%s\" must not have replace block. For dynamic query parameters use @Query.", queryParams));
            }
            this.relativeUrl = url;
            this.relativeUrlParamNames = parseUrlParams(url);
        }

        private Set<String> parseUrlParams(String url) {
            Matcher matcher = REGEX_PARAM_URL.matcher(url);
            Set<String> result = new LinkedHashSet<>();
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            return result;
        }

        private void validatePathName(int index, String name) {
            Assert.isTrue(REGEX_PARAM_NAME.matcher(name).matches(), ReflectUtils.parameterError(method, index, "@Path parameter name must match %s. Found: %s", REGEX_PARAM_URL.pattern(), name));
            Assert.isTrue(relativeUrlParamNames.contains(name), ReflectUtils.parameterError(method, index, "URL \"%s\" does not contain \"{%s}\".", relativeUrl, name));
        }

        private void validateResolvableType(int index, Type type) {
            Assert.isFalse(ReflectUtils.hasUnresolvableType(type), ReflectUtils.parameterError(method, index, "Parameter type must not include a type variable or wildcard: %s", type));
        }

        private static Class<?> boxIfPrimitive(Class<?> type) {
            if (boolean.class == type) return Boolean.class;
            if (byte.class == type) return Byte.class;
            if (char.class == type) return Character.class;
            if (double.class == type) return Double.class;
            if (float.class == type) return Float.class;
            if (int.class == type) return Integer.class;
            if (long.class == type) return Long.class;
            if (short.class == type) return Short.class;
            return type;
        }
    }
}
