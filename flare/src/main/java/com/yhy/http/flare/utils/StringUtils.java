package com.yhy.http.flare.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 字符串工具类。
 * <p>
 * 提供空值 / 空白判定、SLF4J 风格的 {@code {}} 与 {@code {key}} 占位符格式化、
 * 驼峰转下划线、trim 系列与 UTF-8 编解码等常用能力。
 * <p>
 * Created on 2025-09-10 14:21
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class StringUtils {

    /**
     * 防止工具类被实例化。
     */
    private StringUtils() {
        throw new UnsupportedOperationException("This class is not for instantiation");
    }

    /**
     * 判断字符串是否包含文本内容（非 {@code null}、非空且含非空白字符）。
     *
     * @param str 待检查字符串
     * @return {@code true} 表示含有效文本
     */
    public static boolean hasText(@Nullable CharSequence str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    /**
     * 判断字符串中是否至少存在一个非空白字符。
     *
     * @param str 待检查字符串，保证非 {@code null} 且非空
     * @return {@code true} 表示含非空白字符
     */
    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否为 {@code null} 或空串。
     *
     * @param str 待检查字符串
     * @return {@code true} 表示为 {@code null} 或空串
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为 {@code null}、空串或全空白字符。
     *
     * @param str 待检查字符串
     * @return {@code true} 表示为空白
     */
    public static boolean isBlank(CharSequence str) {
        return str == null || str.toString().trim().isEmpty();
    }

    /**
     * 判断字符串非空（非 {@code null} 且长度大于 0），即 {@link #isEmpty(CharSequence)} 的取反。
     *
     * @param str 待检查字符串
     * @return {@code true} 表示非空
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串非空白，即 {@link #isBlank(CharSequence)} 的取反。
     *
     * @param str 待检查字符串
     * @return {@code true} 表示含非空白内容
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否有长度（不为 null 或空）
     *
     * @param str 字符串
     * @return 是否有长度
     */
    public static boolean hasLength(@Nullable CharSequence str) {
        return (str != null && !str.isEmpty());  // as of JDK 15
    }

    // ==================== 常用处理 ====================

    /**
     * 把 {@code null} 转为空串，非 {@code null} 原样返回。
     *
     * @param str 待转换字符串
     * @return {@code null} 时返回 {@code ""}，否则返回原值
     */
    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 把字符串重复指定次数。
     *
     * @param str   待重复字符串
     * @param count 重复次数
     * @return 重复结果；{@code str} 为 {@code null} 或 {@code count <= 0} 时返回空串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) return "";
        return str.repeat(count);
    }

    /**
     * 把集合元素按分隔符拼接为字符串。
     *
     * @param coll      待拼接集合，元素按 {@code toString} 转换
     * @param delimiter 元素之间的分隔符
     * @return 拼接结果；集合为 {@code null} 或空时返回空串
     */
    public static String join(Collection<?> coll, String delimiter) {
        if (coll == null || coll.isEmpty()) return "";
        return String.join(delimiter, coll.stream().map(Objects::toString).toList());
    }

    // ==================== 动态占位符格式化 ====================
    // 类似 SLF4J: "Hello {}, 我是 {}，今年 {} 岁"

    /**
     * 使用 SLF4J 风格的 {@code {}} 占位符格式化字符串。
     * <p>
     * 占位符按顺序被 {@code args} 替换；参数不足时占位符原样保留，
     * 多余参数被忽略。
     *
     * @param template 模板字符串，含 {@code {}} 占位符
     * @param args     依序替换占位符的参数
     * @return 格式化结果；模板或参数为 {@code null} 时原样返回模板
     */
    public static String format(String template, Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }

        // 提前转换参数为字符串，避免重复调用 objectToString
        String[] strArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            strArgs[i] = objectToString(args[i]);
        }

        char[] chars = template.toCharArray();
        StringBuilder sb = getSb(template, chars, strArgs);
        return sb.toString();
    }

    /**
     * 使用 {@code {key}} 占位符格式化字符串，支持 Map 参数。
     * <p>
     * 示例：format("Hello {name}, 你有 {count} 条消息", Map.of("name", "张三", "count", 5))
     * 返回：Hello 张三, 你有 5 条消息。
     * 值为 {@code null} 时替换为 {@code "null"}；key 不存在时保留原占位符。
     *
     * @param template 模板字符串，含 {@code {key}} 占位符，支持嵌套大括号
     * @param params   参数 Map，key 为占位符名称，value 为替换值
     * @return 格式化后的字符串；模板或参数为空时原样返回模板
     */
    public static String format(String template, Map<String, Object> params) {
        if (template == null || template.isEmpty() || params == null || params.isEmpty()) {
            return template;
        }
        return formatInternal(template, params, true);
    }

    /**
     * 内部递归解析方法，支持嵌套大括号。
     *
     * @param template 模板
     * @param params   参数 map
     * @param topLevel 是否为顶层调用（用于决定是否去掉最外层大括号的启发式规则）
     * @return 解析后的字符串
     */
    private static String formatInternal(String template, Map<String, Object> params, boolean topLevel) {
        StringBuilder sb = new StringBuilder(template.length() + 16);
        int len = template.length();
        for (int i = 0; i < len; ) {
            char c = template.charAt(i);
            if (c == '{') {
                int j = i + 1;
                int depth = 1;
                // 找到匹配的闭合 '}'，支持嵌套
                while (j < len && depth > 0) {
                    char cc = template.charAt(j);
                    if (cc == '{') depth++;
                    else if (cc == '}') depth--;
                    j++;
                }
                if (depth > 0) {
                    // 未找到匹配的 '}'，当作普通字符处理
                    sb.append(c);
                    i++;
                    continue;
                }
                int endIndex = j - 1; // 闭合 '}' 的索引
                String inner = template.substring(i + 1, endIndex);
                // 如果 inner 不包含嵌套的大括号，则视为 key，否则递归处理内部占位符
                if (inner.indexOf('{') < 0 && inner.indexOf('}') < 0) {
                    String key = inner.trim();
                    if (!key.isEmpty()) {
                        if (params.containsKey(key)) {
                            Object value = params.get(key);
                            sb.append(value == null ? "null" : value.toString());
                        } else {
                            // key 不存在，保留原占位符
                            sb.append('{').append(inner).append('}');
                        }
                    } else {
                        // 空的占位符，保留
                        sb.append("{}");
                    }
                } else {
                    // 递归解析内部内容
                    String replacedInner = formatInternal(inner, params, false);
                    // 启发式规则：若为顶层且整个模板被一对大括号包裹且内部看起来不是 JSON（没有 ':'），则去掉最外层大括号
                    boolean isWholeWrapped = topLevel && i == 0 && endIndex == len - 1;
                    boolean looksLikeJson = replacedInner.indexOf(':') >= 0;
                    if (isWholeWrapped && !looksLikeJson) {
                        sb.append(replacedInner);
                    } else {
                        sb.append('{').append(replacedInner).append('}');
                    }
                }
                i = j; // 跳过已经处理的部分（包括闭合 '}'）
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static @NotNull StringBuilder getSb(String template, char[] chars, String[] strArgs) {
        StringBuilder sb = new StringBuilder(template.length() + 16);

        int argIndex = 0;
        int i = 0;
        while (i < chars.length) {
            if (chars[i] == '{' && i + 1 < chars.length && chars[i + 1] == '}') {
                // 匹配到占位符
                if (argIndex < strArgs.length) {
                    sb.append(strArgs[argIndex++]);
                } else {
                    sb.append("{}"); // 参数不够
                }
                i += 2; // 跳过 {}
            } else {
                sb.append(chars[i]);
                i++;
            }
        }
        return sb;
    }

    /**
     * 把对象转为字符串，支持 {@code null} 与数组。
     *
     * @param obj 待转换对象
     * @return {@code null} 时返回 {@code "null"}；数组去除外层方括号后返回，其他返回 {@code toString}
     */
    private static String objectToString(Object obj) {
        if (obj == null) return "null";
        if (obj.getClass().isArray()) {
            // 简单数组处理
            return Arrays.deepToString(new Object[]{obj}).replaceFirst("^\\[", "").replaceFirst("]$", "");
        }
        return obj.toString();
    }

    // ==================== 额外方法示例 ====================

    /**
     * 把字符串编码为 UTF-8 字节数组。
     *
     * @param str 待编码字符串
     * @return UTF-8 字节；{@code str} 为 {@code null} 时返回空数组
     */
    public static byte[] toUtf8Bytes(String str) {
        return str == null ? new byte[0] : str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 判断两个字符串相等（忽略大小写）。
     *
     * @param a 字符串一，可为 {@code null}
     * @param b 字符串二，可为 {@code null}
     * @return 两者同为 {@code null} 或忽略大小写相等时为 {@code true}
     */
    public static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    /**
     * 判断字符串是否全为数字字符。
     * <p>
     * 使用 {@link Character#isDigit(char)}，含非 ASCII 数字（如全角数字）也会被判为数字。
     *
     * @param str 待检查字符串
     * @return 非 {@code null} / 空白且所有字符均为数字时为 {@code true}
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) return false;
        return str.chars().allMatch(Character::isDigit);
    }

    /**
     * 判断单个字符是否为 ASCII 数字字符（{@code '0'} 到 {@code '9'}）。
     *
     * @param ch 待检查字符
     * @return 是 ASCII 数字时为 {@code true}
     */
    public static boolean isNumeric(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 把驼峰命名转换为下划线命名（如 {@code userName} 转为 {@code user_name}）。
     * <p>
     * 连续大写按专有名词处理，如 {@code myURL} 转为 {@code my_URL}。
     *
     * @param text 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String toUnderlineCase(String text) {
        return toSymbolCase(text, '_');
    }

    /**
     * 把驼峰命名转换为指定符号分隔的命名。
     * <p>
     * 在大写字母处插入 {@code symbol} 并视上下文决定是否转小写；
     * 连续大写视为专有名词保持原样。
     *
     * @param str    驼峰命名字符串
     * @param symbol 词间分隔符号，如 {@code '_'} 或 {@code '-'}
     * @return 符号分隔的命名；输入为 {@code null} 时返回 {@code null}
     */
    public static String toSymbolCase(CharSequence str, char symbol) {
        if (str == null) {
            return null;
        }

        final int length = str.length();
        final StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                final Character preChar = (i > 0) ? str.charAt(i - 1) : null;
                final Character nextChar = (i < str.length() - 1) ? str.charAt(i + 1) : null;

                if (null != preChar) {
                    if (symbol == preChar) {
                        // 前一个为分隔符
                        if (null == nextChar || Character.isLowerCase(nextChar)) {
                            // 普通首字母大写，如_Abb -> _abb
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如_AB -> _AB
                    } else if (Character.isLowerCase(preChar)) {
                        // 前一个为小写
                        sb.append(symbol);
                        if (null == nextChar || Character.isLowerCase(nextChar) || isNumeric(nextChar)) {
                            // 普通首字母大写，如aBcc -> a_bcc
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如aBC -> a_BC
                    } else {
                        // 前一个为大写
                        if (null != nextChar && Character.isLowerCase(nextChar)) {
                            // 普通首字母大写，如ABcc -> A_bcc
                            sb.append(symbol);
                            c = Character.toLowerCase(c);
                        }
                        // 后一个为大写，按照专有名词对待，如ABC -> ABC
                    }
                } else {
                    // 首字母，需要根据后一个判断是否转为小写
                    if (null == nextChar || Character.isLowerCase(nextChar)) {
                        // 普通首字母大写，如Abc -> abc
                        c = Character.toLowerCase(c);
                    }
                    // 后一个为大写，按照专有名词对待，如ABC -> ABC
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 把 UTF-8 字节数组解码为字符串。
     *
     * @param data UTF-8 编码的字节数组
     * @return 解码后的字符串
     */
    public static String utf8Str(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 去除字符串开头的空白字符
     *
     * @param str 字符串
     * @return 去除开头空白后的字符串
     */
    public static String trimStart(String str) {
        CharSequence charSequence = trimStart((CharSequence) str);
        return (charSequence == null) ? null : charSequence.toString();
    }

    /**
     * 去除字符串末尾的空白字符
     *
     * @param str 字符串
     * @return 去除末尾空白后的字符串
     */
    public static String trimEnd(String str) {
        CharSequence charSequence = trimEnd((CharSequence) str);
        return (charSequence == null) ? null : charSequence.toString();
    }

    /**
     * 去除字符串两端的空白字符
     *
     * @param str 字符串
     * @return 去除两端空白后的字符串
     */
    public static String trim(String str) {
        CharSequence charSequence = trim((CharSequence) str);
        return (charSequence == null) ? null : charSequence.toString();
    }

    /**
     * 去除字符串开头的空白字符
     *
     * @param cs 字符串
     * @return 去除开头空白后的字符串
     */
    public static CharSequence trimStart(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        int i = 0;
        while (i < cs.length() && Character.isWhitespace(cs.charAt(i))) {
            i++;
        }
        return cs.subSequence(i, cs.length());
    }

    /**
     * 去除字符串末尾的空白字符
     *
     * @param cs 字符串
     * @return 去除末尾空白后的字符串
     */
    public static CharSequence trimEnd(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        int i = cs.length() - 1;
        while (i >= 0 && Character.isWhitespace(cs.charAt(i))) {
            i--;
        }
        return cs.subSequence(0, i + 1);
    }

    /**
     * 去除字符串两端的空白字符
     *
     * @param cs 字符串
     * @return 去除两端空白后的字符串
     */
    public static CharSequence trim(CharSequence cs) {
        if (null == cs) {
            return null;
        }
        return trimStart(trimEnd(cs));
    }

    /**
     * 判断字符串是否为可识别的布尔字面量（忽略大小写的 {@code "true"} / {@code "false"}）。
     *
     * @param text 待检查字符串
     * @return 是布尔字面量时为 {@code true}
     */
    public static boolean isBoolean(String text) {
        return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
    }
}
