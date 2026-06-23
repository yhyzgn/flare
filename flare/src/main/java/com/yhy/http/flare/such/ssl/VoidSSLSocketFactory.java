package com.yhy.http.flare.such.ssl;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 默认的 SSLSocketFactory 实现，用于禁用 SSL 连接认证
 * <p>
 * Created on 2025-09-11 09:12
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLSocketFactory extends SSLSocketFactory {

    /**
     * get Default Cipher Suites。
     *
     * @return 处理结果
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    /**
     * get Supported Cipher Suites。
     *
     * @return 处理结果
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
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
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) {
        return null;
    }

    /**
     * create Socket。
     *
     * @param host 字符串
     * @param port 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(String host, int port) {
        return null;
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
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
        return null;
    }

    /**
     * create Socket。
     *
     * @param host 值
     * @param port 值
     * @return 处理结果
     */
    @Override
    public Socket createSocket(InetAddress host, int port) {
        return null;
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
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
        return null;
    }
}
