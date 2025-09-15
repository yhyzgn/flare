package com.yhy.http.flare.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL 工具类
 * <p>
 * Created on 2025-09-15 15:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class UrlUtils {

    private UrlUtils() {
        throw new UnsupportedOperationException("Can not instantiate utils class");
    }

    public static String encode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String decode(String url) {
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }
}
