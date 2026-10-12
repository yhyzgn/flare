package com.yhy.http.flare.spring.starter.register;

import com.yhy.http.flare.spring.starter.annotation.EnableFlare;
import com.yhy.http.flare.spring.starter.annotation.Flare;

import java.lang.annotation.Annotation;

/**
 * Spring Boot starter 的 Flare 自动注册器。
 * <p>
 * 继承 {@link AbstractFlareAutoRegister}，绑定 {@link EnableFlare} 启用注解与
 * {@link Flare} 接口注解，完成扫描注册。
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
     * @return {@link EnableFlare} 注解类型
     */
    @Override
    public Class<? extends Annotation> enableAnnotation() {
        return EnableFlare.class;
    }

    /**
     * 获取 Flare 接口注解类型。
     *
     * @return {@link Flare} 注解类型
     */
    @Override
    public Class<? extends Annotation> flareAnnotation() {
        return Flare.class;
    }
}
