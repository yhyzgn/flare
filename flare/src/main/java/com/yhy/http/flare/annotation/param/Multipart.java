package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * multipart 文件部件参数注解。
 * <p>
 * 标注在方法参数（或表单对象的字段）上，声明该参数作为 {@code multipart/form-data} 的文件部件提交。
 * 仅在 {@link com.yhy.http.flare.annotation.FormData} 编码的方法下有效。
 *
 * Created on 2025-09-13 15:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Multipart {

    /**
     * 部件名称。
     * <p>
     * 为空字符串时回退到方法签名中的参数名（或标注字段的字段名）。
     *
     * @return 部件名称，为空字符串时回退到参数名
     */
    String value() default "";

    /**
     * 文件部件的文件名。
     * <p>
     * 为空字符串时不指定文件名。
     *
     * @return 文件名，为空字符串时不指定
     */
    String filename() default "";
}
