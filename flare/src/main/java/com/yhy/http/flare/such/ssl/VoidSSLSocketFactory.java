package com.yhy.http.flare.such.ssl;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * 默认的 SSLSocketFactory 实现，用于禁用 SSL 证书链校验。
 * <p>该实现不应在不可信网络环境中使用。</p>
 * <p>
 * Created on 2025-09-11 09:12
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLSocketFactory extends SSLSocketFactory {
    private static final SSLSocketFactory DELEGATE = createDelegate();

    private static SSLSocketFactory createDelegate() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new VoidSSLX509TrustManager[]{new VoidSSLX509TrustManager()}, null);
            return context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 创建一个忽略证书链校验的 SSL Socket 工厂。
     */
    public VoidSSLSocketFactory() {
    }

    /**
     * get Default Cipher Suites。
     *
     * @return 处理结果
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return DELEGATE.getDefaultCipherSuites();
    }

    /**
     * get Supported Cipher Suites。
     *
     * @return 处理结果
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return DELEGATE.getSupportedCipherSuites();
    }

    /**
     * create Socket。
     *
     * @param s 值
     * @param host 字符串
     * @param port 值
     * @param autoClose 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return DELEGATE.createSocket(s, host, port, autoClose);
    }

    /**
     * create Socket。
     *
     * @param host 字符串
     * @param port 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return DELEGATE.createSocket(host, port);
    }

    /**
     * create Socket。
     *
     * @param host 字符串
     * @param port 值
     * @param localHost 值
     * @param localPort 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return DELEGATE.createSocket(host, port, localHost, localPort);
    }

    /**
     * create Socket。
     *
     * @param host 值
     * @param port 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return DELEGATE.createSocket(host, port);
    }

    /**
     * create Socket。
     *
     * @param address 值
     * @param port 值
     * @param localAddress 值
     * @param localPort 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return DELEGATE.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket() throws IOException {
        return DELEGATE.createSocket();
    }
}
