package com.yhy.http.flare.spring.boot.sample.model;

/**
 * user
 * <p>
 * Created on 2025-09-16 13:53
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public record User(Long id, String name, Integer age, Cat cat) {
}
