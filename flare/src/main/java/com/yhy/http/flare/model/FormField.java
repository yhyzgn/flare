package com.yhy.http.flare.model;

import com.yhy.http.flare.utils.Opt;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.InputStream;

/**
 * 抽象表单字段。
 * <p>
 * 表示一个待编码的表单/查询字段的名称、值与元信息（编码标记、默认值、文件名），
 * 由 {@code FormFieldConverter} 产出、{@code RequestBuilder} 消费。
 *
 * @param <T> 字段值类型
 * <p>
 * Created on 2025-09-15 15:59
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public abstract class FormField<T> {

    /**
     * 字段名称。
     */
    private String name;

    /**
     * 字段值。
     */
    private T value;

    /**
     * 值是否已完成 URL 编码。
     */
    private boolean encoded;

    /**
     * 值为 {@code null} 时使用的默认值。
     */
    private String defaultValue;

    /**
     * 文件字段的上传文件名。
     */
    private String filename;

    /**
     * 普通字符串值字段。
     * <p>
     * 用于查询参数与普通表单字段。
     */
    public static class ValueFormField extends FormField<String> {

        /**
         * 创建 ValueFormField 实例。
         *
         * @param name         字段名称
         * @param value         字段值
         * @param encoded       值是否已完成 URL 编码
         * @param defaultValue  值为 {@code null} 时的默认值
         */
        public ValueFormField(String name, String value, boolean encoded, String defaultValue) {
            super(name, value, encoded, defaultValue, null);
        }
    }

    /**
     * 带文件名的多行字段。
     * <p>
     * multipart 表单中文件上传字段的公共基类。
     *
     * @param <T> 字段值类型
     */
    public static class MultilineFormField<T> extends FormField<T> {

        /**
         * 创建 MultilineFormField 实例。
         *
         * @param name     字段名称
         * @param value     字段值
         * @param filename 上传文件名
         */
        public MultilineFormField(String name, T value, String filename) {
            super(name, value, false, null, filename);
        }
    }

    /**
     * {@link File} 类型的 multipart 字段。
     */
    public static class FileFormField extends MultilineFormField<File> {

        /**
         * 创建 FileFormField 实例。
         *
         * @param name     字段名称
         * @param value    上传的文件
         * @param filename 上传文件名，为 {@code null} 时取文件自身的名称
         */
        public FileFormField(String name, File value, String filename) {
            super(name, value, Opt.ofNullable(filename).orElse(value.getName()));
        }
    }

    /**
     * {@code byte[]} 类型的 multipart 字段。
     */
    public static class BytesFormField extends MultilineFormField<byte[]> {

        /**
         * 创建 BytesFormField 实例。
         *
         * @param name     字段名称
         * @param value    上传的字节数组
         * @param filename 上传文件名
         */
        public BytesFormField(String name, byte[] value, String filename) {
            super(name, value, filename);
        }
    }

    /**
     * {@link InputStream} 类型的 multipart 字段。
     * <p>
     * 底层流只能读取一次，编码为一次性（one-shot）请求体。
     */
    public static class InputStreamFormField extends MultilineFormField<InputStream> {

        /**
         * 创建 InputStreamFormField 实例。
         *
         * @param name     字段名称
         * @param value    上传的输入流
         * @param filename 上传文件名
         */
        public InputStreamFormField(String name, InputStream value, String filename) {
            super(name, value, filename);
        }
    }
}
