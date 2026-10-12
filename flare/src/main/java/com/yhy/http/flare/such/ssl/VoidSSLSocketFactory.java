package com.yhy.http.flare.such.ssl;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;

/**
 * 默认的 {@link SSLSocketFactory} 实现，用于禁用 SSL 证书链校验。
 * <p>
 * 内部以 TLS {@link SSLContext} 初始化一个不校验证书链的委托工厂
 * （TrustManager 为 {@link VoidSSLX509TrustManager}），全部方法直接转发给该委托。
 * <b>安全警告：</b>该实现关闭证书链校验，会使 TLS 防中间人能力失效，
 * 仅适用于用户明确接受该风险的受控环境（如内部测试、自签名证书服务），
 * 不应在不可信网络或生产环境中使用。
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
     * 默认启用的密码套件。
     *
     * @return 委托工厂的默认密码套件列表
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return DELEGATE.getDefaultCipherSuites();
    }

    /**
     * 支持的密码套件。
     *
     * @return 委托工厂支持的密码套件列表
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return DELEGATE.getSupportedCipherSuites();
    }

    /**
     * 在已有 Socket 上分层创建 SSL Socket。
     *
     * @param s         底层 Socket
     * @param host      目标主机名
     * @param port      目标端口
     * @param autoClose 是否随本 Socket 关闭底层 Socket
     * @return 委托工厂创建的 SSL Socket
     * @throws IOException 创建失败时抛出
     */
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return DELEGATE.createSocket(s, host, port, autoClose);
    }

    /**
     * 创建连接指定主机端口的 SSL Socket。
     *
     * @param host 目标主机名
     * @param port 目标端口
     * @return 委托工厂创建的 SSL Socket
     * @throws IOException 创建失败时抛出
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return DELEGATE.createSocket(host, port);
    }

    /**
     * 创建绑定本地地址后连接远端的 SSL Socket。
     *
     * @param host      目标主机名
     * @param port      目标端口
     * @param localHost 本地绑定地址
     * @param localPort 本地绑定端口
     * @return 委托工厂创建的 SSL Socket
     * @throws IOException 创建失败时抛出
     */
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return DELEGATE.createSocket(host, port, localHost, localPort);
    }

    /**
     * 创建连接指定地址端口的 SSL Socket。
     *
     * @param host 目标地址
     * @param port 目标端口
     * @return 委托工厂创建的 SSL Socket
     * @throws IOException 创建失败时抛出
     */
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return DELEGATE.createSocket(host, port);
    }

    /**
     * 创建绑定本地地址后连接远端的 SSL Socket。
     *
     * @param address      目标地址
     * @param port         目标端口
     * @param localAddress 本地绑定地址
     * @param localPort    本地绑定端口
     * @return 委托工厂创建的 SSL Socket
     * @throws IOException 创建失败时抛出
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
