package com.yhy.http.flare.http.request;

import com.google.gson.internal.LinkedTreeMap;
import com.yhy.http.flare.model.FormField;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.StringUtils;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
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

    private final Map<String, String> pathParamMap;
    private final Map<String, List<String>> queryParamMap;
    private final Map<String, List<FormField<?>>> formFieldParamMap;

    private String relativeUrl;
    private MediaType contentType;
    private RequestBody body;
    private MultipartBody.Builder multipartBuilder;
    private FormBody.Builder formBuilder;

    RequestBuilder(String method, HttpUrl baseUrl, @Nullable String relativeUrl, @Nullable Headers headers, @Nullable MediaType contentType, boolean isFormData, boolean isX3WFormUrlEncoded) {
        this.baseUrl = baseUrl;
        this.method = method;
        this.relativeUrl = relativeUrl;
        this.requestBuilder = new Request.Builder();
        this.contentType = Opt.ofNullable(contentType).orElse(MediaType.parse("application/json; charset=utf-8"));
        this.headersBuilder = Opt.ofNullable(headers).map(Headers::newBuilder).orElse(new Headers.Builder());
        // form-data 和 x-www-form-urlencoded 设置 body
        if (isX3WFormUrlEncoded) {
            this.formBuilder = new FormBody.Builder();
        } else if (isFormData) {
            this.multipartBuilder = new MultipartBody.Builder();
            this.multipartBuilder.setType(MultipartBody.FORM);
        }
        // 临时记录各种参数
        this.pathParamMap = new LinkedTreeMap<>();
        this.queryParamMap = new LinkedTreeMap<>();
        this.formFieldParamMap = new LinkedTreeMap<>();
    }

    public void setRelativeUrl(Object url) {
        this.relativeUrl = url.toString();
    }

    public void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            contentType = MediaType.get(value);
        }
        headersBuilder.set(name, value);
    }

    public void addHeaders(Headers headers) {
        headersBuilder.addAll(headers);
    }

    public void addPathParam(String name, String value, boolean encoded) {
        pathParamMap.put(name, dispatchEncode(value, encoded));
    }

    public void addQueryParam(String name, String value, boolean encoded) {
        queryParamMap.computeIfAbsent(name, k -> new ArrayList<>()).add(dispatchEncode(value, encoded));
    }

    public void addFiled(String name, FormField<?> formField) {
        formFieldParamMap.computeIfAbsent(name, k -> new ArrayList<>()).add(formField);
    }

    public void addPart(Headers headers, RequestBody body) {
        multipartBuilder.addPart(headers, body);
    }

    public void addPart(MultipartBody.Part part) {
        multipartBuilder.addPart(part);
    }

    public void body(RequestBody body) {
        this.body = body;
    }

    public <T> void addTag(Class<T> cls, @Nullable T value) {
        requestBuilder.tag(cls, value);
    }

    public Request.Builder get() {
        HttpUrl.Builder urlBuilder = baseUrl.newBuilder();

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

        if (MapUtils.isNotEmpty(queryParamMap)) {
            // 带参数的url
            queryParamMap.forEach((name, values) -> values.forEach(val -> urlBuilder.addEncodedQueryParameter(name, val)));
        }
        HttpUrl url = urlBuilder.build();

        if (MapUtils.isNotEmpty(formFieldParamMap)) {
            formFieldParamMap.forEach((name, values) -> values.forEach(val -> {
                if (val instanceof FormField.ValueFormField valueFormField) {
                    if (null != formBuilder) {
                        formBuilder.addEncoded(name, valueFormField.getValue());
                    }
                    if (null != multipartBuilder) {
                        multipartBuilder.addFormDataPart(name, valueFormField.getValue());
                    }
                } else if (val instanceof FormField.FileFormField fileFormField) {
                    if (null != multipartBuilder) {
                        File file = fileFormField.getValue();
                        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
                        multipartBuilder.addFormDataPart(name, file.getName(), fileBody);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported form field type: " + val.getClass());
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
        body = Opt.ofNullable(body).map(b -> new ContentTypeOverridingRequestBody(b, contentType)).orElse(null);

        if (null != contentType) {
            headersBuilder.set("Content-Type", contentType.toString());
        }
        return requestBuilder
                .url(url)
                .headers(headersBuilder.build())
                .method(method.toUpperCase(), body);
    }

    private String dispatchEncode(String value, boolean encoded) {
        return encoded ? value : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static class ContentTypeOverridingRequestBody extends RequestBody {
        private final RequestBody delegate;
        private final MediaType contentType;

        ContentTypeOverridingRequestBody(RequestBody delegate, MediaType contentType) {
            this.delegate = delegate;
            this.contentType = contentType;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() throws IOException {
            return delegate.contentLength();
        }

        @Override
        public void writeTo(@NotNull BufferedSink sink) throws IOException {
            delegate.writeTo(sink);
        }
    }
}
