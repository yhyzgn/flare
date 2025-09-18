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
 * POST 请求方式接口
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

    @PostMapping("/index")
    public Res<String> index() {
        return postRemote.index();
    }

    @PostMapping("/form")
    public Res<String> form() {
        return postRemote.form("李万姬", 25, "Form-Header-Value");
    }

    @PostMapping("/formUser")
    public Res<User> formUser() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return postRemote.formUser(user);
    }

    @PostMapping("/formDefault")
    public Res<String> formDefault() {
        return postRemote.formDefault("李万姬", 25);
    }

    @PostMapping("/body")
    public Res<User> body() {
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        return postRemote.body(user);
    }

    @PostMapping("/upload")
    public Res<String> upload() {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.upload(file);
    }

    @PostMapping("/uploadBytes")
    public Res<String> uploadBytes() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBytes(FileUtils.readFileToByteArray(file));
    }

    @PostMapping("/uploadStream")
    public Res<String> uploadStream() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        Res<String> res = postRemote.uploadStream(fis);
        fis.close();
        return res;
    }

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

    @PostMapping("/uploadError")
    public Res<String> uploadError() {
        // postRemote.uploadError();
        return Res.success();
    }

    @PostMapping("/uploadBinary")
    public Res<String> uploadBinary() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBinary(FileUtils.readFileToByteArray(file));
    }

    @PostMapping("/uploadBinaryFile")
    public Res<String> uploadBinaryFile() {
        File file = new File("/home/neo/Downloads/sample1.webp");
        return postRemote.uploadBinaryFile(file);
    }

    @PostMapping("/uploadBinaryInputStream")
    public Res<String> uploadBinaryInputStream() throws IOException {
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        Res<String> res = postRemote.uploadBinaryInputStream(fis);
        fis.close();
        return res;
    }
}
