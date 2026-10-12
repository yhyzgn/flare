package com.yhy.http.flare.spring.boot.sample.model;

/**
 * 猫信息模型，作为 {@link User} 的嵌套对象用于示例数据传递。
 * <p>
 * Created on 2025-09-16 14:35
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @param name 名称
 * @param color 颜色
 */
public record Cat(String name, String color) {
}
