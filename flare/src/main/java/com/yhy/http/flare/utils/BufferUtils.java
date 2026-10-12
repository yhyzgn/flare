package com.yhy.http.flare.utils;

import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.IOException;

/**
 * 响应体缓冲工具类，将 {@link ResponseBody} 全量读入内存以便重复读取。
 * <p>
 * 典型场景为错误响应体复现：OkHttp 的响应体只能读取一次，缓冲后可多次消费。
 * <p>
 * Created on 2025-09-11 06:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BufferUtils {

    private BufferUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 将响应体全量读入内存缓冲区，返回可重复读取的新 {@link ResponseBody}。
     * <p>
     * 调用后原响应体的数据将被耗尽，应使用返回值进行后续操作。
     *
     * @param rawBody 原始响应体，不应为 {@code null}
     * @return 与原响应体具有相同 contentType 和 contentLength 的缓冲副本
     * @throws IOException 读取响应体底层流时发生 I/O 错误
     */
    public static ResponseBody buffer(ResponseBody rawBody) throws IOException {
        Buffer buffer = new Buffer();
        rawBody.source().readAll(buffer);
        return ResponseBody.create(buffer, rawBody.contentType(), rawBody.contentLength());
    }
}
