package com.yhyzgn.http.flare.model;

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
    public Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s.%s() %s", method.getDeclaringClass().getName(), method.getName(), arguments);
    }

    public static Invocation of(Method method, List<?> arguments) {
        Objects.requireNonNull(method, "method can not be null.");
        Objects.requireNonNull(arguments, "arguments can not be null.");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }
}
