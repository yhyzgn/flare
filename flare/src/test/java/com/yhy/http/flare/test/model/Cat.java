package com.yhy.http.flare.test.model;

/**
 * 猫信息测试模型，作为 {@link User} 的嵌套对象用于验证对象参数的序列化。
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
