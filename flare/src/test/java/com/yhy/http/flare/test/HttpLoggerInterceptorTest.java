package com.yhy.http.flare.test;

import com.yhy.http.flare.such.interceptor.HttpLoggerInterceptor;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HttpLoggerInterceptor 测试类。
 *
 * @author Neo
 * @version 1.0.0
 * @since 2.0.0
 */
public class HttpLoggerInterceptorTest {

    /**
     * 普通 x-www-form-urlencoded 表单体应允许日志渲染。
     *
     * @throws Exception 调用异常
     */
    @Test
    public void formBodyCanBeRendered() throws Exception {
        RequestBody body = new FormBody.Builder()
                .add("name", "Neo")
                .add("age", "18")
                .build();

        String text = render(body);

        assertEquals("name=Neo&age=18", text);
    }

    /**
     * 只有普通字段的 multipart/form-data 表单体应允许日志渲染。
     *
     * @throws Exception 调用异常
     */
    @Test
    public void repeatableMultipartFormBodyCanBeRendered() throws Exception {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user", "Neo")
                .build();

        String text = render(body);

        assertTrue(text.contains("form-data; name=\"user\""));
        assertTrue(text.contains("Neo"));
    }

    /**
     * 包含 InputStream 的 multipart/form-data 表单体只能写入一次，日志不应提前消费流。
     *
     * @throws Exception 调用异常
     */
    @Test
    public void oneShotMultipartFormBodyCannotBeRendered() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("flare".getBytes(StandardCharsets.UTF_8));
        RequestBody streamBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }

            @Override
            public void writeTo(@NotNull BufferedSink sink) throws IOException {
                sink.write(inputStream.readAllBytes());
            }

            @Override
            public boolean isOneShot() {
                return true;
            }
        };
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "input-stream.webp", streamBody)
                .build();

        String text = render(body);

        assertEquals("(one-shot body is not supported)", text);
        assertEquals(5, inputStream.available());
    }

    private String render(RequestBody body) throws Exception {
        Method method = HttpLoggerInterceptor.class.getDeclaredMethod("requestBodyToString", String.class, RequestBody.class);
        method.setAccessible(true);
        return (String) method.invoke(new HttpLoggerInterceptor(), null, body);
    }
}
