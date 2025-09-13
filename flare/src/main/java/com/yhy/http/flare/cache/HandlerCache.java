package com.yhy.http.flare.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yhy.http.flare.http.HttpHandler;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 *
 * <p>
 * Created on 2025-09-11 06:04
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class HandlerCache {
    private static final Cache<@NotNull Method, HttpHandler<?>> CACHE = Caffeine.newBuilder().build();

    public static void put(Method method, HttpHandler<?> handler) {
        CACHE.put(method, handler);
    }

    public static HttpHandler<?> get(Method method) {
        return CACHE.getIfPresent(method);
    }
}
