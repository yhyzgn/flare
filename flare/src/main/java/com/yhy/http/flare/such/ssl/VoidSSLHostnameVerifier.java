package com.yhy.http.flare.such.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 默认的 HostnameVerifier 实现，用于禁用 SSL 连接认证
 * <p>
 * Created on 2025-09-11 09:10
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return false;
    }
}
