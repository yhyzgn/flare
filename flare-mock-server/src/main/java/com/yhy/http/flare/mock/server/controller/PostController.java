package com.yhy.http.flare.mock.server.controller;

import com.yhy.http.flare.mock.server.model.PartForm;
import com.yhy.http.flare.mock.server.model.Res;
import com.yhy.http.flare.mock.server.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * mock 服务 POST 请求测试端点。
 * <p>
 * 挂载在 {@code /post} 前缀下，提供 POST 请求的模拟场景：
 * 固定响应、表单参数绑定、对象表单绑定、JSON 请求体、文件上传、
 * 多段表单与二进制流上传。
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
     * 固定响应端点，返回请求路径的描述文本。
     *
     * @return 成功响应，内容为端点描述
     */
    @PostMapping("/index")
    public Res<String> index() {
        log.info("POST 请求 /post/index");
        return Res.success("POST 请求 /post/index");
    }

    /**
     * 表单端点，接收 name 与 age 表单参数。
     *
     * @param name 表单参数：名称
     * @param age 表单参数：年龄
     * @return 成功响应，内容为参数拼接文本
     */
    @PostMapping("/form")
    public Res<String> form(String name, Integer age) {
        log.info("POST 请求 /post/form?name={}&age={}", name, age);
        return Res.success("POST 请求 /post/form?name=" + name + "&age=" + age);
    }

    /**
     * 对象表单端点，将表单参数绑定为 {@link User} 对象并原样返回。
     *
     * @param user 由表单参数绑定的用户对象
     * @return 成功响应，data 为绑定的用户对象
     */
    @PostMapping("/formUser")
    public Res<User> form(User user) {
        log.info("POST 请求 /post/formUser?user={}", user);
        return Res.success(user);
    }

    /**
     * JSON 请求体端点，接收 {@link User} 请求体并原样返回。
     *
     * @param user JSON 反序列化的用户对象
     * @return 成功响应，data 为请求体中的用户对象
     */
    @PostMapping("/body")
    public Res<User> body(@RequestBody User user) {
        log.info("POST 请求 /post/body, user={}", user);
        return Res.success(user);
    }

    /**
     * 单文件上传端点，接收名为 {@code file} 的 multipart 分片。
     *
     * @param file 上传的文件分片
     * @return 成功响应，内容为文件名与字节大小
     */
    @PostMapping("/upload")
    public Res<String> upload(@RequestPart("file") MultipartFile file) {
        log.info("POST 请求 /post/upload, file={}, size={}", file.getOriginalFilename(), file.getSize());
        return Res.success(file.getOriginalFilename() + ", size = " + file.getSize());
    }

    /**
     * 多段表单端点，将普通字段与多个文件分片绑定为 {@link PartForm}。
     *
     * @param form 由 multipart 表单绑定的表单对象
     * @return 成功响应，内容为表单的 name 字段
     */
    @PostMapping("/partForm")
    public Res<String> partForm(@ModelAttribute PartForm form) {
        log.info("POST 请求 /post/upload, form={}", form);
        return Res.success(form.getName());
    }

    /**
     * 二进制流上传端点，接收 {@code application/octet-stream} 原始请求体。
     *
     * @param data 请求体的原始字节
     * @return 成功响应，内容为收到的字节数
     */
    @PostMapping(value = "/uploadBinary", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Res<String> uploadBinary(@RequestBody byte[] data) {
        log.info("POST 请求 /post/upload, data={}", data.length);
        return Res.success("收到二进制流, 大小: " + data.length);
    }
}
