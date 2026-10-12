package com.yhy.http.flare.model;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 代理方法调用信息。
 * <p>
 * 记录被调用的接口方法与实参列表，作为请求 tag 附加到 OkHttp 请求上，便于拦截器追踪调用来源。
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
     * @param method    被调用的接口方法
     * @param arguments 实参列表，保存为不可变列表
     */
    public Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    /**
     * 输出形如 {@code 类全名.方法名 [实参]} 的调用描述。
     *
     * @return 可读的调用描述字符串
     */
    @Override
    public @NotNull String toString() {
        return String.format("%s.%s() %s", method.getDeclaringClass().getName(), method.getName(), arguments);
    }

    /**
     * 静态构造器，对实参做防御性复制。
     *
     * @param method    被调用的接口方法，不能为 {@code null}
     * @param arguments 实参列表，不能为 {@code null}
     * @return Invocation 实例
     */
    public static Invocation of(Method method, List<?> arguments) {
        Objects.requireNonNull(method, "method can not be null.");
        Objects.requireNonNull(arguments, "arguments can not be null.");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }
}
