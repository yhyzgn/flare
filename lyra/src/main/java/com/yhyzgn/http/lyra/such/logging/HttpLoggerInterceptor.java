package com.yhyzgn.http.lyra.such.logging;

import com.yhyzgn.http.lyra.model.Invocation;
import com.yhyzgn.http.lyra.such.SystemClock;
import com.yhyzgn.http.lyra.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 默认的日志打印器
 * <p>
 * Created on 2025-09-10 16:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class HttpLoggerInterceptor implements Interceptor {
    private final static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E", Locale.getDefault());

    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        // 开始时间
        long start = SystemClock.now();

        // 获取http信息
        Request request = chain.request();
        HttpUrl url = request.url();
        String method = request.method();
        Headers headers = request.headers();
        Set<String> names = headers.names();
        Iterator<String> it = names.iterator();

        String requestContentType = request.header("Content-Type");

        LogLines lines = LogLines.start("-- Request starting at {} --", FORMAT.format(new Date())).line("url : " + url).line("method : " + method);

        lines.empty().line("-- Request Header --");
        while (it.hasNext()) {
            String name = it.next();
            String value = headers.get(name);
            lines.line(name + " : " + value);
        }

        RequestBody reqBody = request.body();
        if (null != reqBody) {
            lines.empty().line("-- Request Body --");
            lines.line(requestBodyToString(requestContentType, reqBody).replace(System.lineSeparator(), System.lineSeparator() + "│ "));
        }

        Response wrapResponse;
        try (Response response = chain.proceed(request)) {
            String responseContentTypeString = response.header("Content-Type");
            headers = response.headers();
            names = headers.names();
            it = names.iterator();
            lines.empty().line("-- Response Header --");
            lines.line("Status : " + response.code());
            while (it.hasNext()) {
                String name = it.next();
                String value = headers.get(name);
                lines.line(name + " : " + value);
            }

            ResponseBody resBody = response.body();
            MediaType responseContentType = resBody.contentType();
            if (null == responseContentTypeString || responseContentTypeString.isEmpty()) {
                responseContentTypeString = Optional.ofNullable(responseContentType).map(MediaType::toString).orElse("");
            }
            String encoding = Optional.ofNullable(response.header("Content-Encoding")).orElse("");
            BufferedSource source;
            if ("gzip".equals(encoding)) {
                source = Okio.buffer(new GzipSource(resBody.source()));
            } else {
                source = resBody.source();
            }

            lines.empty().line("-- Response Body --");
            byte[] bytes = source.readByteArray();

            String content = responseToString(responseContentTypeString, bytes);
            lines.line(content.replace(System.lineSeparator(), System.lineSeparator() + "│ "));

            // 重组 Response
            // 须移除 Content-Encoding，因为当前 body 已解压
            wrapResponse = response.newBuilder().removeHeader("Content-Encoding").body(ResponseBody.create(bytes, responseContentType)).build();
        }

        // 结束时间
        long end = SystemClock.now();
        lines.empty().line("-- Http Lyra Finished. Used {} millis. --", end - start);

        // tag
        Invocation tag = request.tag(Invocation.class);
        log(null != tag ? tag : this, lines);

        return wrapResponse;
    }

    private void log(Object tag, LogLines lines) {
        StringBuffer sb = new StringBuffer(System.lineSeparator());
        sb
                .append("┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────")
                .append(System.lineSeparator())
                .append("│ ").append(tag.toString())
                .append(System.lineSeparator())
                .append("├────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────")
                .append(System.lineSeparator());
        lines.lines().stream()
                .filter(Objects::nonNull)
                .peek(item -> item.msg = Optional.ofNullable(item.msg).orElse(""))
                .forEach(item -> sb.append("│ ").append(null != item.args && item.args.length > 0 ? StringUtils.format(item.msg, item.args) : item.msg).append(System.lineSeparator()));
        sb.append("└────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        log.info(sb.toString());
    }

    private String requestBodyToString(String contentType, RequestBody body) throws IOException {
        // 如果是二进制的 body，则直接返回 (binary body is not supported) 字符串
        if (null == contentType || contentType.isEmpty()) {
            contentType = Optional.ofNullable(body.contentType()).map(MediaType::toString).orElse("");
        }
        if (contentType.startsWith("application/octet-stream")) {
            return "(binary body is not supported)";
        }

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    private String responseToString(String contentType, byte[] bytes) {
        if (contentType.startsWith("application/octet-stream")) {
            return "(binary body is not supported)";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("SameParameterValue")
    private static class LogLines {
        private static final List<LogLine> lines = new ArrayList<>();

        static LogLines start(String msg, Object... args) {
            lines.add(new LogLine(msg, args));
            return new LogLines();
        }

        LogLines line(String msg, Object... args) {
            lines.add(new LogLine(msg, args));
            return this;
        }

        LogLines empty() {
            lines.add(new LogLine(""));
            return this;
        }

        List<LogLine> lines() {
            List<LogLine> temp = new ArrayList<>(lines);
            lines.clear();
            return temp;
        }
    }

    private static class LogLine {
        String msg;
        Object[] args;

        private LogLine(String msg, Object... args) {
            this.msg = msg;
            this.args = null == args || args.length == 0 ? null : args;
        }
    }
}
