package com.yhy.http.flare.spring.boot.sample.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * POST 请求拦截器，在 {@code @Flare} 的 {@code PostRemote} 接口上声明，演示接口级拦截器用法。
 * <p>
 * Created on 2025-09-18 14:51
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class PostInterceptor implements Interceptor {

    /**
     * 拦截请求。
     *
     * @param chain OkHttp 拦截器链
     * @return 拦截后的响应
     * @throws IOException IO 异常
     */
    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        log.info("PostInterceptor executing");
        return chain.proceed(chain.request());
    }
}
