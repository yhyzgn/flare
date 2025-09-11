package com.yhyzgn.http.flare.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 断言工具类
 * <p>
 * 从 spring-framework:6.2.10 源码中抽取，做了部分扩展
 * <p>
 * Created on 2025-09-10 14:18
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class Assert {

    /**
     * Prevent instantiation.
     */
    private Assert() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalStateException}
     * if the expression evaluates to {@code false}.
     * <p>Call {@link #isTrue} if you wish to throw an {@code IllegalArgumentException}
     * on an assertion failure.
     * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalStateException if {@code expression} is {@code false}
     */
    public static void state(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalStateException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalStateException}
     * if the expression evaluates to {@code false}.
     * <p>Call {@link #isTrue} if you wish to throw an {@code IllegalArgumentException}
     * on an assertion failure.
     * <pre class="code">
     * Assert.state(entity.getId() == null,
     *     () -&gt; "ID for entity " + entity.getName() + " must not already be initialized");
     * </pre>
     *
     * @param expression      a boolean expression
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalStateException if {@code expression} is {@code false}
     * @since 5.0
     */
    public static void state(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     *
     * @param expression a boolean expression
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     */
    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert a boolean expression, throwing an {@code IllegalArgumentException}
     * if the expression evaluates to {@code false}.
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, () -&gt; "The value '" + i + "' must be greater than zero");
     * </pre>
     *
     * @param expression      a boolean expression
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if {@code expression} is {@code false}
     * @since 5.0
     */
    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an object is {@code null}.
     * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(@Nullable Object object, String message, Object... args) {
        if (object != null) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that an object is {@code null}.
     * <pre class="code">
     * Assert.isNull(value, () -&gt; "The value '" + value + "' must be null");
     * </pre>
     *
     * @param object          the object to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     * @since 5.0
     */
    public static void isNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void notNull(@Nullable Object object, String message, Object... args) {
        if (object == null) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre class="code">
     * Assert.notNull(entity.getId(),
     *     () -&gt; "ID for entity " + entity.getName() + " must not be null");
     * </pre>
     *
     * @param object          the object to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     * @since 5.0
     */
    public static void notNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that the given String is not empty; that is,
     * it must not be {@code null} and not the empty String.
     * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
     *
     * @param text    the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text is empty
     * @see StringUtils#hasLength
     */
    public static void hasLength(@Nullable String text, String message, Object... args) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that the given String is not empty; that is,
     * it must not be {@code null} and not the empty String.
     * <pre class="code">
     * Assert.hasLength(account.getName(),
     *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
     * </pre>
     *
     * @param text            the String to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the text is empty
     * @see StringUtils#hasLength
     * @since 5.0
     */
    public static void hasLength(@Nullable String text, Supplier<String> messageSupplier) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that the given String contains valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text    the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text does not contain valid text content
     * @see StringUtils#hasText
     */
    public static void hasText(@Nullable String text, String message, Object... args) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that the given String contains valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">
     * Assert.hasText(account.getName(),
     *     () -&gt; "Name for account '" + account.getId() + "' must not be empty");
     * </pre>
     *
     * @param text            the String to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the text does not contain valid text content
     * @see StringUtils#hasText
     * @since 5.0
     */
    public static void hasText(@Nullable String text, Supplier<String> messageSupplier) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that the given text does not contain the given substring.
     * <pre class="code">Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");</pre>
     *
     * @param textToSearch the text to search
     * @param substring    the substring to find within the text
     * @param message      the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text contains the substring
     */
    public static void doesNotContain(@Nullable String textToSearch, String substring, String message, Object... args) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
                textToSearch.contains(substring)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that the given text does not contain the given substring.
     * <pre class="code">
     * Assert.doesNotContain(name, forbidden, () -&gt; "Name must not contain '" + forbidden + "'");
     * </pre>
     *
     * @param textToSearch    the text to search
     * @param substring       the substring to find within the text
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the text contains the substring
     * @since 5.0
     */
    public static void doesNotContain(@Nullable String textToSearch, String substring, Supplier<String> messageSupplier) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) &&
                textToSearch.contains(substring)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre class="code">Assert.notEmpty(array, "The array must contain elements");</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     */
    public static void notEmpty(@Nullable Object[] array, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that an array contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre class="code">
     * Assert.notEmpty(array, () -&gt; "The " + arrayType + " array must contain elements");
     * </pre>
     *
     * @param array           the array to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
     * @since 5.0
     */
    public static void notEmpty(@Nullable Object[] array, Supplier<String> messageSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that an array contains no {@code null} elements.
     * <p>Note: Does not complain if the array is empty!
     * <pre class="code">Assert.noNullElements(array, "The array must contain non-null elements");</pre>
     *
     * @param array   the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
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
     * Assert that an array contains no {@code null} elements.
     * <p>Note: Does not complain if the array is empty!
     * <pre class="code">
     * Assert.noNullElements(array, () -&gt; "The " + arrayType + " array must contain non-null elements");
     * </pre>
     *
     * @param array           the array to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     * @since 5.0
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
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre class="code">Assert.notEmpty(collection, "Collection must contain elements");</pre>
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     */
    public static void notEmpty(@Nullable Collection<?> collection, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that a collection contains elements; that is, it must not be
     * {@code null} and must contain at least one element.
     * <pre class="code">
     * Assert.notEmpty(collection, () -&gt; "The " + collectionType + " collection must contain elements");
     * </pre>
     *
     * @param collection      the collection to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or
     *                                  contains no elements
     * @since 5.0
     */
    public static void notEmpty(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that a collection contains no {@code null} elements.
     * <p>Note: Does not complain if the collection is empty!
     * <pre class="code">Assert.noNullElements(collection, "Collection must contain non-null elements");</pre>
     *
     * @param collection the collection to check
     * @param message    the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection contains a {@code null} element
     * @since 5.2
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
     * Assert that a collection contains no {@code null} elements.
     * <p>Note: Does not complain if the collection is empty!
     * <pre class="code">
     * Assert.noNullElements(collection, () -&gt; "Collection " + collectionName + " must contain non-null elements");
     * </pre>
     *
     * @param collection      the collection to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the collection contains a {@code null} element
     * @since 5.2
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
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <pre class="code">Assert.notEmpty(map, "Map must contain entries");</pre>
     *
     * @param map     the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     */
    public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * Assert that a Map contains entries; that is, it must not be {@code null}
     * and must contain at least one entry.
     * <pre class="code">
     * Assert.notEmpty(map, () -&gt; "The " + mapType + " map must contain entries");
     * </pre>
     *
     * @param map             the map to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or contains no entries
     * @since 5.0
     */
    public static void notEmpty(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * <pre class="code">Assert.instanceOf(Foo.class, foo, "Foo expected");</pre>
     *
     * @param type    the type to check against
     * @param obj     the object to check
     * @param message a message which will be prepended to provide further context.
     *                If it is empty or ends in ":" or ";" or "," or ".", a full exception message
     *                will be appended. If it ends in a space, the name of the offending object's
     *                type will be appended. In any other case, a ":" with a space and the name
     *                of the offending object's type will be appended.
     * @throws IllegalArgumentException if the object is not an instance of type
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj, String message, Object... args) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, StringUtils.format(message, args));
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo, () -&gt; "Processing " + Foo.class.getSimpleName() + ":");
     * </pre>
     *
     * @param type            the type to check against
     * @param obj             the object to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails. See {@link #isInstanceOf(Class, Object, String, Object...)} for details.
     * @throws IllegalArgumentException if the object is not an instance of type
     * @since 5.0
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj, Supplier<String> messageSupplier) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
     *
     * @param type the type to check against
     * @param obj  the object to check
     * @throws IllegalArgumentException if the object is not an instance of type
     */
    public static void isInstanceOf(Class<?> type, @Nullable Object obj) {
        isInstanceOf(type, obj, "");
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * <pre class="code">Assert.isAssignable(Number.class, myClass, "Number expected");</pre>
     *
     * @param superType the supertype to check against
     * @param subType   the subtype to check
     * @param message   a message which will be prepended to provide further context.
     *                  If it is empty or ends in ":" or ";" or "," or ".", a full exception message
     *                  will be appended. If it ends in a space, the name of the offending subtype
     *                  will be appended. In any other case, a ":" with a space and the name of the
     *                  offending subtype will be appended.
     * @throws IllegalArgumentException if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, @Nullable Class<?> subType, String message, Object... args) {
        notNull(superType, "Supertype to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, StringUtils.format(message, args));
        }
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass, () -&gt; "Processing " + myAttributeName + ":");
     * </pre>
     *
     * @param superType       the supertype to check against
     * @param subType         the subtype to check
     * @param messageSupplier a supplier for the exception message to use if the
     *                        assertion fails. See {@link #isAssignable(Class, Class, String, Object...)} for details.
     * @throws IllegalArgumentException if the classes are not assignable
     * @since 5.0
     */
    public static void isAssignable(Class<?> superType, @Nullable Class<?> subType, Supplier<String> messageSupplier) {
        notNull(superType, "Supertype to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
        }
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
     *
     * @param superType the supertype to check
     * @param subType   the subtype to check
     * @throws IllegalArgumentException if the classes are not assignable
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
     * 条件为真
     *
     * @param expression 条件表达式
     * @param t          异常对象
     * @param <T>        异常类型
     */
    public static <T extends RuntimeException> void isTrue(boolean expression, T t) {
        if (!expression) {
            throw t;
        }
    }

    /**
     * 条件为假
     *
     * @param expression 条件表达式
     * @param message    异常信息
     * @param args       异常信息参数
     */
    public static void isFalse(boolean expression, String message, Object... args) {
        if (expression) {
            throw new IllegalArgumentException(StringUtils.format(message, args));
        }
    }

    /**
     * 条件为假
     *
     * @param expression 条件表达式
     * @param t          异常对象
     * @param <T>        异常类型
     */
    public static <T extends RuntimeException> void isFalse(boolean expression, T t) {
        if (expression) {
            throw t;
        }
    }

    /**
     * 对象为空
     *
     * @param object 对象
     * @param t      异常对象
     * @param <T>    异常类型
     */
    public static <T extends RuntimeException> void isNull(@Nullable Object object, T t) {
        if (null != object) {
            throw t;
        }
    }

    /**
     * 对象不为空
     *
     * @param object 对象
     * @param t      异常对象
     * @param <T>    异常类型
     */
    public static <T extends RuntimeException> void notNull(@Nullable Object object, T t) {
        if (null == object) {
            throw t;
        }
    }

    /**
     * 文本有长度
     *
     * @param text 文本
     * @param t    异常对象
     * @param <T>  异常类型
     */
    public static <T extends RuntimeException> void hasLength(@Nullable String text, T t) {
        if (!StringUtils.hasLength(text)) {
            throw t;
        }
    }

    /**
     * 文本有数据
     *
     * @param text 文本
     * @param t    异常对象
     * @param <T>  异常类型
     */
    public static <T extends RuntimeException> void hasText(@Nullable String text, T t) {
        if (!StringUtils.hasText(text)) {
            throw t;
        }
    }

    /**
     * 文本不包含
     *
     * @param textToSearch 全文
     * @param substring    子串
     * @param t            异常对象
     * @param <T>          异常类型
     */
    public static <T extends RuntimeException> void doesNotContain(@Nullable String textToSearch, String substring, T t) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw t;
        }
    }

    /**
     * 对象数组不为空
     *
     * @param array 对象数组
     * @param t     异常对象
     * @param <T>   异常类型
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Object[] array, T t) {
        if (ObjectUtils.isEmpty(array)) {
            throw t;
        }
    }

    /**
     * 对象数组不包含空节点
     *
     * @param array 对象数组
     * @param t     异常对象
     * @param <T>   异常类型
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
     * 集合不为空
     *
     * @param collection 集合
     * @param t          异常对象
     * @param <T>        异常类型
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Collection<?> collection, T t) {
        if (CollectionUtils.isEmpty(collection)) {
            throw t;
        }
    }

    /**
     * 集合不包含空节点
     *
     * @param collection 集合
     * @param t          异常对象
     * @param <T>        异常类型
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
     * Map 集合不为空
     *
     * @param map 集合
     * @param t   异常对象
     * @param <T> 异常类型
     */
    public static <T extends RuntimeException> void notEmpty(@Nullable Map<?, ?> map, T t) {
        if (MapUtils.isEmpty(map)) {
            throw t;
        }
    }

    /**
     * 实例所属类判断
     *
     * @param type 类
     * @param obj  实例
     * @param t    异常对象
     * @param <T>  异常类型
     */
    public static <T extends RuntimeException> void isInstanceOf(Class<?> type, @Nullable Object obj, T t) {
        notNull(type, t);
        if (!type.isInstance(obj)) {
            throw t;
        }
    }

    /**
     * 类继承关系判断
     *
     * @param superType 父类
     * @param subType   子类
     * @param t         异常对象
     * @param <T>       异常类型
     */
    public static <T extends RuntimeException> void isAssignable(Class<?> superType, @Nullable Class<?> subType, T t) {
        notNull(superType, t);
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw t;
        }
    }

    /**
     * 主动抛出一个异常
     *
     * @param t   异常对象
     * @param <T> 异常类型
     */
    public static <T extends RuntimeException> void thr(T t) {
        throw t;
    }

    /**
     * 主动抛出一个异常
     *
     * @param message 异常信息
     * @param args    异常信息参数
     */
    public static void illegalState(String message, Object... args) {
        throw new IllegalStateException(StringUtils.format(message, args));
    }

    /**
     * 主动抛出一个异常
     *
     * @param message 异常信息
     * @param args    异常信息参数
     */
    public static void illegalArguments(String message, Object... args) {
        throw new IllegalArgumentException(StringUtils.format(message, args));
    }

    /**
     * 包裹异常并转换为运行时异常
     *
     * @param wrapper  包裹器
     * @param function 异常处理函数
     * @param <E>      异常类型
     */
    public static <E extends RuntimeException> void wrap(ThrowableWrapper wrapper, Function<Throwable, E> function) {
        try {
            wrapper.wrap();
        } catch (Throwable e) {
            throw function.apply(e);
        }
    }

    @FunctionalInterface
    public interface ThrowableWrapper {

        /**
         * 包裹异常
         *
         * @throws Throwable 异常
         */
        void wrap() throws Throwable;
    }
}
