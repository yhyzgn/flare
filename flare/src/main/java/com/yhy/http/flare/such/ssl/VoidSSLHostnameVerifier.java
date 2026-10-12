package com.yhy.http.flare.such.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 默认的 {@link HostnameVerifier} 实现，用于禁用 SSL 主机名校验。
 * <p>
 * {@link #verify} 恒返回 {@code true}，即接受任意主机名。
 * <b>安全警告：</b>该实现关闭主机名校验，会使 TLS 防中间人能力失效，
 * 仅适用于用户明确接受该风险的受控环境（如内部测试、自签名证书服务），
 * 不应在不可信网络或生产环境中使用。
 * <p>
 * Created on 2025-09-11 09:10
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public final class VoidSSLHostnameVerifier implements HostnameVerifier {

    /**
     * 校验主机名。
     * <p>
     * 恒返回 {@code true}，不做任何实际校验。
     *
     * @param hostname 对端主机名
     * @param session  当前 SSL 会话
     * @return 始终返回 {@code true}
     */
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
