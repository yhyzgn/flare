package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 文件下载注解。
 * <p>
 * 标注在被代理接口的方法上，声明该方法的响应体应作为文件保存到本地磁盘，
 * 而不是转换为普通响应对象。
 * <p>
 * Created on 2025-09-17 13:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Download {

    /**
     * 保存路径，包含文件路径和文件名。
     * <p>
     * 默认为空字符串，表示保存到系统的临时目录中。
     *
     * @return 保存路径，空字符串表示使用系统临时目录
     */
    String filePath() default "";

    /**
     * 是否覆盖已存在的文件。
     * <p>
     * 为 {@code false} 且目标文件已存在时不覆盖。
     *
     * @return 目标文件已存在时是否覆盖
     */
    boolean overwrite() default false;
}
