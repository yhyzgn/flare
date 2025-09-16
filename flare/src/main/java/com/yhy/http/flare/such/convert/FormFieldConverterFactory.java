package com.yhy.http.flare.such.convert;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.param.Multipart;
import com.yhy.http.flare.convert.FormFieldConverter;
import com.yhy.http.flare.model.FormField;
import com.yhy.http.flare.utils.Opt;
import com.yhy.http.flare.utils.ReflectUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 表单字段转换器工厂
 * <p>
 * Created on 2025-09-15 15:29
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class FormFieldConverterFactory implements FormFieldConverter.Factory {

    @Override
    public FormFieldConverter<?> converter(Type type, Annotation[] annotations, Flare flare) {
        return new InternalFormFieldConverter<>();
    }

    private record InternalFormFieldConverter<T>() implements FormFieldConverter<T> {
        /**
         * 递归后的默认值
         */
        private static final String DEFAULT_VALUE = null;

        @Override
        public List<FormField<?>> convert(String name, T value, boolean encoded, String defaultValue) {
            List<FormField<?>> formFields = new ArrayList<>();
            serialize(name == null ? "" : name, value, formFields, encoded, defaultValue);
            return formFields;
        }

        // 递归把对象展开成 Map
        private static void serialize(String name, Object obj, List<FormField<?>> fieldList, boolean encoded, String defaultValue) {
            if (obj == null) {
                return;
            }

            Class<?> clazz = obj.getClass();

            // 基本类型或 String，直接放进去
            if (ReflectUtils.isPrimitiveOrString(clazz)) {
                String value = Opt.ofNullable(obj).map(Object::toString).orElse(defaultValue);
                fieldList.add(new FormField.ValueFormField(name, value, encoded, defaultValue));
                return;
            }

            // 如果是数组，按下标展开
            if (clazz.isArray()) {
                int i = 0;
                for (Object item : (Object[]) obj) {
                    serialize(name + "[" + i + "]", item, fieldList, encoded, DEFAULT_VALUE);
                    i++;
                }
                return;
            }

            // 如果是集合，按下标展开
            if (obj instanceof Collection) {
                int i = 0;
                for (Object item : (Collection<?>) obj) {
                    serialize(name + "[" + i + "]", item, fieldList, encoded, DEFAULT_VALUE);
                    i++;
                }
                return;
            }

            // 如果是 Map，按 key 展开
            if (obj instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                    serialize(name + "." + entry.getKey().toString(), entry.getValue(), fieldList, encoded, DEFAULT_VALUE);
                }
                return;
            }

            // 普通 Java Bean，反射字段
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                // 如果字段是静态的或者 transient 的，则跳过
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                Object value = ReflectUtils.getValue(field, obj);

                if (null == value) {
                    continue;
                }

                // 如果是 Multipart 文件字段，直接添加
                if (field.isAnnotationPresent(Multipart.class)) {
                    Multipart multipart = field.getAnnotation(Multipart.class);
                    String filedName = Opt.ofNullable(multipart.value()).orElse(field.getName());
                    String newName = name.isEmpty() ? filedName : name + "." + filedName;

                    // 支持 File, byte[], InputStream 类型的字段
                    if (field.getType() == File.class) {
                        fieldList.add(new FormField.FileFormField(newName, (File) value, multipart.filename()));
                        continue;
                    }

                    if (field.getType() == byte[].class) {
                        fieldList.add(new FormField.BytesFormField(newName, (byte[]) value, multipart.filename()));
                        continue;
                    }

                    if (InputStream.class.isAssignableFrom(field.getType())) {
                        fieldList.add(new FormField.InputStreamFormField(newName, (InputStream) value, multipart.filename()));
                        continue;
                    }

                    throw new IllegalArgumentException(field.getType().getName() + " is not a valid multipart field");
                }

                // 否则递归处理字段
                String newName = name.isEmpty() ? field.getName() : name + "." + field.getName();
                serialize(newName, value, fieldList, encoded, DEFAULT_VALUE);
            }
        }
    }
}
