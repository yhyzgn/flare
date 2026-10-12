package com.yhy.http.flare.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 反射与泛型解析工具类。
 * <p>
 * 提供泛型参数上下限、原始类型提取、类型变量解析、类型相等比较等能力，
 * 是 {@code ParameterHandler} 解析方法签名与返回类型的底层支撑。
 * <p>
 * Created on 2025-09-10 15:35
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ReflectUtils {
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    /**
     * 防止工具类被实例化。
     */
    private ReflectUtils() {
        throw new UnsupportedOperationException("Instantiation is not allowed");
    }

    /**
     * 获取泛型参数的上限类型。
     * <p>
     * 泛型参数为 {@code ? extends X} 通配符时返回 {@code X}，否则返回参数本身。
     *
     * @param index 泛型参数在 {@code type} 中的索引，从 0 开始
     * @param type  泛型化类型
     * @return 指定索引处泛型参数的上限类型
     * @throws IllegalArgumentException 索引越界时
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
     * 获取泛型参数的下限类型。
     * <p>
     * 泛型参数为 {@code ? super X} 通配符时返回 {@code X}，否则返回参数本身。
     *
     * @param index 泛型参数在 {@code type} 中的索引，从 0 开始
     * @param type  泛型化类型
     * @return 指定索引处泛型参数的下限类型
     * @throws IllegalArgumentException 索引越界时
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
     * 提取类型的原始 {@code Class}。
     * <p>
     * {@code ParameterizedType} 取其 rawType；泛型数组取元素类型再重建数组类；
     * 类型变量返回 {@code Object.class}；通配符取第一个上限的原始类型。
     *
     * @param type 待解析类型，不能为 {@code null}
     * @return 对应的原始 {@code Class}
     * @throws IllegalArgumentException 类型结构不符合预期或不支持时
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
     * 判断类型是否可被安全解析。
     * <p>
     * {@code Class}、类型变量与通配符可解析；{@code ParameterizedType} 与泛型数组递归检查其成分。
     *
     * @param type 待检查类型，可为 {@code null}
     * @return {@code true} 表示包含不可解析成分（应尽早失败），{@code false} 表示可解析
     * @throws IllegalArgumentException 遇到不支持的类型形态时
     */
    public static boolean hasUnresolvableType(@Nullable Type type) {
        if (type instanceof Class<?>) {
            return false;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (hasUnresolvableType(typeArgument)) {
                    return true;
                }
            }
            return false;
        }
        if (type instanceof GenericArrayType) {
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            return false;
        }
        if (type instanceof WildcardType) {
            return false;
        }
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
    }

    private static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // We can't reduce this further.
        if (declaredByRaw == null) return unknown;

        Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    private static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) return context;

        // We skip searching through interfaces if unknown is an interface.
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // Check our supertypes.
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // We can't resolve this further.
        return toResolve;
    }

    /**
     * 在数组中查找指定对象首次出现的索引。
     *
     * @param array  待查找数组
     * @param toFind 目标对象，按 {@code equals} 匹配
     * @return 首次出现的索引
     * @throws NoSuchElementException 数组中不含目标对象时
     */
    public static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) {
            if (toFind.equals(array[i])) return i;
        }
        throw new NoSuchElementException();
    }

    /**
     * 把类型转为字符串表示。
     * <p>
     * {@code Class} 输出全限定名，其他 {@code Type} 输出其 {@code toString}。
     *
     * @param type 待转换类型
     * @return 类型的字符串表示
     */
    public static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    /**
     * 校验类型不是基本类型。
     *
     * @param type 待检查类型
     * @throws IllegalArgumentException 类型是基本类型（如 {@code int.class}）时
     */
    public static void checkNotPrimitive(Type type) {
        if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 判断注解数组中是否存在指定类型的注解。
     *
     * @param annotations 待检查的注解数组
     * @param cls         目标注解类型
     * @return {@code true} 表示存在指定类型的注解
     */
    public static boolean isAnnotationPresent(Annotation[] annotations, Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 比较两个 {@code Type} 是否语义相等。
     * <p>
     * 对 {@code Class}、{@code ParameterizedType}、{@code GenericArrayType}、
     * {@code WildcardType} 与 {@code TypeVariable} 分别按各自语义比较；
     * 两边同为 {@code null} 视为相等，形态不匹配一律视为不等。
     *
     * @param a 类型一，可为 {@code null}
     * @param b 类型二，可为 {@code null}
     * @return {@code true} 表示两个类型语义相等
     */
    public static boolean equals(Type a, Type b) {
        if (a == b) {
            return true; // Also handles (a == null && b == null).
        } else if (a instanceof Class) {
            return a.equals(b); // Class already specifies equals().
        } else if (a instanceof ParameterizedType pa) {
            if (!(b instanceof ParameterizedType pb)) return false;
            Object ownerA = pa.getOwnerType();
            Object ownerB = pb.getOwnerType();
            return (Objects.equals(ownerA, ownerB)) && pa.getRawType().equals(pb.getRawType()) && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
        } else if (a instanceof GenericArrayType ga) {
            if (!(b instanceof GenericArrayType gb)) return false;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
        } else if (a instanceof WildcardType wa) {
            if (!(b instanceof WildcardType wb)) return false;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
        } else if (a instanceof TypeVariable<?> va) {
            if (!(b instanceof TypeVariable<?> vb)) return false;
            return va.getGenericDeclaration() == vb.getGenericDeclaration() && va.getName().equals(vb.getName());
        } else {
            return false; // This isn't a type we support!
        }
    }

    /**
     * 解析指定类型相对某个上下文类型的父类型。
     *
     * @param context        上下文类型，提供类型变量的实参绑定
     * @param contextRawType 上下文类型的原始 {@code Class}
     * @param supertype      要解析到的父类型
     * @return 已把类型变量替换为实参的父类型
     * @throws IllegalArgumentException {@code supertype} 不是 {@code contextRawType} 的父类型时
     */
    public static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        if (!supertype.isAssignableFrom(contextRawType)) throw new IllegalArgumentException();
        return resolve(context, contextRawType, getGenericSupertype(context, contextRawType, supertype));
    }

    /**
     * 在给定上下文中解析类型，把类型变量替换为实参。
     * <p>
     * 递归处理数组、参数化类型与通配符的成分；无需替换时原样返回以避免对象创建。
     *
     * @param context        上下文类型，提供类型变量的实参绑定
     * @param contextRawType 上下文类型的原始 {@code Class}
     * @param toResolve      待解析类型
     * @return 类型变量已被实参替换的解析结果
     */
    public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        // This implementation is made a little more complicated in an attempt to avoid object-creation.
        while (true) {
            switch (toResolve) {
                case TypeVariable<?> typeVariable -> {
                    toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
                    if (toResolve == typeVariable) {
                        return toResolve;
                    }
                }
                case Class<?> original when original.isArray() -> {
                    Type componentType = original.getComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                        newComponentType);

                }
                case GenericArrayType original -> {
                    Type componentType = original.getGenericComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                        newComponentType);

                }
                case ParameterizedType original -> {
                    Type ownerType = original.getOwnerType();
                    Type newOwnerType = resolve(context, contextRawType, ownerType);
                    boolean changed = newOwnerType != ownerType;

                    Type[] args = original.getActualTypeArguments();
                    for (int t = 0, length = args.length; t < length; t++) {
                        Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
                        if (resolvedTypeArgument != args[t]) {
                            if (!changed) {
                                args = args.clone();
                                changed = true;
                            }
                            args[t] = resolvedTypeArgument;
                        }
                    }

                    return changed
                        ? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
                        : original;
                }
                case WildcardType original -> {
                    Type[] originalLowerBound = original.getLowerBounds();
                    Type[] originalUpperBound = original.getUpperBounds();

                    if (originalLowerBound.length == 1) {
                        Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
                        if (lowerBound != originalLowerBound[0]) {
                            return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
                        }
                    } else if (originalUpperBound.length == 1) {
                        Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
                        if (upperBound != originalUpperBound[0]) {
                            return new WildcardTypeImpl(new Type[]{upperBound}, EMPTY_TYPE_ARRAY);
                        }
                    }
                    return original;
                }
                case null, default -> {
                    return toResolve;
                }
            }
        }
    }

    /**
     * 构造带方法上下文的 {@code IllegalArgumentException}。
     *
     * @param method 发生错误的方法
     * @param message 异常信息模板，按 {@code String.format} 规则以 {@code args} 填充
     * @param args    消息的格式化参数
     * @return 可直接抛出的运行时异常
     */
    public static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(method, null, message, args);
    }

    /**
     * 构造带方法上下文与根因的 {@code IllegalArgumentException}。
     *
     * @param method 发生错误的方法
     * @param cause   根因异常，可为 {@code null}
     * @param message 异常信息模板，按 {@code String.format} 规则以 {@code args} 填充
     * @param args    消息的格式化参数
     * @return 可直接抛出的运行时异常
     */
    public static RuntimeException methodError(Method method, @Nullable Throwable cause, String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException(message
            + "\n    for method "
            + method.getDeclaringClass().getSimpleName()
            + "."
            + method.getName(), cause);
    }

    /**
     * 构造带方法与参数序号上下文的 {@code IllegalArgumentException}。
     *
     * @param method   发生错误的方法
     * @param cause     根因异常
     * @param position  参数在方法签名中的索引，从 0 开始（提示信息中转为从 1 计数）
     * @param message   异常信息模板，按 {@code String.format} 规则以 {@code args} 填充
     * @param args      消息的格式化参数
     * @return 可直接抛出的运行时异常
     */
    public static RuntimeException parameterError(Method method, Throwable cause, int position, String message, Object... args) {
        return methodError(method, cause, message + " (parameter #" + (position + 1) + ")", args);
    }

    /**
     * 构造带方法与参数序号上下文的 {@code IllegalArgumentException}。
     *
     * @param method   发生错误的方法
     * @param position  参数在方法签名中的索引，从 0 开始（提示信息中转为从 1 计数）
     * @param message   异常信息模板，按 {@code String.format} 规则以 {@code args} 填充
     * @param args      消息的格式化参数
     * @return 可直接抛出的运行时异常
     */
    public static RuntimeException parameterError(Method method, int position, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (position + 1) + ")", args);
    }

    /**
     * 通过无参构造方法创建实例。
     *
     * @param clazz 目标类，必须存在可访问的无参构造方法
     * @param <T>   目标类型
     * @return 新创建的实例
     * @throws RuntimeException 缺少无参构造方法、构造方法抛出异常或访问受限时
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过指定参数的构造方法创建实例。
     *
     * @param clazz          目标类
     * @param parameterTypes 构造方法的形参类型数组
     * @param args           传给构造方法的实参数组
     * @param <T>            目标类型
     * @return 新创建的实例
     * @throws RuntimeException 匹配的构造方法不存在、构造方法抛出异常或访问受限时
     */
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object[] args) {
        try {
            return clazz.getConstructor(parameterTypes).newInstance(args);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断类型是否为基本类型、包装类型或 {@code String}。
     * <p>
     * 覆盖基本类型、{@code Number} 及其子类、{@code Boolean}、{@code Character} 与 {@code String}。
     *
     * @param clazz 待检查类型
     * @return {@code true} 表示属于上述简单类型集合
     */
    public static boolean isPrimitiveOrString(Class<?> clazz) {
        return clazz.isPrimitive()
            || Number.class.isAssignableFrom(clazz)
            || clazz == Boolean.class
            || clazz == Character.class
            || clazz == String.class;
    }

    /**
     * 反射读取对象上指定字段的值。
     * <p>
     * 读取前会强制把字段设为可访问。
     *
     * @param field 目标字段
     * @param obj   字段所属对象
     * @return 字段值；{@code field} 或 {@code obj} 为 {@code null} 时返回 {@code null}
     * @throws RuntimeException 字段访问被拒绝时
     */
    public static Object getValue(Field field, Object obj) {
        if (null == obj || null == field) {
            return null;
        }
        field.setAccessible(true);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static final class ParameterizedTypeImpl implements ParameterizedType {
        private final @Nullable
        Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... typeArguments) {
            // Require an owner type if the raw type needs it.
            if (rawType instanceof Class<?>
                && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
                throw new IllegalArgumentException();
            }

            for (Type typeArgument : typeArguments) {
                Objects.requireNonNull(typeArgument, "typeArgument == null");
                checkNotPrimitive(typeArgument);
            }

            this.ownerType = ownerType;
            this.rawType = rawType;
            this.typeArguments = typeArguments.clone();
        }

        /**
         * 返回类型实参数组。
         *
         * @return 类型实参的防御性副本
         */
        @Override
        public Type @NotNull [] getActualTypeArguments() {
            return typeArguments.clone();
        }

        /**
         * 返回原始类型。
         *
         * @return 参数化类型的 rawType
         */
        @Override
        public @NotNull Type getRawType() {
            return rawType;
        }

        @Override
        public @Nullable
        Type getOwnerType() {
            return ownerType;
        }

        /**
         * 按 {@link ReflectUtils#equals(Type, Type)} 的语义比较相等性。
         *
         * @param other 待比较对象
         * @return {@code true} 表示是语义相等的 {@code ParameterizedType}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && ReflectUtils.equals(this, (ParameterizedType) other);
        }

        /**
         * 由实参、rawType 与 ownerType 组合出的哈希值。
         *
         * @return 哈希值
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments)
                ^ rawType.hashCode()
                ^ (ownerType != null ? ownerType.hashCode() : 0);
        }

        /**
         * 返回形如 {@code Raw<A, B>} 的字符串表示。
         *
         * @return 类型的字符串表示
         */
        @Override
        public String toString() {
            if (typeArguments.length == 0) return typeToString(rawType);
            StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
            result.append(typeToString(rawType));
            result.append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < typeArguments.length; i++) {
                result.append(", ").append(typeToString(typeArguments[i]));
            }
            return result.append(">").toString();
        }
    }

    private record GenericArrayTypeImpl(Type componentType) implements GenericArrayType {

        /**
         * 返回数组的元素类型。
         *
         * @return 泛型数组的 componentType
         */
        @Override
        public @NotNull Type getGenericComponentType() {
            return componentType;
        }

        /**
         * 按 {@link ReflectUtils#equals(Type, Type)} 的语义比较相等性。
         *
         * @param o 待比较对象
         * @return {@code true} 表示是语义相等的 {@code GenericArrayType}
         */
        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType && ReflectUtils.equals(this, (GenericArrayType) o);
        }

        /**
         * 返回形如 {@code Component[]} 的字符串表示。
         *
         * @return 类型的字符串表示
         */
        @Override
        public @NotNull String toString() {
            return typeToString(componentType) + "[]";
        }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type upperBound;
        private final @Nullable
        Type lowerBound;

        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            if (lowerBounds.length > 1) throw new IllegalArgumentException();
            if (upperBounds.length != 1) throw new IllegalArgumentException();

            if (lowerBounds.length == 1) {
                if (lowerBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(lowerBounds[0]);
                if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
                this.lowerBound = lowerBounds[0];
                this.upperBound = Object.class;
            } else {
                if (upperBounds[0] == null) throw new NullPointerException();
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = upperBounds[0];
            }
        }

        /**
         * 返回通配符的上限类型数组。
         *
         * @return 恰含一个上限类型的数组
         */
        @Override
        public Type @NotNull [] getUpperBounds() {
            return new Type[]{upperBound};
        }

        /**
         * 返回通配符的下限类型数组。
         *
         * @return 无下限时为空数组，否则恰含一个下限类型
         */
        @Override
        public Type @NotNull [] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
        }

        /**
         * 按 {@link ReflectUtils#equals(Type, Type)} 的语义比较相等性。
         *
         * @param other 待比较对象
         * @return {@code true} 表示是语义相等的 {@code WildcardType}
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && ReflectUtils.equals(this, (WildcardType) other);
        }

        /**
         * 由上限与下限组合出的哈希值。
         *
         * @return 哈希值，等价于上下限数组哈希的异或
         */
        @Override
        public int hashCode() {
            // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        /**
         * 返回形如 {@code ?}、{@code ? extends X} 或 {@code ? super X} 的字符串表示。
         *
         * @return 通配符的字符串表示
         */
        @Override
        public String toString() {
            if (lowerBound != null) return "? super " + typeToString(lowerBound);
            if (upperBound == Object.class) return "?";
            return "? extends " + typeToString(upperBound);
        }
    }
}
