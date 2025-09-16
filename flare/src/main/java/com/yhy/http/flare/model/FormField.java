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

    public static class ValueFormField extends FormField<String> {

        public ValueFormField(String name, String value, boolean encoded, String defaultValue) {
            super(name, value, encoded, defaultValue, null);
        }
    }

    public static class MultilineFormField<T> extends FormField<T> {

        public MultilineFormField(String name, T value, String filename) {
            super(name, value, false, null, filename);
        }
    }

    public static class FileFormField extends MultilineFormField<File> {

        public FileFormField(String name, File value, String filename) {
            super(name, value, Opt.ofNullable(filename).orElse(value.getName()));
        }
    }

    public static class BytesFormField extends MultilineFormField<byte[]> {

        public BytesFormField(String name, byte[] value, String filename) {
            super(name, value, filename);
        }
    }

    public static class InputStreamFormField extends MultilineFormField<InputStream> {

        public InputStreamFormField(String name, InputStream value, String filename) {
            super(name, value, filename);
        }
    }
}
