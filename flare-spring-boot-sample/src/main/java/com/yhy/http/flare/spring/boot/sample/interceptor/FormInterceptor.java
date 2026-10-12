package com.yhy.http.flare.spring.boot.sample.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 表单请求拦截器，在 {@code @PostRemote.form} 方法上声明，演示方法级拦截器用法。
 * <p>
 * Created on 2025-09-18 14:57
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class FormInterceptor implements Interceptor {

    /**
     * 拦截请求。
     *
     * @param chain OkHttp 拦截器链
     * @return 拦截后的响应
     * @throws IOException IO 异常
     */
    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        log.info("FormInterceptor executing");
        return chain.proceed(chain.request());
    }
}
