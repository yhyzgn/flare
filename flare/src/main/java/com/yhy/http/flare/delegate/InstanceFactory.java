package com.yhy.http.flare.delegate;

/**
 * Flare 实例创建策略。
 * <p>
 * 下游可以通过该策略决定目标类型使用构造方法、Spring Bean 或其他容器创建。
 *
 * @since 2.0.2
 */
@FunctionalInterface
public interface InstanceFactory {

    /**
     * 创建指定类型的实例。
     *
     * @param type 目标类型
     * @return 实例
     */
    Object create(Class<?> type);

    /**
     * 创建并转换为指定类型的实例。
     *
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 实例
     */
    default <T> T createTyped(Class<T> type) {
        return type.cast(create(type));
    }
}
