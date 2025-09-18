package com.yhy.http.flare.spring.boot.sample.controller;

import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.boot.sample.remote.GetRemote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET 请求方式接口
 * <p>
 * Created on 2025-09-16 13:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/get")
public class GetController {
    private final GetRemote getRemote;

    @GetMapping("/index")
    public Res<String> index() {
        return getRemote.index();
    }

    @GetMapping("/query")
    public Res<String> query(String name, Integer age) {
        log.info("GET 请求 /get/query?name={}&age={}", name, age);
        return Res.success("GET 请求 /get/query?name=" + name + "&age=" + age);
    }

    @GetMapping("/queryUser")
    public Res<User> query(User user) {
        log.info("GET 请求 /get/queryUser?user={}", user);
        return Res.success(user);
    }

    @GetMapping("/query/{name}/{age}")
    public Res<String> queryPath(@PathVariable("name") String name, @PathVariable("age") Integer age) {
        log.info("GET 请求 /get/query/{}/{}", name, age);
        return Res.success("GET 请求 /get/query/" + name + "/" + age);
    }
}
