package com.yhy.http.flare.spring.starter.register;

import com.yhy.http.flare.spring.starter.annotation.EnableFlare;
import com.yhy.http.flare.spring.starter.annotation.Flare;

import java.lang.annotation.Annotation;

/**
 * Flare 自动注册器
 * <p>
 * Created on 2025-09-18 11:16
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public class FlareAutoRegister extends AbstractFlareAutoRegister {

    /**
     * 获取启用注解类型。
     *
     * @return 处理结果
     */
    @Override
    public Class<? extends Annotation> enableAnnotation() {
        return EnableFlare.class;
    }

    /**
     * 获取 Flare 注解类型。
     *
     * @return 处理结果
     */
    @Override
    public Class<? extends Annotation> flareAnnotation() {
        return Flare.class;
    }
}
