package com.yhy.http.flare.annotation.param;

import java.lang.annotation.*;

/**
 * 参数标签注解。
 * <p>
 * 标注在方法参数上，把参数值附加到请求的 {@link com.yhy.http.flare.model.Invocation} 标签区，
 * 供拦截器在请求处理链中读取；同一方法上同类型标签不能重复。
 *
 * Created on 2025-09-13 15:24
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tag {
}
