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
public class VoidSSLSocketFactory extends SSLSocketFactory {

    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port) {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
        return null;
    }
}
