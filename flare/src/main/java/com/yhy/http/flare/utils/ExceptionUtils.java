package com.yhy.http.flare.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 异常分发工具类。
 * <p>
 * 按异常继承深度从深到浅遍历处理器列表，首个类型匹配的处理器消费该异常；
 * 全部不匹配时由默认处理器兜底。
 * <p>
 * Created on 2026-03-04 18:08
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public abstract class ExceptionUtils {

    /**
     * 防止工具类被实例化。
     */
    private ExceptionUtils() {
        throw new UnsupportedOperationException("Utility class, not instantiable");
    }

    /**
     * 分发异常并返回是否被消费。
     * <p>
     * 使用默认兜底处理器：记录该未消费异常的错误日志。
     *
     * @param catchers  按异常类型注册的捕获器列表，决定哪些异常会被消费
     * @param throwable 要分发的异常
     * @return 是否已被消费，{@code true} 表示有处理器匹配并处理，{@code false} 表示未被消费
     */
    public static boolean dispatch(List<Class<? extends Throwable>> catchers, Throwable throwable) {
        return dispatch(catchers, throwable, t -> {
            // 默认不做任何处理，因为此处的逻辑一般不会被调用
            log.error("Unhandled exception: {}", throwable.getMessage(), throwable);
        });
    }

    /**
     * 分发异常并返回是否被消费。
     * <p>
     * 捕获器声明的类型按继承深度从深到浅排序，匹配最具体类型的处理器优先执行；
     * 没有任何一个类型匹配时由 {@code defaultHandler} 兜底处理，并返回 {@code false}。
     *
     * @param catchers       按异常类型注册的捕获器列表，决定哪些异常会被消费
     * @param throwable      要分发的异常
     * @param defaultHandler 没有处理器匹配时调用的兜底处理器，可为 {@code null}（此时仅返回 {@code false}）
     * @return 是否已被消费，{@code true} 表示有处理器匹配并处理，{@code false} 表示未被消费
     */
    public static boolean dispatch(List<Class<? extends Throwable>> catchers, Throwable throwable, Consumer<? super Throwable> defaultHandler) {
        // 构建 (类型 → 处理器) 映射，默认处理器兜底
        Map<Class<? extends Throwable>, Consumer<? super Throwable>> map =
            catchers.stream()
                .collect(Collectors.toMap(
                    cls -> cls,
                    cls -> defaultHandler,
                    (a, b) -> a,          // 合并策略：保留第一个（实际不会冲突）
                    LinkedHashMap::new
                ));

        // 构建处理器列表，按继承深度排序（ Throwable 算 0）
        List<Handler> list = map.entrySet().stream()
            .map(et -> new Handler(et.getKey(), et.getValue(), depth(et.getKey())))
            .sorted(Comparator.comparingInt(Handler::depth).reversed())
            .toList();

        // 处理异常，未被消费时调用默认处理器
        return handle(list, throwable);
    }

    /**
     * 尝试处理异常并返回是否已被消费。
     *
     * @param sortedHandlers 按继承深度从深到浅排序好的处理器列表
     * @param t              要处理的异常
     * @return 是否已被消费
     */
    private static boolean handle(List<Handler> sortedHandlers, Throwable t) {
        for (Handler h : sortedHandlers) {
            if (h.type.isInstance(t)) {
                h.handler.accept(t);
                return true;
            }
        }
        return false;
    }

    /**
     * 计算类型相对 {@code Throwable} 的继承深度（{@code Throwable} 记为 0）。
     *
     * @param cls 异常类型
     * @return 继承深度，越深表示类型越具体
     */
    private static int depth(Class<?> cls) {
        int depth = 0;
        while (cls != Throwable.class && cls != null) {
            cls = cls.getSuperclass();
            depth++;
        }
        return depth;
    }

    /**
     * 内部处理器结构，简化异常类型、处理器与继承深度的绑定。
     *
     * @param type    可消费的异常类型
     * @param handler 该类型对应的处理器
     * @param depth   {@code type} 的继承深度，用于排序
     */
    private record Handler(
        Class<? extends Throwable> type,
        Consumer<? super Throwable> handler,
        int depth
    ) {
    }
}
