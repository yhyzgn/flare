package com.yhy.http.flare.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.utils.DownloadFileUtils;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 请求体 / 响应体转换器接口。
 * <p>
 * 请求方向把业务对象转换为 OkHttp {@link RequestBody}，响应方向把 {@link ResponseBody}
 * 转换为代理方法声明的返回类型。
 *
 * @param <F> 源对象类型
 * @param <T> 目标对象类型
 * <p>
 * Created on 2025-09-10 15:51
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BodyConverter<F, T> {

    /**
     * 转换源对象为目标对象。
     *
     * @param from 源对象
     * @return 转换结果
     * @throws IOException 转换过程中发生 I/O 错误
     */
    T convert(F from) throws IOException;

    /**
     * 获取本转换器声明的目标对象类型。
     * <p>
     * 用于 {@link #responseBodyResolve(ResponseBody, Annotation[], SilenceFunction, StringConverter)}
     * 分派到 String / File / InputStream 等内置类型分支。
     *
     * @return 目标对象类型
     */
    Class<?> resultType();

    /**
     * 解析响应体为内置类型或自定义类型。
     * <p>
     * 按 {@link #resultType()} 分派：{@code String} 直接读为字符串；{@code File} 结合 {@code @Download}
     * 注解（无注解时落盘为临时文件）；{@code InputStream} / {@code Reader} / {@code byte[]} / {@code void}
     * 在有 {@code @Download} 时先保存为文件再包装返回，无注解时直接返回流或字节；其余类型交给
     * {@code function} 自定义转换。
     *
     * @param from            原始响应体
     * @param annotations      方法与参数上的注解（用于查找 {@link Download}）
     * @param function         内置类型不匹配时的自定义转换函数
     * @param stringConverter  {@code @Download} 路径等字符串值的转换器
     * @return 转换后的目标对象（{@code void} 类型返回 {@code null}）
     * @throws IOException 读取响应体或写文件失败
     */
    @SuppressWarnings("unchecked")
    default T responseBodyResolve(ResponseBody from, Annotation[] annotations, SilenceFunction<ResponseBody, T, IOException> function, StringConverter<String> stringConverter) throws IOException {
        Class<?> type = resultType();
        if (type == String.class) {
            return (T) from.string();
        }
        if (type == ResponseBody.class) {
            return (T) from;
        }

        // 处理注解
        // File 类型，则需要检查是否有 @Download 注解，如果没有则保存为临时文件
        if (type == File.class) {
            Download download = (Download) Arrays.stream(annotations).filter(a -> a instanceof Download).findFirst().orElse(null);
            return (T) DownloadFileUtils.write(download, from.byteStream(), stringConverter);
        }

        // 处理 InputStream、 Reader、 byte[] 和 void 类型，如果有 @Download 注解则才存为文件，否则直接返回
        if (InputStream.class.isAssignableFrom(type) || Reader.class.isAssignableFrom(type) || type == byte[].class || type == void.class) {
            Download download = (Download) Arrays.stream(annotations).filter(a -> a instanceof Download).findFirst().orElse(null);
            // 未指定下载文件，则直接返回
            if (null == download) {
                if (InputStream.class.isAssignableFrom(type)) {
                    return (T) from.byteStream();
                }
                if (Reader.class.isAssignableFrom(type)) {
                    return (T) from.charStream();
                }
                if (type == byte[].class) {
                    return (T) from.bytes();
                }
                // void 类型，则不返回值
                return null;
            }

            // 确认指定为下载文件，保存为文件
            File file = DownloadFileUtils.write(download, from.byteStream(), stringConverter);
            if (InputStream.class.isAssignableFrom(type)) {
                return (T) new FileInputStream(file);
            }
            if (Reader.class.isAssignableFrom(type)) {
                return (T) new FileReader(file);
            }
            if (type == byte[].class) {
                return (T) FileUtils.readFileToByteArray(file);
            }
            // void 类型，则不返回值
            return null;
        }

        // 处理其他类型
        return function.apply(from);
    }

    /**
     * 转换器工厂接口。
     * <p>
     * 按目标类型和注解创建请求方向或响应方向的转换器实例。
     */
    interface Factory {

        /**
         * 创建请求体转换器。
         *
         * @param type        请求体对象类型
         * @param annotations 参数上的注解
         * @param flare       当前 Flare 实例
         * @return 把业务对象转换为 {@link RequestBody} 的转换器
         */
        BodyConverter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations, Flare flare);

        /**
         * 创建响应体转换器。
         *
         * @param type        响应体目标类型
         * @param annotations 方法上的注解
         * @param flare       当前 Flare 实例
         * @return 把 {@link ResponseBody} 转换为目标类型的转换器
         */
        BodyConverter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare);
    }

    /**
     * 静默函数接口。
     * <p>
     * 允许抛出受检异常的函数式接口，供 {@link #responseBodyResolve} 的自定义转换分支使用。
     *
     * @param <T> 入参类型
     * @param <R> 返回值类型
     * @param <E> 可抛出的受检异常类型
     */
    @FunctionalInterface
    interface SilenceFunction<T, R, E extends Exception> {

        /**
         * 执行函数。
         *
         * @param t 入参
         * @return 函数结果
         * @throws E 执行中抛出的受检异常
         */
        R apply(T t) throws E;
    }
}
