package com.yhy.http.flare.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 断言工具类。
 * <p>
 * 从 spring-framework:6.2.10 源码中抽取，做了部分扩展。
 * <p>
 * 提供布尔表达式、对象空值、字符串内容、数组、集合、Map 以及类型匹配等常用断言：
 * 失败时抛出 {@code IllegalStateException}（状态断言）或 {@code IllegalArgumentException}（参数断言），
 * 扩展方法还支持直接抛出调用方提供的异常实例。
 * <p>
 * Created on 2025-09-10 14:18
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class Assert {

    /**
     * 防止工具类被实例化。
     */
    private Assert() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * 断言布尔表达式为真，否则抛出 {@code IllegalStateException}。
     * <p>如果希望断言失败时抛出 {@code IllegalArgumentException}，请改用 {@link #isTrue}。
     * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
     *
     * @param expression 布尔表达式
     * @param message     断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args        异常信息的格式化参数
     * @throws IllegalStateException 当 {@code expression} 为 {@code false}
     */
    public static void state(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalStateException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言布尔表达式为真，否则抛出 {@code IllegalStateException}。
     * <p>如果希望断言失败时抛出 {@code IllegalArgumentException}，请改用 {@link #isTrue}。
     * <pre class="code">
     * Assert.state(entity.getId() == null,
     *     () -&gt; "ID for entity " + entity.getName() + " must not already be initialized");
     * </pre>
     *
     * @param expression      布尔表达式
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalStateException 当 {@code expression} 为 {@code false}
     */
    public static void state(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言布尔表达式为真，否则抛出 {@code IllegalArgumentException}。
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     *
     * @param expression 布尔表达式
     * @param message     断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args        异常信息的格式化参数
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code false}
     */
    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言布尔表达式为真，否则抛出 {@code IllegalArgumentException}。
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, () -&gt; "The value '" + i + "' must be greater than zero");
     * </pre>
     *
     * @param expression      布尔表达式
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code false}
     */
    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言对象为 {@code null}。
     * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
     *
     * @param object  待检查对象
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当对象不为 {@code null}
     */
    public static void isNull(@Nullable Object object, String message, Object... args) {
        if (object != null) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言对象为 {@code null}。
     * <pre class="code">
     * Assert.isNull(value, () -&gt; "The value '" + value + "' must be null");
     * </pre>
     *
     * @param object          待检查对象
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当对象不为 {@code null}
     */
    public static void isNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言对象不为 {@code null}。
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object  待检查对象
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当对象为 {@code null}
     */
    public static void notNull(@Nullable Object object, String message, Object... args) {
        if (object == null) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言对象不为 {@code null}。
     * <pre class="code">
     * Assert.notNull(entity.getId(),
     *     () -&gt; "ID for entity " + entity.getName() + " must not be null");
     * </pre>
     *
     * @param object          待检查对象
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当对象为 {@code null}
     */
    public static void notNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言字符串非空：既不是 {@code null}，也不是空字符串。
     * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
     *
     * @param text    待检查字符串
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当字符串为 {@code null} 或空串
     * @see StringUtils#hasLength
     */
    public static void hasLength(@Nullable String text, String message, Object... args) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言字符串非空：既不是 {@code null}，也不是空字符串。
     * <pre class="code">
     * Assert.hasLength(account.getName(),
     *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
     * </pre>
     *
     * @param text            待检查字符串
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当字符串为 {@code null} 或空串
     * @see StringUtils#hasLength
     */
    public static void hasLength(@Nullable String text, Supplier<String> messageSupplier) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言字符串含有效文本：不是 {@code null}，且至少包含一个非空白字符。
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text    待检查字符串
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当字符串为 {@code null} 或不含非空白字符
     * @see StringUtils#hasText
     */
    public static void hasText(@Nullable String text, String message, Object... args) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言字符串含有效文本：不是 {@code null}，且至少包含一个非空白字符。
     * <pre class="code">
     * Assert.hasText(account.getName(),
     *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
     * </pre>
     *
     * @param text            待检查字符串
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当字符串为 {@code null} 或不含非空白字符
     * @see StringUtils#hasText
     */
    public static void hasText(@Nullable String text, Supplier<String> messageSupplier) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言文本中不包含指定子串。
     * <p>待检查文本或子串为 {@code null} / 空串时视为不包含，断言通过。
     * <pre class="code">Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");</pre>
     *
     * @param textToSearch 待检查文本
     * @param substring    要查找的子串
     * @param message      断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args         异常信息的格式化参数
     * @throws IllegalArgumentException 当文本包含子串
     */
    public static void doesNotContain(@Nullable String textToSearch, String substring, String message, Object... args) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
                textToSearch.contains(substring)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言文本中不包含指定子串。
     * <p>待检查文本或子串为 {@code null} / 空串时视为不包含，断言通过。
     * <pre class="code">
     * Assert.doesNotContain(name, forbidden, () -&gt; "Name must not contain '" + forbidden + "'");
     * </pre>
     *
     * @param textToSearch    待检查文本
     * @param substring       要查找的子串
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当文本包含子串
     */
    public static void doesNotContain(@Nullable String textToSearch, String substring, Supplier<String> messageSupplier) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
                textToSearch.contains(substring)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言数组含元素：既不是 {@code null}，也至少包含一个元素。
     * <pre class="code">Assert.notEmpty(array, "The array must contain elements");</pre>
     *
     * @param array   待检查数组
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当数组为 {@code null} 或不含任何元素
     */
    public static void notEmpty(@Nullable Object[] array, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言数组含元素：既不是 {@code null}，也至少包含一个元素。
     * <pre class="code">
     * Assert.notEmpty(array, () -&gt; "The " + arrayType + " array must contain elements");
     * </pre>
     *
     * @param array           待检查数组
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当数组为 {@code null} 或不含任何元素
     */
    public static void notEmpty(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言数组不含 {@code null} 元素。
     * <p>注意：数组为空时不抛异常。
     * <pre class="code">Assert.noNullElements(array, "The array must contain non-null elements");</pre>
     *
     * @param array   待检查数组
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当数组包含 {@code null} 元素
     */
    public static void noNullElements(@Nullable Object[] array, String message, Object... args) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(StringUtils.format(message, args));
                }
            }
        }
    }

    /**
     * 断言数组不含 {@code null} 元素。
     * <p>注意：数组为空时不抛异常。
     * <pre class="code">
     * Assert.noNullElements(array, () -&gt; "The " + arrayType + " array must contain non-null elements");
     * </pre>
     *
     * @param array           待检查数组
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当数组包含 {@code null} 元素
     */
    public static void noNullElements(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }
    }

    /**
     * 断言集合含元素：既不是 {@code null}，也至少包含一个元素。
     * <pre class="code">Assert.notEmpty(collection, "Collection must contain elements");</pre>
     *
     * @param collection 待检查集合
     * @param message    断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args       异常信息的格式化参数
     * @throws IllegalArgumentException 当集合为 {@code null} 或不含任何元素
     */
    public static void notEmpty(@Nullable Collection<?> collection, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言集合含元素：既不是 {@code null}，也至少包含一个元素。
     * <pre class="code">
     * Assert.notEmpty(collection, () -&gt; "The " + collectionType + " collection must contain elements");
     * </pre>
     *
     * @param collection      待检查集合
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当集合为 {@code null} 或不含任何元素
     */
    public static void notEmpty(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言集合不含 {@code null} 元素。
     * <p>注意：集合为空时不抛异常。
     * <pre class="code">Assert.noNullElements(collection, "Collection must contain non-null elements");</pre>
     *
     * @param collection 待检查集合
     * @param message    断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args       异常信息的格式化参数
     * @throws IllegalArgumentException 当集合包含 {@code null} 元素
     */
    public static void noNullElements(@Nullable Collection<?> collection, String message, Object... args) {
        if (collection != null) {
            for (Object element : collection) {
                if (element == null) {
                    throw new IllegalArgumentException(StringUtils.format(message, args));
                }
            }
        }
    }

    /**
     * 断言集合不含 {@code null} 元素。
     * <p>注意：集合为空时不抛异常。
     * <pre class="code">
     * Assert.noNullElements(collection, () -&gt; "Collection " + collectionName + " must contain non-null elements");
     * </pre>
     *
     * @param collection      待检查集合
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当集合包含 {@code null} 元素
     */
    public static void noNullElements(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (collection != null) {
            for (Object element : collection) {
                if (element == null) {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }
    }

    /**
     * 断言 Map 含条目：既不是 {@code null}，也至少包含一个键值对。
     * <pre class="code">Assert.notEmpty(map, "Map must contain entries");</pre>
     *
     * @param map     待检查 Map
     * @param message 断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当 Map 为 {@code null} 或不含任何条目
     */
    public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言 Map 含条目：既不是 {@code null}，也至少包含一个键值对。
     * <pre class="code">
     * Assert.notEmpty(map, () -&gt; "The " + mapType + " map must contain entries");
     * </pre>
     *
     * @param map             待检查 Map
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}（此时异常信息为 {@code null}）
     * @throws IllegalArgumentException 当 Map 为 {@code null} 或不含任何条目
     */
    public static void notEmpty(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言对象是指定类型的实例。
     * <pre class="code">Assert.instanceOf(Foo.class, foo, "Foo expected");</pre>
     *
     * @param type    要检查的目标类型，不能为 {@code null}
     * @param obj     待检查对象
     * @param message 断言失败时前缀的上下文信息。
     *                为空或以 ":" / ";" / "," / "." 结尾时追加完整默认异常信息；
     *                以空格结尾时追加实际对象的类型名；其余情况追加 ": " 与类型名。
     *                支持以 {@code args} 填充的格式化占位。
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 当对象不是 {@code type} 的实例
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj, String message, Object... args) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, StringUtils.format(message, args));
        }
    }

    /**
     * 断言对象是指定类型的实例。
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo, () -&gt; "Processing " + Foo.class.getSimpleName() + ":");
     * </pre>
     *
     * @param type            要检查的目标类型，不能为 {@code null}
     * @param obj             待检查对象
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}。
     *                        消息拼接规则见 {@link #isInstanceOf(Class, Object, String, Object...)}。
     * @throws IllegalArgumentException 当对象不是 {@code type} 的实例
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj, Supplier<String> messageSupplier) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言对象是指定类型的实例，失败时使用默认异常信息。
     * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
     *
     * @param type 要检查的目标类型，不能为 {@code null}
     * @param obj  待检查对象
     * @throws IllegalArgumentException 当对象不是 {@code type} 的实例
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj) {
        isInstanceOf(type, obj, "");
    }

    /**
     * 断言 {@code superType.isAssignableFrom(subType)} 为 {@code true}。
     * <pre class="code">Assert.isAssignable(Number.class, myClass, "Number expected");</pre>
     *
     * @param superType 要检查的父类型，不能为 {@code null}
     * @param subType   待检查的子类型
     * @param message   断言失败时前缀的上下文信息。
     *                  为空或以 ":" / ";" / "," / "." 结尾时追加完整默认异常信息；
     *                  以空格结尾时追加子类型名；其余情况追加 ": " 与子类型名。
     *                  支持以 {@code args} 填充的格式化占位。
     * @param args      异常信息的格式化参数
     * @throws IllegalArgumentException 当子类型为 {@code null} 或不可赋值给父类型
     */
    public static void isAssignable(Class<?> superType, @Nullable Class<?> subType, String message, Object... args) {
        notNull(superType, "Supertype to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, StringUtils.format(message, args));
        }
    }

    /**
     * 断言 {@code superType.isAssignableFrom(subType)} 为 {@code true}。
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass, () -&gt; "Processing " + myAttributeName + ":");
     * </pre>
     *
     * @param superType       要检查的父类型，不能为 {@code null}
     * @param subType         待检查的子类型
     * @param messageSupplier 断言失败时提供异常信息的 {@code Supplier}，可为 {@code null}。
     *                        消息拼接规则见 {@link #isAssignable(Class, Class, String, Object...)}。
     * @throws IllegalArgumentException 当子类型为 {@code null} 或不可赋值给父类型
     */
    public static void isAssignable(Class<?> superType, @Nullable Class<?> subType, Supplier<String> messageSupplier) {
        notNull(superType, "Supertype to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
        }
    }

    /**
     * 断言 {@code superType.isAssignableFrom(subType)} 为 {@code true}，失败时使用默认异常信息。
     * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
     *
     * @param superType 要检查的父类型
     * @param subType   待检查的子类型
     * @throws IllegalArgumentException 当子类型为 {@code null} 或不可赋值给父类型
     */
    public static void isAssignable(Class<?> superType, @Nullable Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    private static void instanceCheckFailed(Class<?> type, @Nullable Object obj, @Nullable String msg) {
        String className = (obj != null ? obj.getClass().getName() : "null");
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }
        if (defaultMessage) {
            result = result + ("Object of class [" + className + "] must be an instance of " + type);
        }
        throw new IllegalArgumentException(result);
    }

    private static void assignableCheckFailed(Class<?> superType, @Nullable Class<?> subType, @Nullable String msg) {
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, subType);
                defaultMessage = false;
            }
        }
        if (defaultMessage) {
            result = result + (subType + " is not assignable to " + superType);
        }
        throw new IllegalArgumentException(result);
    }

    private static boolean endsWithSeparator(String msg) {
        return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
    }

    private static String messageWithTypeName(String msg, @Nullable Object typeName) {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

    @Nullable
    private static String nullSafeGet(@Nullable Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

    // 扩展方法

    /**
     * 断言布尔表达式为真，否则抛出调用方提供的异常。
     *
     * @param expression 布尔表达式
     * @param t          断言失败时抛出的异常对象
     * @param <T>        异常类型
     * @throws T 当 {@code expression} 为 {@code false} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void isTrue(boolean expression, T t) {
        if (!expression) {
            throw t;
        }
    }

    /**
     * 断言布尔表达式为假，否则抛出 {@code IllegalArgumentException}。
     *
     * @param expression 布尔表达式
     * @param message    断言失败时使用的异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args       异常信息的格式化参数
     * @throws IllegalArgumentException 当 {@code expression} 为 {@code true}
     */
    public static void isFalse(boolean expression, String message, Object... args) {
        if (expression) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 断言布尔表达式为假，否则抛出调用方提供的异常。
     *
     * @param expression 布尔表达式
     * @param t          断言失败时抛出的异常对象
     * @param <T>        异常类型
     * @throws T 当 {@code expression} 为 {@code true} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void isFalse(boolean expression, T t) {
        if (expression) {
            throw t;
        }
    }

    /**
     * 断言对象为 {@code null}，否则抛出调用方提供的异常。
     *
     * @param object 待检查对象
     * @param t      断言失败时抛出的异常对象
     * @param <T>    异常类型
     * @throws T 当对象不为 {@code null} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void isNull(@Nullable Object object, T t) {
        if (null != object) {
            throw t;
        }
    }

    /**
     * 断言对象不为 {@code null}，否则抛出调用方提供的异常。
     *
     * @param object 待检查对象
     * @param t      断言失败时抛出的异常对象
     * @param <T>    异常类型
     * @throws T 当对象为 {@code null} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void notNull(@Nullable Object object, T t) {
        if (null == object) {
            throw t;
        }
    }

    /**
     * 断言字符串非空（非 {@code null} 且非空串），否则抛出调用方提供的异常。
     *
     * @param text 待检查字符串
     * @param t    断言失败时抛出的异常对象
     * @param <T>  异常类型
     * @throws T 当字符串为 {@code null} 或空串时抛出 {@code t}
     */
    public static <T extends RuntimeException> void hasLength(@Nullable String text, T t) {
        if (!StringUtils.hasLength(text)) {
            throw t;
        }
    }

    /**
     * 断言字符串含有效文本（非 {@code null} 且至少一个非空白字符），否则抛出调用方提供的异常。
     *
     * @param text 待检查字符串
     * @param t    断言失败时抛出的异常对象
     * @param <T>  异常类型
     * @throws T 当字符串为 {@code null} 或不含非空白字符时抛出 {@code t}
     */
    public static <T extends RuntimeException> void hasText(@Nullable String text, T t) {
        if (!StringUtils.hasText(text)) {
            throw t;
        }
    }

    /**
     * 断言文本中不包含指定子串，否则抛出调用方提供的异常。
     * <p>待检查文本或子串为 {@code null} / 空串时视为不包含，断言通过。
     *
     * @param textToSearch 待检查文本
     * @param substring    要查找的子串
     * @param t            断言失败时抛出的异常对象
     * @param <T>          异常类型
     * @throws T 当文本包含子串时抛出 {@code t}
     */
    public static <T extends RuntimeException> void doesNotContain(@Nullable String textToSearch, String substring, T t) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw t;
        }
    }

    /**
     * 断言数组含元素（非 {@code null} 且至少一个元素），否则抛出调用方提供的异常。
     *
     * @param array 待检查数组
     * @param t     断言失败时抛出的异常对象
     * @param <T>   异常类型
     * @throws T 当数组为 {@code null} 或不含任何元素时抛出 {@code t}
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Object[] array, T t) {
        if (ObjectUtils.isEmpty(array)) {
            throw t;
        }
    }

    /**
     * 断言数组不含 {@code null} 元素，否则抛出调用方提供的异常。
     * <p>注意：数组为空时不抛异常。
     *
     * @param array 待检查数组
     * @param t     断言失败时抛出的异常对象
     * @param <T>   异常类型
     * @throws T 当数组包含 {@code null} 元素时抛出 {@code t}
     */
    public static <T extends RuntimeException> void noNullElements(@Nullable Object[] array, T t) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw t;
                }
            }
        }
    }

    /**
     * 断言集合含元素（非 {@code null} 且至少一个元素），否则抛出调用方提供的异常。
     *
     * @param collection 待检查集合
     * @param t          断言失败时抛出的异常对象
     * @param <T>        异常类型
     * @throws T 当集合为 {@code null} 或不含任何元素时抛出 {@code t}
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Collection<?> collection, T t) {
        if (CollectionUtils.isEmpty(collection)) {
            throw t;
        }
    }

    /**
     * 断言集合不含 {@code null} 元素，否则抛出调用方提供的异常。
     * <p>注意：集合为空时不抛异常。
     *
     * @param collection 待检查集合
     * @param t          断言失败时抛出的异常对象
     * @param <T>        异常类型
     * @throws T 当集合包含 {@code null} 元素时抛出 {@code t}
     */
    public static <T extends RuntimeException> void noNullElements(@Nullable Collection<?> collection, T t) {
        if (collection != null) {
            for (Object element : collection) {
                if (element == null) {
                    throw t;
                }
            }
        }
    }

    /**
     * 断言 Map 含条目（非 {@code null} 且至少一个键值对），否则抛出调用方提供的异常。
     *
     * @param map 待检查 Map
     * @param t   断言失败时抛出的异常对象
     * @param <T> 异常类型
     * @throws T 当 Map 为 {@code null} 或不含任何条目时抛出 {@code t}
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Map<?, ?> map, T t) {
        if (MapUtils.isEmpty(map)) {
            throw t;
        }
    }

    /**
     * 断言对象是指定类型的实例，否则抛出调用方提供的异常。
     * <p>{@code type} 为 {@code null} 时同样抛出 {@code t}。
     *
     * @param type 要检查的目标类型
     * @param obj  待检查对象
     * @param t    断言失败时抛出的异常对象
     * @param <T>  异常类型
     * @throws T 当对象不是 {@code type} 的实例或 {@code type} 为 {@code null} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void isInstanceOf(Class<?> type, @Nullable Object obj, T t) {
        notNull(type, t);
        if (!type.isInstance(obj)) {
            throw t;
        }
    }

    /**
     * 断言父类型可接收子类型（{@code superType.isAssignableFrom(subType)}），否则抛出调用方提供的异常。
     * <p>{@code superType} 为 {@code null} 时同样抛出 {@code t}。
     *
     * @param superType 要检查的父类型
     * @param subType   待检查的子类型
     * @param t         断言失败时抛出的异常对象
     * @param <T>       异常类型
     * @throws T 当子类型为 {@code null}、不可赋值给父类型或父类型为 {@code null} 时抛出 {@code t}
     */
    public static <T extends RuntimeException> void isAssignable(Class<?> superType, @Nullable Class<?> subType, T t) {
        notNull(superType, t);
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw t;
        }
    }

    /**
     * 主动抛出指定的运行时异常。
     *
     * @param t   要抛出的异常对象
     * @param <T> 异常类型
     * @throws T 总是抛出 {@code t}
     */
    public static <T extends RuntimeException> void thr(T t) {
        throw t;
    }

    /**
     * 主动抛出格式化的 {@code IllegalStateException}。
     *
     * @param message 异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalStateException 总是抛出
     */
    public static void illegalState(String message, Object... args) {
        throw new IllegalStateException(StringUtils.format(message, args));
    }

    /**
     * 主动抛出格式化的 {@code IllegalArgumentException}。
     *
     * @param message 异常信息模板，支持以 {@code args} 填充的格式化占位
     * @param args    异常信息的格式化参数
     * @throws IllegalArgumentException 总是抛出
     */
    public static void illegalArguments(String message, Object... args) {
        throw new IllegalArgumentException(StringUtils.format(message, args));
    }

    /**
     * 包裹一段可能抛出任意异常的逻辑，并将其转换为运行时异常。
     * <p>典型用法是把受检异常或反射调用包裹为调用方可控的运行时异常。
     *
     * @param wrapper  待执行的逻辑，异常会向上传播
     * @param function 把捕获的 {@code Throwable} 转换为目标运行时异常的函数
     * @param <R>      正常执行的返回值类型
     * @param <E>      转换后的运行时异常类型
     * @return {@code wrapper} 正常执行的返回值
     * @throws E 当 {@code wrapper} 抛出任何异常时，抛出转换后的异常
     */
    public static <R, E extends RuntimeException> R wrap(ThrowableWrapper<R> wrapper, Function<Throwable, E> function) {
        try {
            return wrapper.wrap();
        } catch (Throwable e) {
            throw function.apply(e);
        }
    }

    /**
     * 可抛出任意异常的逻辑包裹器，供 {@link #wrap(ThrowableWrapper, Function)} 使用。
     *
     * @param <R> 逻辑的返回值类型
     */
    @FunctionalInterface
    public interface ThrowableWrapper<R> {

        /**
         * 执行被包裹的逻辑。
         *
         * @return 逻辑的返回值
         * @throws Throwable 逻辑执行过程中的任意异常，由 {@code wrap} 的调用方转换处理
         */
        R wrap() throws Throwable;
    }
}
