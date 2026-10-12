package com.yhy.http.flare.such.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 默认的 HostnameVerifier 实现，用于禁用 SSL 主机名校验。
 * <p>该实现不应在不可信网络环境中使用。</p>
 * <p>
 * Created on 2025-09-11 09:10
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLHostnameVerifier implements HostnameVerifier {

    /**
     * verify。
     *
     * @param hostname 字符串
     * @param session 值
     * @return 处理结果
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
