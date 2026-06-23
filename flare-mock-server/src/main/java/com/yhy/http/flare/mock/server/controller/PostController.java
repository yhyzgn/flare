package com.yhy.http.flare.mock.server.controller;

import com.yhy.http.flare.mock.server.model.PartForm;
import com.yhy.http.flare.mock.server.model.Res;
import com.yhy.http.flare.mock.server.model.User;
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

    /**
     * 首页接口。
     *
     * @return 处理结果
     */
    @PostMapping("/index")
    public Res<String> index() {
        log.info("POST 请求 /post/index");
        return Res.success("POST 请求 /post/index");
    }

    /**
     * 表单接口。
     *
     * @param name 字符串
     * @param age 整数
     * @return 处理结果
     */
    @PostMapping("/form")
    public Res<String> form(String name, Integer age) {
        log.info("POST 请求 /post/form?name={}&age={}", name, age);
        return Res.success("POST 请求 /post/form?name=" + name + "&age=" + age);
    }

    /**
     * 表单接口。
     *
     * @param user 值
     * @return 处理结果
     */
    @PostMapping("/formUser")
    public Res<User> form(User user) {
        log.info("POST 请求 /post/formUser?user={}", user);
        return Res.success(user);
    }

    /**
     * 请求体接口。
     *
     * @param user 值
     * @return 处理结果
     */
    @PostMapping("/body")
    public Res<User> body(@RequestBody User user) {
        log.info("POST 请求 /post/body, user={}", user);
        return Res.success(user);
    }

    /**
     * 上传接口。
     *
     * @param file 文件
     * @return 处理结果
     */
    @PostMapping("/upload")
    public Res<String> upload(@RequestPart("file") MultipartFile file) {
        log.info("POST 请求 /post/upload, file={}", file.getOriginalFilename());
        return Res.success(file.getOriginalFilename());
    }

    /**
     * 多段表单接口。
     *
     * @param form 值
     * @return 处理结果
     */
    @PostMapping("/partForm")
    public Res<String> partForm(@ModelAttribute PartForm form) {
        log.info("POST 请求 /post/upload, form={}", form);
        return Res.success(form.getName());
    }

    /**
     * 二进制上传接口。
     *
     * @param data 字节数组
     * @return 处理结果
     */
    @PostMapping(value = "/uploadBinary", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Res<String> uploadBinary(@RequestBody byte[] data) {
        log.info("POST 请求 /post/upload, data={}", data.length);
        return Res.success("收到二进制流, 大小: " + data.length);
    }
}
