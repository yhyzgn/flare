package com.yhy.http.flare.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;

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

    public static class ValueFormField extends FormField<String> {

        public ValueFormField(String name, String value) {
            super(name, value);
        }
    }

    public static class FileFormField extends FormField<File> {

        public FileFormField(String name, File value) {
            super(name, value);
        }
    }
}
