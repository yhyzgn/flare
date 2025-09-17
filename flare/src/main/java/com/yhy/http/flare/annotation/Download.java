package com.yhy.http.flare.annotation;

import java.lang.annotation.*;

/**
 * 文件下载注解
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
     * 保存路径，包含文件路径和文件名
     * <p>
     * 默认为空，表示保存到系统的临时目录中
     *
     * @return 保存路径
     */
    String filePath() default "";

    /**
     * 是否覆盖已存在的文件
     *
     * @return 是否覆盖
     */
    boolean overwrite() default false;
}
