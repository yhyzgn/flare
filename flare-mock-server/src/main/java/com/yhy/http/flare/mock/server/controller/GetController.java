package com.yhy.http.flare.mock.server.controller;

import com.yhy.http.flare.mock.server.model.Res;
import com.yhy.http.flare.mock.server.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * mock 服务 GET 请求测试端点。
 * <p>
 * 挂载在 {@code /get} 前缀下，提供 GET 请求的模拟场景：
 * 固定响应、查询参数绑定、对象参数绑定与路径变量绑定。
 * <p>
 * Created on 2025-09-16 13:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/get")
public class GetController {

    /**
     * 固定响应端点，返回请求路径的描述文本。
     *
     * @return 成功响应，内容为端点描述
     */
    @GetMapping("/index")
    public Res<String> index() {
        log.info("GET 请求 /get/index");
        return Res.success("GET 请求 /get/index");
    }

    /**
     * 查询参数端点，接收 name 与 age 查询参数。
     *
     * @param name 查询参数：名称
     * @param age 查询参数：年龄
     * @return 成功响应，内容为参数拼接文本
     */
    @GetMapping("/query")
    public Res<String> query(String name, Integer age) {
        log.info("GET 请求 /get/query?name={}&age={}", name, age);
        return Res.success("GET 请求 /get/query?name=" + name + "&age=" + age);
    }

    /**
     * 对象查询参数端点，将查询参数绑定为 {@link User} 对象并原样返回。
     *
     * @param user 由查询参数绑定的用户对象
     * @return 成功响应，data 为绑定的用户对象
     */
    @GetMapping("/queryUser")
    public Res<User> query(User user) {
        log.info("GET 请求 /get/queryUser?user={}", user);
        return Res.success(user);
    }

    /**
     * 路径变量端点，接收路径中的 name 与 age。
     *
     * @param name 路径变量：名称
     * @param age 路径变量：年龄
     * @return 成功响应，内容为路径变量拼接文本
     */
    @GetMapping("/query/{name}/{age}")
    public Res<String> queryPath(@PathVariable("name") String name, @PathVariable("age") Integer age) {
        log.info("GET 请求 /get/query/{}/{}", name, age);
        return Res.success("GET 请求 /get/query/" + name + "/" + age);
    }
}
