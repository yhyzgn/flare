package com.yhy.http.flare.utils;

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
 * 可空值包装类，语义类似 {@link java.util.Optional} 但提供更贴合项目的 "有效 / 无效" API。
 * <p>
 * "有效"（{@link #isValid()}）的判定比非 null 更严格：字符串要求非空白、数字要求大于 0、
 * 集合与 Map 要求非空、数组要求长度大于 0，其他类型仅要求非 {@code null}。
 * <p>
 * Created on 2025-09-13 16:18
 *
 * @param <T> 包装的值类型
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings({"UnusedReturnValue", "DeprecatedIsStillUsed", "unused", "ClassCanBeRecord"})
public final class Opt<T> {
    private static final Opt<?> EMPTY = new Opt<>(null);

    private final T value;

    /**
     * 私有构造方法，确保只能通过静态工厂方法创建实例。
     *
     * @param value 被包装的值，可为 {@code null}
     */
    private Opt(T value) {
        this.value = value;
    }

    /**
     * 静态工厂方法：包装指定值。
     * <p>
     * 不对 {@code null} 做拦截，如需可空语义请使用 {@link #ofNullable(Object)}。
     *
     * @param value 被包装的值
     * @param <T>   值类型
     * @return 包装了 {@code value} 的 {@code Opt} 实例
     */
    public static <T> Opt<T> of(T value) {
        return new Opt<>(value);
    }

    /**
     * 静态工厂方法：包装可能为 {@code null} 的值。
     * <p>
     * {@code null} 会映射到全局共享的空实例。
     *
     * @param value 可能为 {@code null} 的值
     * @param <T>   值类型
     * @return 包装了 {@code value} 的 {@code Opt} 实例，值为 {@code null} 时返回空实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> ofNullable(T value) {
        return value == null ? (Opt<T>) EMPTY : new Opt<>(value);
    }

    /**
     * 静态工厂方法：返回全局共享的空实例。
     *
     * @param <T> 值类型
     * @return 空的 {@code Opt} 实例，其 {@link #isValid()} 恒为 {@code false}
     */
    @SuppressWarnings("unchecked")
    public static <T> Opt<T> empty() {
        return (Opt<T>) EMPTY;
    }

    /**
     * 检查值是否有效。
     * <p>
     * 判定规则：{@code null} 无效；字符串非空白才有效；数字大于 0 才有效；
     * 集合 / Map 非空才有效；数组（含基本类型数组）长度大于 0 才有效；
     * 其他类型非 {@code null} 即有效。
     *
     * @return {@code true} 值有效，{@code false} 值无效
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
     * 检查值是否无效，即 {@link #isValid()} 的取反。
     *
     * @return {@code true} 值无效，{@code false} 值有效
     */
    public boolean isInvalid() {
        return !isValid();
    }

    /**
     * 值有效时执行给定操作。
     *
     * @param action 值有效时以该值执行的消费者，不能为 {@code null}
     * @return 当前实例，支持链式调用
     */
    public Opt<T> ifValid(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (isValid()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * 值无效时执行给定操作。
     *
     * @param action 值无效时执行的任务，不能为 {@code null}
     * @return 当前实例，支持链式调用
     */
    public Opt<T> ifInvalid(Runnable action) {
        Objects.requireNonNull(action);
        if (!isValid()) {
            action.run();
        }
        return this;
    }

    /**
     * 值有效时执行给定操作。
     *
     * @param action 值有效时以该值执行的消费者
     * @return 当前实例，支持链式调用
     * @deprecated 请使用 {@link #ifValid(Consumer)}
     */
    @Deprecated
    public Opt<T> ifPresent(Consumer<? super T> action) {
        return ifValid(action);
    }

    /**
     * 值无效时执行给定操作。
     *
     * @param action 值无效时执行的任务
     * @return 当前实例，支持链式调用
     * @deprecated 请使用 {@link #ifInvalid(Runnable)}
     */
    public Opt<T> ifAbsent(Runnable action) {
        return ifInvalid(action);
    }

    /**
     * 获取值，值无效时抛出 {@link NoSuchElementException}。
     *
     * @return 被包装的值
     * @throws NoSuchElementException 值为 {@code null} 或未通过有效性判定时
     */
    public T get() {
        if (isValid()) {
            return value;
        }
        throw new NoSuchElementException("value is empty or invalid");
    }

    /**
     * 检查值是否存在。
     *
     * @return {@code true} 值有效，{@code false} 值无效
     * @deprecated 请使用 {@link #isValid()}
     */
    @Deprecated
    public boolean isPresent() {
        return isValid();
    }

    /**
     * 检查值是否为空。
     *
     * @return {@code true} 值无效，{@code false} 值有效
     * @deprecated 请使用 {@link #isInvalid()}
     */
    @Deprecated
    public boolean isEmpty() {
        return isInvalid();
    }

    /**
     * 值无效时改用备用 {@code Opt}。
     * <p>
     * 当前值有效时本方法会因类型不匹配抛出 {@code ClassCastException}，
     * 因此仅应在值可能无效的分支上调用。
     *
     * @param supplier 值无效时提供备用 {@code Opt} 的生成器，不能为 {@code null} 且不能返回 {@code null}
     * @param <U>      备用值的类型
     * @return 备用 {@code Opt} 实例
     * @throws ClassCastException 当前值有效（不应进入本分支）时
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
     * 值无效时执行给定任务，值有效时不执行。
     *
     * @param runnable 值无效时执行的任务，不能为 {@code null}
     * @return 当前实例，支持链式调用
     */
    public Opt<T> or(Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (isInvalid()) {
            runnable.run();
        }
        return this;
    }

    /**
     * 返回值，值无效时返回默认值。
     *
     * @param other 值无效时返回的默认值，可为 {@code null}
     * @return 值有效时为被包装的值，否则为 {@code other}
     */
    public T orElse(T other) {
        if (isValid()) {
            return value;
        }
        return other;
    }

    /**
     * 返回值，值无效时通过生成器动态生成默认值。
     *
     * @param supplier 值无效时生成默认值的 {@code Supplier}，不能为 {@code null}
     * @return 值有效时为被包装的值，否则为生成器产生的值
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        if (isValid()) {
            return value;
        }
        return supplier.get();
    }

    /**
     * 返回值，值无效时抛出指定异常。
     *
     * @param exceptionSupplier 值无效时生成待抛异常的 {@code Supplier}，不能为 {@code null}
     * @param <X>               异常类型
     * @return 被包装的值
     * @throws X 值无效时抛出生成的异常
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);
        if (isValid()) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * 把有效值映射为新类型。
     * <p>
     * 值无效时返回空实例；映射结果为 {@code null} 时同样得到空实例。
     *
     * @param mapper 映射函数，不能为 {@code null}，可返回 {@code null}
     * @param <U>    映射后的类型
     * @return 映射结果包装成的 {@code Opt} 实例
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
     * 平面映射：映射函数直接返回 {@code Opt}，避免双层嵌套。
     * <p>
     * 值无效时返回空实例且不调用映射函数；映射函数返回 {@code null} 时抛出 {@code NullPointerException}。
     *
     * @param mapper 返回 {@code Opt} 的映射函数，不能为 {@code null}，也不能返回 {@code null}
     * @param <U>    映射后的类型
     * @return 映射函数返回的 {@code Opt} 实例，或值无效时的空实例
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
     * 按断言过滤值：不满足断言时变为空实例。
     * <p>
     * 值无效时直接返回当前实例，不会调用断言。
     *
     * @param predicate 过滤断言，不能为 {@code null}
     * @return 断言通过时为当前实例，否则为空实例
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
     * 把值转为单元素 {@link Stream}，值无效时返回空流。
     *
     * @return 值有效时含一个元素的流，否则为空流
     */
    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    /**
     * 比较两个 {@code Opt} 是否包装了相等的值。
     *
     * @param obj 待比较对象
     * @return 都是 {@code Opt} 且内部值 {@code Objects.equals} 相等时为 {@code true}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Opt<?> other)) return false;
        return Objects.equals(value, other.value);
    }

    /**
     * 返回内部值的哈希值。
     *
     * @return {@code value.hashCode()} 的结果（内部值恒不为 {@code null} 语义上等价于空实例）
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * 返回可读字符串表示。
     *
     * @return 值有效时形如 {@code Opt[值]}，否则为 {@code Opt.empty}
     */
    @Override
    public String toString() {
        return value != null
            ? ("Opt[" + value + "]")
            : "Opt.empty";
    }

    /**
     * 入参非空的函数接口，供 {@link #map(NonNullFunction)} 使用。
     *
     * @param <T> 输入值类型，保证非 {@code null}
     * @param <R> 输出值类型，允许为 {@code null}
     */
    @FunctionalInterface
    public interface NonNullFunction<T, R> {

        /**
         * 应用函数。
         *
         * @param t 输入值，保证非 {@code null}
         * @return 输出值，可为 {@code null}
         */
        @Nullable
        R apply(@NotNull T t);
    }
}
