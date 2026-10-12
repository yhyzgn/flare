package com.yhy.http.flare.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * URL 编解码工具类。
 * <p>
 * 基于 {@code application/x-www-form-urlencoded} 规则的 UTF-8 编码与解码。
 * <p>
 * Created on 2025-09-15 15:48
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class UrlUtils {

    /**
     * 防止工具类被实例化。
     */
    private UrlUtils() {
        throw new UnsupportedOperationException("Can not instantiate utils class");
    }

    /**
     * 对字符串进行 UTF-8 URL 编码。
     * <p>
     * 采用 {@link URLEncoder} 的表单编码规则（空格转为 {@code '+'}）。
     *
     * @param url 待编码字符串
     * @return 编码后的字符串
     */
    public static String encode(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    /**
     * 对 UTF-8 URL 编码的字符串进行解码。
     * <p>
     * 采用 {@link URLDecoder} 的表单解码规则（{@code '+'} 转为空格）。
     *
     * @param url 待解码字符串
     * @return 解码后的字符串
     */
    public static String decode(String url) {
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }
}
