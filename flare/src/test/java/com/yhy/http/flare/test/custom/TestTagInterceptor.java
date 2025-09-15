package com.yhy.http.flare.test.custom;

import com.yhy.http.flare.model.Invocation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 *
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
