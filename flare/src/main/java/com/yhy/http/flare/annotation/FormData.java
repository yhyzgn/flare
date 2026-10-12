package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * {@code multipart/form-data} 表单编码注解。
 * <p>
 * 标注在被代理接口的方法上，声明该方法以 {@code multipart/form-data} 编码提交表单。
 * 方法参数可以使用 {@link com.yhy.http.flare.annotation.param.Field} 添加普通表单字段、
 * {@link com.yhy.http.flare.annotation.param.Multipart} 添加文件部件。
 * <p>
 * Created on 2025-09-13 15:15
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormData {
}
