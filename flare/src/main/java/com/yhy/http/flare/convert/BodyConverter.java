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
 * 转换器接口
 * <p>
 * Created on 2025-09-10 15:51
 *
 * @param <F> 源对象类型
 * @param <T> 目标对象类型
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BodyConverter<F, T> {

    /**
     * 转换
     *
     * @param from 源对象
     * @return 目标对象
     * @throws IOException 转换异常
     */
    T convert(F from) throws IOException;

    /**
     * 源对象类型
     *
     * @return 源对象类型
     */
    Class<?> resultType();

    /**
     * 目标对象类型
     *
     * @param from     源对象
     * @param function 转换函数
     * @return 目标对象类型
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
     * 转换工厂接口
     */
    interface Factory {

        /**
         * 请求体转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare       Flare
         * @return 请求体转换器
         */
        BodyConverter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations, Flare flare);

        /**
         * 响应体转换器
         *
         * @param type        类型
         * @param annotations 注解
         * @param flare       Flare
         * @return 响应体转换器
         */
        BodyConverter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Flare flare);
    }

    @FunctionalInterface
    interface SilenceFunction<T, R, E extends Exception> {

        /**
         * 执行函数
         *
         * @param t 参数
         * @return 结果
         * @throws E 异常
         */
        R apply(T t) throws E;
    }
}
