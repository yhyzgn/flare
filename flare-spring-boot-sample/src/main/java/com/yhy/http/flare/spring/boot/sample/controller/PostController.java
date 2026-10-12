package com.yhy.http.flare.spring.boot.sample.controller;

import com.yhy.http.flare.spring.boot.sample.model.Cat;
import com.yhy.http.flare.spring.boot.sample.model.PartForm;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.boot.sample.remote.PostRemote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * POST 请求示例控制器，将本地 HTTP 端点转发为对 mock-server 的远端调用。
 * <p>
 * 通过注入的 {@link PostRemote} 代理演示 POST 类场景：表单提交、请求体、
 * 文件上传（{@code File} / {@code byte[]} / {@code FileInputStream}）、多段表单和二进制上传。
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
@RequestMapping("/post")
public class PostController {
    private final PostRemote postRemote;

    /**
     * 首页接口。
     *
     * @return mock-server 首页响应结果
     */
    @PostMapping("/index")
    public Res<String> index() {
        return postRemote.index();
    }

    /**
     * 表单接口。
     *
     * @return 表单提交结果
     */
    @PostMapping("/form")
    public Res<String> form() {
        return postRemote.form("李万姬", 25, "Form-Header-Value");
    }

    /**
     * 用户表单接口。
     *
     * @return 按用户对象提交表单的结果
     */
    @PostMapping("/formUser")
    public Res<User> formUser() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return postRemote.formUser(user);
    }

    /**
     * 默认表单接口。
     *
     * @return 无注解名称参数按签名参数名绑定的表单提交结果
     */
    @PostMapping("/formDefault")
    public Res<String> formDefault() {
        return postRemote.formDefault("李万姬", 25);
    }

    /**
     * 请求体接口。
     *
     * @return 按用户对象提交 JSON 请求体的结果
     */
    @PostMapping("/body")
    public Res<User> body() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return postRemote.body(user);
    }

    /**
     * 上传接口。
     *
     * @return 文件上传结果
     */
    @PostMapping("/upload")
    public Res<String> upload() {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.upload(file);
    }

    /**
     * 字节上传接口。
     *
     * @return 字节数组上传结果
     * @throws IOException 读取本地文件时发生 I/O 错误
     */
    @PostMapping("/uploadBytes")
    public Res<String> uploadBytes() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBytes(FileUtils.readFileToByteArray(file));
    }

    /**
     * 流上传接口。
     *
     * @return 文件输入流上传结果
     * @throws IOException 打开或读取本地文件时发生 I/O 错误
     */
    @PostMapping("/uploadStream")
    public Res<String> uploadStream() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        Res<String> res = postRemote.uploadStream(fis);
        fis.close();
        return res;
    }

    /**
     * 多段表单接口。
     *
     * @return 多段表单提交结果
     * @throws IOException 打开或读取本地文件时发生 I/O 错误
     */
    @PostMapping("/partForm")
    public Res<String> partForm() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        PartForm form = new PartForm();
        form.setName("test");
        form.setFile(file);
        form.setBytesFile(FileUtils.readFileToByteArray(file));
        form.setTempInputStreamFile(fis);
        Res<String> res = postRemote.partForm(form);
        fis.close();
        return res;
    }

    /**
     * 上传异常示例接口。
     *
     * @return 固定成功响应（远端调用当前已注释，仅保留端点）
     */
    @PostMapping("/uploadError")
    public Res<String> uploadError() {
        // postRemote.uploadError();
        return Res.success();
    }

    /**
     * 二进制上传接口。
     *
     * @return 二进制数据上传结果
     * @throws IOException 读取本地文件时发生 I/O 错误
     */
    @PostMapping("/uploadBinary")
    public Res<String> uploadBinary() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBinary(FileUtils.readFileToByteArray(file));
    }

    /**
     * 二进制文件上传接口。
     *
     * @return 二进制文件上传结果
     */
    @PostMapping("/uploadBinaryFile")
    public Res<String> uploadBinaryFile() {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBinaryFile(file);
    }

    /**
     * 二进制输入流上传接口。
     *
     * @return 二进制输入流上传结果
     * @throws IOException 打开或读取本地文件时发生 I/O 错误
     */
    @PostMapping("/uploadBinaryInputStream")
    public Res<String> uploadBinaryInputStream() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        Res<String> res = postRemote.uploadBinaryInputStream(fis);
        fis.close();
        return res;
    }
}
