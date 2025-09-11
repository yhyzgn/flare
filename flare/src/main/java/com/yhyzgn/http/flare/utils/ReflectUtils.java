package com.yhyzgn.http.flare.utils;

import java.lang.reflect.*;

/**
 * 反射工具类
 * <p>
 * Created on 2025-09-10 15:35
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ReflectUtils {

    /**
     * Prevent instantiation
     */
    private ReflectUtils() {
        throw new UnsupportedOperationException("Instantiation is not allowed");
    }

    /**
     * 获取泛型参数的上限类型
     *
     * @param index 泛型参数索引
     * @param type  类型
     * @return 泛型参数的上限类型
     */
    public static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        Assert.isTrue(index >= 0 && index < types.length, "Index {} not in range [0, {}) for {}", index, types.length, type);
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    /**
     * 获取泛型参数的下限类型
     *
     * @param index 泛型参数索引
     * @param type  类型
     * @return 泛型参数的下限类型
     */
    public static Type getParameterLowerBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        Assert.isTrue(index >= 0 && index < types.length, "Index {} not in range [0, {}) for {}", index, types.length, type);
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getLowerBounds()[0];
        }
        return paramType;
    }

    /**
     * 获取类型参数的类型
     *
     * @param type 类型
     * @return 类型参数的类型
     */
    public static Class<?> getRawType(Type type) {
        Assert.notNull(type, "Type must not be null");
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterizedType -> {
                Type rawType = parameterizedType.getRawType();
                Assert.isTrue(rawType instanceof Class<?>, "Expected a Class<?> but got a " + rawType.getClass());
                yield (Class<?>) rawType;
            }
            case GenericArrayType genericArrayType -> {
                Type componentType = genericArrayType.getGenericComponentType();
                Assert.isTrue(componentType instanceof Class<?>, "Expected a Class<?> but got a " + componentType.getClass());
                yield Array.newInstance(getRawType(componentType), 0).getClass();
            }
            case TypeVariable<?> ignored -> Object.class;
            case WildcardType wildcardType -> getRawType(wildcardType.getUpperBounds()[0]);
            default -> {
                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        };
    }

    /**
     * 创建实例
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建实例
     *
     * @param clazz          类
     * @param parameterTypes 参数类型
     * @param args           参数值
     * @param <T>            类型
     * @return 实例
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object[] args) {
        try {
            return clazz.getConstructor(parameterTypes).newInstance(args);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
