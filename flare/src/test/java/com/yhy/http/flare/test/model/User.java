package com.yhy.http.flare.test.model;

/**
 * 用户信息测试模型，包含嵌套的 {@link Cat}，用于验证嵌套对象的序列化。
 * <p>
 * Created on 2025-09-16 13:53
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 * @param id 用户 ID
 * @param name 名称
 * @param age 年龄
 * @param cat 猫信息
 */
public record User(Long id, String name, Integer age, Cat cat) {
}
