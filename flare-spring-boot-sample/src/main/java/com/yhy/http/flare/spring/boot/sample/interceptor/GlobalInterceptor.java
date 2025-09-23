package com.yhy.http.flare.spring.boot.sample.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 * <p>
 * Created on 2025-09-18 14:50
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class GlobalInterceptor implements Interceptor {

    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        log.info("GlobalInterceptor executing");
        log.info("当前线程：{}，是否是虚拟线程：{}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return chain.proceed(chain.request());
    }
}
