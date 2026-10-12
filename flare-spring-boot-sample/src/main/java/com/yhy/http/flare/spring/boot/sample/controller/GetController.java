package com.yhy.http.flare.spring.boot.sample.controller;

import com.yhy.http.flare.spring.boot.sample.model.Cat;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.boot.sample.remote.GetRemote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * GET 请求示例控制器，将本地 HTTP 端点转发为对 mock-server 的远端调用。
 * <p>
 * 通过注入的 {@link GetRemote} 代理演示 GET 类场景：路径参数、查询参数、对象参数、
 * 原始响应体、字节数组、输入流、文件下载等返回类型。
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

    /**
     * 首页接口。
     *
     * @return mock-server 首页响应结果
     */
    @GetMapping("/index")
    public Res<String> index() {
        return getRemote.index();
    }

    /**
     * 指定完整 URL 请求示例，演示 {@code @Url} 参数覆盖注解子路径。
     *
     * @return mock-server 首页响应结果
     */
    @GetMapping("/indexByUrl")
    public Res<String> indexByUrl() {
        return getRemote.indexByUrl("http://localhost:8080/get/index");
    }

    /**
     * 查询接口。
     *
     * @return 查询转发结果（响应仅记录日志，固定返回成功）
     */
    @GetMapping("/query")
    public Res<String> query() {
        Res<String> res = getRemote.query("李万姬", 25);
        log.info("query: {}", res);
        return Res.success();
    }

    /**
     * 路径查询接口。
     *
     * @return 带斜杠路径参数的查询结果
     */
    @GetMapping("/queryPath")
    public Res<String> queryPath() {
        return getRemote.queryPath("李/万姬", 25);
    }

    /**
     * 用户查询接口。
     *
     * @return 按用户对象查询的结果
     */
    @GetMapping("/queryUser")
    public Res<User> queryUser() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return getRemote.queryUser(user);
    }

    /**
     * 默认查询接口。
     *
     * @return 无注解名称参数按签名参数名绑定的查询结果
     */
    @GetMapping("/queryDefault")
    public Res<String> queryDefault() {
        return getRemote.queryDefault("李/万姬", 25);
    }

    /**
     * 响应体示例接口。
     *
     * @return 原始响应体转换成的字符串内容
     * @throws IOException 读取响应体时发生 I/O 错误
     */
    @GetMapping("/forBody")
    public Res<String> forBody() throws IOException {
        okhttp3.ResponseBody body = getRemote.forBody();
        return Res.success(body.string());
    }

    /**
     * 字节数组示例接口。
     *
     * @return 字节数组响应（仅记录日志，固定返回成功）
     * @throws IOException 读取响应体时发生 I/O 错误
     */
    @GetMapping("/forBytes")
    public Res<String> forBytes() throws IOException {
        byte[] bytes = getRemote.forBytes();
        log.info("forBytes: {}", bytes);
        return Res.success();
    }

    /**
     * 输入流示例接口。
     *
     * @return 输入流读取的字符串内容
     * @throws IOException 读取输入流时发生 I/O 错误
     */
    @GetMapping("/forInputStream")
    public Res<String> forInputStream() throws IOException {
        InputStream inputStream = getRemote.forInputStream();
        return Res.success(new String(inputStream.readAllBytes()));
    }

    /**
     * 文件示例接口。
     *
     * @return 临时文件的绝对路径
     * @throws IOException 响应体写入文件时发生 I/O 错误
     */
    @GetMapping("/forFile")
    public Res<String> forFile() throws IOException {
        File file = getRemote.forFile();
        return Res.success(file.getAbsolutePath());
    }

    /**
     * 文件下载示例接口。
     *
     * @return 下载文件的绝对路径
     * @throws IOException 下载写入文件时发生 I/O 错误
     */
    @GetMapping("/forFileDownload")
    public Res<String> forFileDownload() throws IOException {
        File file = getRemote.forFileDownload();
        return Res.success(file.getAbsolutePath());
    }

    /**
     * 无返回文件下载示例接口。
     *
     * @return 固定成功响应（远端方法无返回值）
     * @throws IOException 下载写入文件时发生 I/O 错误
     */
    @GetMapping("/forVoidFileDownload")
    public Res<String> forVoidFileDownload() throws IOException {
        getRemote.forVoidFileDownload();
        return Res.success();
    }
}
