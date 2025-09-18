package com.yhy.http.flare.spring.boot.sample.controller;

import com.yhy.http.flare.spring.boot.sample.model.PartForm;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * POST 请求方式接口
 * <p>
 * Created on 2025-09-16 13:34
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    @PostMapping("/index")
    public Res<String> index() {
        log.info("POST 请求 /post/index");
        return Res.success("POST 请求 /post/index");
    }

    @PostMapping("/form")
    public Res<String> form(String name, Integer age) {
        log.info("POST 请求 /post/form?name={}&age={}", name, age);
        return Res.success("POST 请求 /post/form?name=" + name + "&age=" + age);
    }

    @PostMapping("/formUser")
    public Res<User> form(User user) {
        log.info("POST 请求 /post/formUser?user={}", user);
        return Res.success(user);
    }

    @PostMapping("/body")
    public Res<User> body(@RequestBody User user) {
        log.info("POST 请求 /post/body, user={}", user);
        return Res.success(user);
    }

    @PostMapping("/upload")
    public Res<String> upload(@RequestPart("file") MultipartFile file) {
        log.info("POST 请求 /post/upload, file={}", file.getOriginalFilename());
        return Res.success(file.getOriginalFilename());
    }

    @PostMapping("/partForm")
    public Res<String> partForm(@ModelAttribute PartForm form) {
        log.info("POST 请求 /post/upload, form={}", form);
        return Res.success(form.getName());
    }

    @PostMapping(value = "/uploadBinary", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Res<String> uploadBinary(@RequestBody byte[] data) {
        log.info("POST 请求 /post/upload, data={}", data.length);
        return Res.success("收到二进制流, 大小: " + data.length);
    }
}
