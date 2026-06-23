package com.yhy.http.flare.model;

import com.yhy.http.flare.utils.Opt;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.InputStream;

/**
 * 表单字段值信息
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

    private String name;

    private T value;

    private boolean encoded;

    private String defaultValue;

    private String filename;

    /**
     * Value Form Field类。
     *
     */
    public static class ValueFormField extends FormField<String> {

        /**
         * 创建 ValueFormField 实例。
         *
         * @param name 字符串
         * @param value 字符串
         * @param encoded 值
         * @param defaultValue 字符串
         */
        public ValueFormField(String name, String value, boolean encoded, String defaultValue) {
            super(name, value, encoded, defaultValue, null);
        }
    }

    /**
     * Multiline Form Field类。
     *
     */
    public static class MultilineFormField<T> extends FormField<T> {

        /**
         * 创建 MultilineFormField 实例。
         *
         * @param name 字符串
         * @param value 值
         * @param filename 字符串
         */
        public MultilineFormField(String name, T value, String filename) {
            super(name, value, false, null, filename);
        }
    }

    /**
     * File Form Field类。
     *
     */
    public static class FileFormField extends MultilineFormField<File> {

        /**
         * 创建 FileFormField 实例。
         *
         * @param name 字符串
         * @param value 文件
         * @param filename 字符串
         */
        public FileFormField(String name, File value, String filename) {
            super(name, value, Opt.ofNullable(filename).orElse(value.getName()));
        }
    }

    /**
     * Bytes Form Field类。
     *
     */
    public static class BytesFormField extends MultilineFormField<byte[]> {

        /**
         * 创建 BytesFormField 实例。
         *
         * @param name 字符串
         * @param value 字节数组
         * @param filename 字符串
         */
        public BytesFormField(String name, byte[] value, String filename) {
            super(name, value, filename);
        }
    }

    /**
     * Input Stream Form Field类。
     *
     */
    public static class InputStreamFormField extends MultilineFormField<InputStream> {

        /**
         * 创建 InputStreamFormField 实例。
         *
         * @param name 字符串
         * @param value 输入流
         * @param filename 字符串
         */
        public InputStreamFormField(String name, InputStream value, String filename) {
            super(name, value, filename);
        }
    }
}
