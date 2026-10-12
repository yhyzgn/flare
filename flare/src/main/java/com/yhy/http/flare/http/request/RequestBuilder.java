package com.yhy.http.flare.http.request;

import com.google.gson.internal.LinkedTreeMap;
import com.yhy.http.flare.model.FormField;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.StringUtils;
import lombok.Setter;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OkHttp 请求构建器。
 * <p>
 * 收集 {@code ParameterHandler} 产出的路径参数、查询参数、表单字段、请求头与请求体，
 * 在 {@link #get()} 时统一编码为最终的 {@link Request.Builder}。路径参数与请求头采用
 * 后写覆盖语义，保证靠近代理方法的配置优先生效。
 * <p>
 * Created on 2025-09-13 13:39
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class RequestBuilder {
    private final HttpUrl baseUrl;
    private final String method;
    private final Request.Builder requestBuilder;
    private final Headers.Builder headersBuilder;

    private final Map<String, Object> pathParamMap;
    private final Map<String, List<FormField.ValueFormField>> queryParamMap;
    private final Map<String, List<FormField<?>>> formFieldParamMap;

    @Setter
    private String relativeUrl;

    @Setter
    private String absoluteUrl;

    private MediaType contentType;
    private RequestBody body;
    private MultipartBody.Builder multipartBuilder;
    private FormBody.Builder formBuilder;

    RequestBuilder(String method, HttpUrl baseUrl, @Nullable String relativeUrl, @Nullable Headers headers, @Nullable MediaType contentType, boolean isFormData, boolean isX3WFormUrlEncoded) {
        this.baseUrl = baseUrl;
        this.method = method;
        this.relativeUrl = relativeUrl;
        this.requestBuilder = new Request.Builder();
        this.contentType = contentType;
        this.headersBuilder = Opt.ofNullable(headers).map(Headers::newBuilder).orElse(new Headers.Builder());
        // form-data 和 x-www-form-urlencoded 设置 body
        if (isX3WFormUrlEncoded) {
            this.formBuilder = new FormBody.Builder();
        } else if (isFormData) {
            this.multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        }
        // 临时记录各种参数
        this.pathParamMap = new LinkedTreeMap<>();
        this.queryParamMap = new LinkedTreeMap<>();
        this.formFieldParamMap = new LinkedTreeMap<>();
    }

    /**
     * 添加请求头（同名请求头会被覆盖）。
     * <p>
     * 名称为 {@code Content-Type} 时同步更新请求体类型。
     *
     * @param name  请求头名称
     * @param value 请求头值
     */
    public void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            contentType = MediaType.get(value);
        }
        headersBuilder.set(name, value);
    }

    /**
     * 批量追加请求头。
     *
     * @param headers 待追加的 OkHttp 请求头集合
     */
    public void addHeaders(Headers headers) {
        headersBuilder.addAll(headers);
    }

    /**
     * 添加路径参数（用于替换 URL 模板中的 {@code {name}} 占位符）。
     *
     * @param name    参数名
     * @param value   参数值
     * @param encoded 值是否已完成 URL 编码；未编码时按 UTF-8 编码
     */
    public void addPathParam(String name, String value, boolean encoded) {
        pathParamMap.put(name, dispatchEncode(value, encoded));
    }

    /**
     * 添加查询参数（同名参数可重复出现）。
     *
     * @param name  参数名
     * @param value 已编码为表单字段形式的参数值
     */
    public void addQueryParam(String name, FormField.ValueFormField value) {
        queryParamMap.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    /**
     * 添加表单字段（同名字段可重复出现）。
     *
     * @param name      字段名
     * @param formField 待编码的表单字段（值字段、文件、字节或流）
     */
    public void addFiled(String name, FormField<?> formField) {
        formFieldParamMap.computeIfAbsent(name, k -> new ArrayList<>()).add(formField);
    }

    /**
     * 设置请求体（@Body / @Binary 参数专用）。
     * <p>
     * 表单编码（form / multipart）方法存在表单字段时不生效。
     *
     * @param body OkHttp 请求体
     */
    public void body(RequestBody body) {
        this.body = body;
    }

    /**
     * 为请求附加类型化 tag（供拦截器读取）。
     *
     * @param cls   tag 类型
     * @param value tag 值，可为 {@code null}
     * @param <T>   tag 类型
     */
    public <T> void addTag(Class<T> cls, @Nullable T value) {
        requestBuilder.tag(cls, value);
    }

    /**
     * 结束收集并生成 OkHttp {@link Request.Builder}。
     * <p>
     * 拼接 URL（优先绝对 URL，其次 baseUrl + 相对路径）、写入查询参数、按表单编码类型
     * 编码表单字段（multipart 字节/文件/流或 urlencoded 键值对）、补齐空请求体与
     * {@code Content-Type}；方法名统一大写。
     *
     * @return 已填好 URL、请求头、方法与请求体的请求构建器
     */
    public Request.Builder get() {
        HttpUrl.Builder urlBuilder = urlBuilder();
        if (MapUtils.isNotEmpty(queryParamMap)) {
            // 带参数的url
            queryParamMap.forEach((name, values) -> values.forEach(val -> {
                if (val.isEncoded()) {
                    urlBuilder.addEncodedQueryParameter(name, Opt.ofNullable(val.getValue()).orElse(val.getDefaultValue()));
                } else {
                    urlBuilder.addQueryParameter(name, Opt.ofNullable(val.getValue()).orElse(val.getDefaultValue()));
                }
            }));
        }
        HttpUrl url = urlBuilder.build();

        if (MapUtils.isNotEmpty(formFieldParamMap)) {
            formFieldParamMap.forEach((name, values) -> values.stream().filter(Objects::nonNull).forEach(val -> {
                switch (val) {
                    case FormField.ValueFormField valueFormField -> {
                        if (null != formBuilder) {
                            if (valueFormField.isEncoded()) {
                                formBuilder.addEncoded(name, Opt.ofNullable(valueFormField.getValue()).orElse(valueFormField.getDefaultValue()));
                            } else {
                                formBuilder.add(name, Opt.ofNullable(valueFormField.getValue()).orElse(valueFormField.getDefaultValue()));
                            }
                        }
                        if (null != multipartBuilder) {
                            multipartBuilder.addFormDataPart(name, Opt.ofNullable(valueFormField.getValue()).orElse(valueFormField.getDefaultValue()));
                        }
                    }
                    case FormField.FileFormField fileFormField -> {
                        if (null != multipartBuilder && Objects.nonNull(fileFormField.getValue())) {
                            RequestBody fileBody = RequestBody.create(fileFormField.getValue(), MediaType.parse("application/octet-stream"));
                            multipartBuilder.addFormDataPart(name, fileFormField.getFilename(), fileBody);
                        }
                    }
                    case FormField.BytesFormField bytesFormField -> {
                        if (null != multipartBuilder && Objects.nonNull(bytesFormField.getValue())) {
                            RequestBody fileBody = RequestBody.create(bytesFormField.getValue(), MediaType.parse("application/octet-stream"));
                            multipartBuilder.addFormDataPart(name, bytesFormField.getFilename(), fileBody);
                        }
                    }
                    case FormField.InputStreamFormField inputStreamFormField -> {
                        if (null != multipartBuilder && Objects.nonNull(inputStreamFormField.getValue())) {
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
                                    while ((len = inputStreamFormField.getValue().read(buffer)) != -1) {
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
                            multipartBuilder.addFormDataPart(name, inputStreamFormField.getFilename(), streamBody);
                        }
                    }
                    default -> throw new IllegalArgumentException("Unsupported form field type: " + val.getClass());
                }
            }));
        }

        if (null == body) {
            if (null != formBuilder) {
                body = formBuilder.build();
            } else if (null != multipartBuilder) {
                body = multipartBuilder.build();
            } else if (HttpMethod.requiresRequestBody(method)) {
                // 如果强行有body，则设置个空body
                body = RequestBody.create(new byte[0], MediaType.parse("application/json"));
            }
        }

        if (null != contentType) {
            headersBuilder.set("Content-Type", contentType.toString());
        }
        return requestBuilder
            .url(url)
            .headers(headersBuilder.build())
            .method(method.toUpperCase(), body);
    }

    private HttpUrl.Builder urlBuilder() {
        HttpUrl.Builder urlBuilder;
        // 如果是绝对 url，直接使用 absolute url 构建 request
        if (StringUtils.hasText(absoluteUrl)) {
            // 处理 path 参数
            if (MapUtils.isNotEmpty(pathParamMap)) {
                // 存在 path 参数
                absoluteUrl = StringUtils.format(absoluteUrl, pathParamMap);
            }
            urlBuilder = HttpUrl.get(absoluteUrl).newBuilder();
        } else {
            urlBuilder = baseUrl.newBuilder();
            // 处理 path 参数
            if (MapUtils.isNotEmpty(pathParamMap)) {
                // 存在 path 参数
                relativeUrl = StringUtils.format(relativeUrl, pathParamMap);
            }
            // 设置 path
            // 去除 path 前的 /
            if (relativeUrl.startsWith("/")) {
                relativeUrl = relativeUrl.replaceAll("^/+", "");
            }
            urlBuilder.addPathSegments(relativeUrl);
        }
        return urlBuilder;
    }

    private String dispatchEncode(String value, boolean encoded) {
        return encoded ? value : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
