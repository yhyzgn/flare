package com.yhy.http.flare.mock.server.model;

/**
 * mock 服务的猫信息模型。
 * <p>
 * 作为 {@link User} 的嵌套字段，用于验证嵌套对象 JSON 的序列化与反序列化。
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
