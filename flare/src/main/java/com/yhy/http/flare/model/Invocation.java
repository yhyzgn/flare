package com.yhy.http.flare.model;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * invocation
 * <p>
 * Created on 2025-09-10 16:42
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record Invocation(Method method, List<?> arguments) {
    /**
     * 创建 Invocation 实例。
     *
     * @param method 方法
     * @param arguments 列表
     */
    public Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    /**
     * 转换为字符串。
     *
     * @return 处理结果
     */
    @Override
    public @NotNull String toString() {
        return String.format("%s.%s() %s", method.getDeclaringClass().getName(), method.getName(), arguments);
    }

    /**
     * 创建对象。
     *
     * @param method 方法
     * @param arguments 列表
     * @return 处理结果
     */
    public static Invocation of(Method method, List<?> arguments) {
        Objects.requireNonNull(method, "method can not be null.");
        Objects.requireNonNull(arguments, "arguments can not be null.");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }
}
