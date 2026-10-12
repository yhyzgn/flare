package com.yhy.http.flare.such.ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 默认的 {@link X509ExtendedTrustManager} 实现，用于禁用 SSL 证书链校验。
 * <p>
 * 所有校验方法均为空实现，即信任任意证书链；{@link #getAcceptedIssuers()} 返回空数组。
 * <b>安全警告：</b>该实现关闭证书链校验，会使 TLS 防中间人能力失效，
 * 仅适用于用户明确接受该风险的受控环境（如内部测试、自签名证书服务），
 * 不应在不可信网络或生产环境中使用。
 * <p>
 * Created on 2025-09-11 09:15
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLX509TrustManager extends X509ExtendedTrustManager {
    /**
     * 校验客户端证书链（基于 {@link Socket}）。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @param socket   连接 Socket
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    }

    /**
     * 校验服务端证书链（基于 {@link Socket}）。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @param socket   连接 Socket
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    }

    /**
     * 校验客户端证书链（基于 {@link SSLEngine}）。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @param engine   SSLEngine
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
    }

    /**
     * 校验服务端证书链（基于 {@link SSLEngine}）。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @param engine   SSLEngine
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
    }

    /**
     * 校验客户端证书链。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    /**
     * 校验服务端证书链。
     * <p>
     * 空实现，不做任何校验。
     *
     * @param chain    对端证书链
     * @param authType 使用的认证算法
     * @throws CertificateException 本实现不会抛出
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    /**
     * 受信任的证书颁发者列表。
     *
     * @return 始终返回空数组，即不限制颁发者
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
