package com.yhy.http.flare.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 异常工具类
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
     * 工具类，不允许实例化
     */
    private ExceptionUtils() {
        throw new UnsupportedOperationException("Utility class, not instantiable");
    }

    /**
     * 异常分发器
     * <p>
     * 按继承深度从高到低遍历处理器，若异常类型匹配则调用处理器并返回 true，否则返回 false
     *
     * @param catchers  异常捕获器列表
     * @param throwable 要处理的异常
     * @return 是否已被消费，true 表示已被消费，false 表示未被消费
     */
    public static boolean dispatch(List<Class<? extends Throwable>> catchers, Throwable throwable) {
        return dispatch(catchers, throwable, t -> {
            // 默认不做任何处理，因为此处的逻辑一般不会被调用
            log.error("Unhandled exception: {}", throwable.getMessage(), throwable);
        });
    }

    /**
     * 异常分发器
     * <p>
     * 按继承深度从高到低遍历处理器，若异常类型匹配则调用处理器并返回 true，否则返回 false
     *
     * @param catchers       异常捕获器列表
     * @param throwable      要处理的异常
     * @param defaultHandler 默认异常处理器
     * @return 是否已被消费，true 表示已被消费，false 表示未被消费
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
     * 尝试处理异常，返回是否已被消费
     * <p>
     * 按继承深度从高到低遍历处理器，若异常类型匹配则调用处理器并返回 true，否则返回 false
     *
     * @param sortedHandlers 已排序的处理器列表
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
     * 计算继承深度（Throwable 算 0）
     *
     * @param cls 异常类型
     * @return 继承深度
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
     * record 简化内部结构
     *
     * @param type    异常类型
     * @param handler 异常处理器
     * @param depth   继承深度
     */
    private record Handler(
        Class<? extends Throwable> type,
        Consumer<? super Throwable> handler,
        int depth
    ) {
    }
}
