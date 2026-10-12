package com.yhy.http.flare.mock.server.model;

/**
 * mock 服务的用户信息模型。
 * <p>
 * 用于验证 JSON 请求体、表单对象绑定与嵌套对象（{@link Cat}）的往返序列化。
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
