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
    public Res<String> query() {
        return getRemote.query("李万姬", 25);
    }

    @GetMapping("/queryPath")
    public Res<String> queryPath() {
        return getRemote.queryPath("李/万姬", 25);
    }

    @GetMapping("/queryUser")
    public Res<User> queryUser() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return getRemote.queryUser(user);
    }

    @GetMapping("/queryDefault")
    public Res<String> queryDefault() {
        return getRemote.queryDefault("李/万姬", 25);
    }

    @GetMapping("/forBody")
    public Res<String> forBody() throws IOException {
        okhttp3.ResponseBody body = getRemote.forBody();
        return Res.success(body.string());
    }

    @GetMapping("/forBytes")
    public Res<String> forBytes() throws IOException {
        byte[] bytes = getRemote.forBytes();
        return Res.success(new String(bytes));
    }

    @GetMapping("/forInputStream")
    public Res<String> forInputStream() throws IOException {
        InputStream inputStream = getRemote.forInputStream();
        return Res.success(new String(inputStream.readAllBytes()));
    }

    @GetMapping("/forFile")
    public Res<String> forFile() throws IOException {
        File file = getRemote.forFile();
        return Res.success(file.getAbsolutePath());
    }

    @GetMapping("/forFileDownload")
    public Res<String> forFileDownload() throws IOException {
        File file = getRemote.forFileDownload();
        return Res.success(file.getAbsolutePath());
    }

    @GetMapping("/forVoidFileDownload")
    public Res<String> forVoidFileDownload() throws IOException {
        getRemote.forVoidFileDownload();
        return Res.success();
    }
}
