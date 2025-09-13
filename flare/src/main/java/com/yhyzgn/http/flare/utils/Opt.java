package com.yhyzgn.http.flare.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Opt 类，用于封装 Optional 类，提供更方便的 API
 *
 * @param <T> 泛型类型
 */
@SuppressWarnings({"UnusedReturnValue", "DeprecatedIsStillUsed", "unused", "ClassCanBeRecord"})
public final class Opt<T> {
    private static final Opt<?> EMPTY = new Opt<>(null);

    private final T value;

    /**
     * 私有构造方法，确保通过静态方法创建实例
     *
     * @param value 非空 Optional 实例
     */
    private Opt(T value) {
        this.value = value;
    }

    /**
     * 静态工厂方法：创建非空值实例
     *
     * @param value 非空值
     * @param <T>   泛型类型
     * @return Opt 实例
     */
    public static <T> Opt<T> of(T value) {
        return new Opt<>(value);
    }

    /**
     * 静态工厂方法：创建可能为空值实例
     *
     * @param value 可能为空值
     * @param <T>   泛型类型
     * @return Opt 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> ofNullable(T value) {
        return value == null ? (Opt<T>) EMPTY : new Opt<>(value);
    }

    /**
     * 静态工厂方法：创建空值实例
     *
     * @param <T> 泛型类型
     * @return Opt 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> empty() {
        return (Opt<T>) EMPTY;
    }

    /**
     * 检查值是否有效
     *
     * @return true 值有效，false 值无效
     */
    public boolean isValid() {
        if (null == value) {
            return false; // null 为无效
        }
        if (value instanceof String str) {
            return !str.isEmpty() && !str.trim().isEmpty(); // 非空且非仅空白
        } else if (value instanceof Number num) {
            return num.doubleValue() > 0; // 大于 0
        } else if (value instanceof Collection<?> collection) {
            return !collection.isEmpty();
        } else if (value instanceof Map<?, ?> map) {
            return !map.isEmpty();
        } else if (value.getClass().isArray()) {
            // 支持对象数组
            switch (value) {
                case Object[] array -> {
                    return array.length > 0;
                }
                // 支持基本类型数组
                case int[] array -> {
                    return array.length > 0;
                }
                case double[] array -> {
                    return array.length > 0;
                }
                case long[] array -> {
                    return array.length > 0;
                }
                case float[] array -> {
                    return array.length > 0;
                }
                case boolean[] array -> {
                    return array.length > 0;
                }
                case byte[] array -> {
                    return array.length > 0;
                }
                case char[] array -> {
                    return array.length > 0;
                }
                case short[] array -> {
                    return array.length > 0;
                }
                default -> {
                }
            }
        }
        // 其他类型默认仅检查非 null
        return true;
    }

    /**
     * 检查值是否无效
     *
     * @return true 值无效，false 值有效
     */
    public boolean isInvalid() {
        return !isValid();
    }

    /**
     * 如果值有效，执行操作
     *
     * @param action 操作
     * @return Opt 实例
     */
    public Opt<T> ifValid(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (isValid()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * 如果值无效，执行操作
     *
     * @param action 操作
     * @return Opt 实例s
     */
    public Opt<T> ifInvalid(Runnable action) {
        Objects.requireNonNull(action);
        if (!isValid()) {
            action.run();
        }
        return this;
    }

    /**
     * 如果值存在，执行操作
     *
     * @param action 操作
     * @return Opt 实例
     * @deprecated 请使用 {@link #ifValid(Consumer)}
     */
    @Deprecated
    public Opt<T> ifPresent(Consumer<? super T> action) {
        return ifValid(action);
    }

    /**
     * 如果值不存在，执行操作，并返回自身以支持链式调用
     *
     * @param action 操作
     * @return Opt 实例
     * @deprecated 请使用 {@link #ifInvalid(Runnable)}
     */
    public Opt<T> ifAbsent(Runnable action) {
        return ifInvalid(action);
    }

    /**
     * 获取值，若不存在则抛出异常
     *
     * @return 值
     */
    public T get() {
        if (isValid()) {
            return value;
        }
        throw new NoSuchElementException("value is empty or invalid");
    }

    /**
     * 检查值是否存在
     *
     * @return true 值存在，false 值不存在
     * @deprecated 请使用 {@link #isValid()}
     */
    @Deprecated
    public boolean isPresent() {
        return isValid();
    }

    /**
     * 检查值是否不存在
     *
     * @return true 值不存在，false 值存在
     * @deprecated 请使用 {@link #isInvalid()}
     */
    @Deprecated
    public boolean isEmpty() {
        return isInvalid();
    }

    /**
     * 无效时执行操作，并返回自身以支持链式调用
     *
     * @param supplier 操作
     * @return Opt 实例
     */
    @SuppressWarnings("unchecked")
    public <U> Opt<U> or(Supplier<? extends Opt<? extends U>> supplier) {
        Objects.requireNonNull(supplier);
        if (isInvalid()) {
            Opt<U> opt = (Opt<U>) supplier.get();
            return Objects.requireNonNull(opt);
        }
        throw new ClassCastException("Opt<" + value.getClass().getName() + "> cannot be cast to Opt<" + supplier.getClass().getName() + ">");
    }

    /**
     * 无效时执行操作，并返回自身以支持链式调用
     *
     * @param runnable 操作
     * @return Opt 实例
     */
    public Opt<T> or(Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (isInvalid()) {
            runnable.run();
        }
        return this;
    }

    /**
     * 如果值不存在，返回默认值
     *
     * @param other 默认值
     * @return 值或默认值
     */
    public T orElse(T other) {
        if (isValid()) {
            return value;
        }
        return other;
    }

    /**
     * 如果值不存在，动态生成默认值
     *
     * @param supplier 默认值生成器
     * @return 值或默认值
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        if (isValid()) {
            return value;
        }
        return supplier.get();
    }

    /**
     * 如果值不存在，抛出指定异常
     *
     * @param exceptionSupplier 异常生成器
     * @param <X>               异常类型
     * @return 值
     * @throws X 异常
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);
        if (isValid()) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * 映射值到新类型，返回新的 Opt 实例
     *
     * @param mapper 映射函数
     * @param <U>    新类型
     * @return Opt 实例
     */
    public <U> Opt<U> map(NonNullFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isInvalid()) {
            return empty();
        } else {
            return Opt.ofNullable(mapper.apply(value));
        }
    }

    /**
     * 平面映射，支持返回 Opt 的函数
     *
     * @param mapper 映射函数
     * @param <U>    新类型
     * @return Opt 实例
     */
    public <U> Opt<U> flatMap(Function<? super T, ? extends Opt<? extends U>> mapper) {
        if (isInvalid()) {
            return empty();
        } else {
            @SuppressWarnings("unchecked")
            Opt<U> r = (Opt<U>) mapper.apply(value);
            return Objects.requireNonNull(r);
        }
    }

    /**
     * 过滤值，返回新的 Opt 实例
     *
     * @param predicate 过滤函数
     * @return Opt 实例
     */
    public Opt<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isInvalid()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    /**
     * 获取值流，若值不存在，返回空流
     *
     * @return 值流
     */
    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Opt<?> other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value != null
                ? ("Opt[" + value + "]")
                : "Opt.empty";
    }

    @FunctionalInterface
    public interface NonNullFunction<T, R> {

        @Nullable
        R apply(@NotNull T t);
    }
}