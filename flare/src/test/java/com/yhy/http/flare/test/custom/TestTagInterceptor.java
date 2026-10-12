package com.yhy.http.flare.test.custom;

import com.yhy.http.flare.model.Invocation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 测试用拦截器，为请求附加 {@code Tag} 请求头并记录 {@link Invocation} 标签与响应 Content-Type。
 * <p>
 * 在 {@code MockGetApi} / {@code MockPostApi} 的方法上通过 {@code @Interceptor} 声明，
 * 用于验证方法级拦截器与请求标签在代理调用链中的传递。
 * <p>
 * Created on 2025-09-15 17:47
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class TestTagInterceptor implements Interceptor {

    @Override
    public @NotNull Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        log.info("tag: {}", chain.request().tag(Invocation.class));
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        builder.header("Tag", "TestTagInterceptor");
        Response proceed = chain.proceed(builder.build());
        log.info("response header: {}", proceed.header("Content-Type"));
        return proceed;
    }
}
