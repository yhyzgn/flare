package com.yhyzgn.http.flare.utils;

import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.IOException;

/**
 *
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

    public static ResponseBody buffer(ResponseBody rawBody) throws IOException {
        Buffer buffer = new Buffer();
        rawBody.source().readAll(buffer);
        return ResponseBody.create(buffer, rawBody.contentType(), rawBody.contentLength());
    }
}
