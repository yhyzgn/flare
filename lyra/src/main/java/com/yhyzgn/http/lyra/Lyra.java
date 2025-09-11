package com.yhyzgn.http.lyra;

import com.yhyzgn.http.lyra.annotation.Header;
import com.yhyzgn.http.lyra.call.CallAdapter;
import com.yhyzgn.http.lyra.convert.Converter;
import com.yhyzgn.http.lyra.such.adapter.GuavaCallAdapter;
import com.yhyzgn.http.lyra.such.convert.JacksonConverter;
import com.yhyzgn.http.lyra.such.logging.HttpLoggerInterceptor;
import com.yhyzgn.http.lyra.utils.Assert;
import com.yhyzgn.http.lyra.utils.Opt;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * 一个 HTTP 请求客户端
 * <p>
 * Created on 2025-09-10 14:05
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class Lyra {
    private final HttpUrl baseUrl;

    private Lyra(Builder builder) {
        this.baseUrl = builder.baseUrl;
    }

    public static class Builder {
        private final List<Interceptor> netInterceptors = new ArrayList<>();
        private final List<Interceptor> interceptors = new ArrayList<>();
        private final Map<String, String> headers = new HashMap<>();
        private final List<Header.Dynamic> dynamicHeaders = new ArrayList<>();

        private HttpUrl baseUrl;
        private Dispatcher dispatcher;
        private OkHttpClient.Builder client;
        private Boolean logEnabled;
        private Interceptor loggerInterceptor;
        private CallAdapter.Factory callAdapterFactory;
        private Converter.Factory converterFactory;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager sslTrustManager;
        private HostnameVerifier sslHostnameVerifier;
        private Duration timeout;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder netInterceptor(Interceptor interceptor) {
            this.netInterceptors.add(interceptor);
            return this;
        }

        /**
         * 配置分发器
         *
         * @param dispatcher 分发器
         * @return builder
         */
        public Builder dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        /**
         * 静态请求头
         *
         * @param name  名称
         * @param value 静态值
         * @return builder
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * 动态请求头
         *
         * @param header 动态支持
         * @return builder
         */
        public Builder header(Header.Dynamic header) {
            this.dynamicHeaders.add(header);
            return this;
        }

        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.callAdapterFactory = factory;
            return this;
        }

        public Builder converterFactory(Converter.Factory factory) {
            this.converterFactory = factory;
            return this;
        }

        public Builder https(SSLSocketFactory factory, X509TrustManager manager, HostnameVerifier verifier) {
            this.sslSocketFactory = factory;
            this.sslTrustManager = manager;
            this.sslHostnameVerifier = verifier;
            return this;
        }

        public Builder client(OkHttpClient.Builder client) {
            this.client = client;
            return this;
        }

        public Builder timeout(long millis) {
            this.timeout = Duration.ofMillis(millis);
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder logEnabled(boolean enabled) {
            this.logEnabled = enabled;
            return this;
        }

        public Builder loggerInterceptor(Interceptor interceptor) {
            this.loggerInterceptor = interceptor;
            return this;
        }

        public Lyra build() {
            Assert.notNull(baseUrl, "baseUrl cannot be null");

            callAdapterFactory = Opt.ofNullable(callAdapterFactory).orElse(new GuavaCallAdapter());
            converterFactory = Opt.ofNullable(converterFactory).orElse(new JacksonConverter());

            client = Opt.ofNullable(client).orElse(new OkHttpClient.Builder());

            // 配置分发器
            client.dispatcher(Opt.ofNullable(dispatcher).orElse(virtualDispatcher()));

            // 配置 ssl
            if (null != sslSocketFactory && null != sslTrustManager && null != sslHostnameVerifier) {
                client.sslSocketFactory(sslSocketFactory, sslTrustManager).hostnameVerifier(sslHostnameVerifier);
            }

            if (Objects.equals(logEnabled, true)) {
                // 配置日志拦截器
                netInterceptors.add(Opt.ofNullable(loggerInterceptor).orElse(new HttpLoggerInterceptor()));
            }

            // 配置全局拦截器
            netInterceptors.forEach(client::addNetworkInterceptor);
            interceptors.forEach(client::addInterceptor);

            // 配置超时
            Opt.ofNullable(timeout).ifValid(t -> client.connectTimeout(t).callTimeout(t).readTimeout(t).writeTimeout(t));

            // 创建 Lyra 实例
            return new Lyra(this);
        }

        /**
         * 创建虚拟线程池分发器
         *
         * @return 分发器
         */
        private static Dispatcher virtualDispatcher() {
            return new Dispatcher(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("lyra-virtual-thread-", 0).factory()));
        }
    }
}
