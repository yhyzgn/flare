package com.yhy.http.flare.test;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.test.model.Cat;
import com.yhy.http.flare.test.model.PartForm;
import com.yhy.http.flare.test.model.Res;
import com.yhy.http.flare.test.model.User;
import com.yhy.http.flare.test.remote.MockPostApi;
import com.yhy.http.flare.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 测试类
 * <p>
 * Created on 2025-09-15 17:15
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class FlarePostTest {

    @Test
    public void index() {
        MockPostApi api = flare().create(MockPostApi.class);
        Res<String> res = api.index();
        logRes(res);
    }

    @Test
    public void form() {
        MockPostApi api = flare().create(MockPostApi.class);
        Res<String> res = api.form("李万姬", 25);
        logRes(res);
    }

    @Test
    public void formUser() {
        MockPostApi api = flare().create(MockPostApi.class);
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        Res<User> res = api.formUser(user);
        logRes(res);
    }

    @Test
    public void formDefault() {
        MockPostApi api = flare().create(MockPostApi.class);
        Res<String> res = api.formDefault("李万姬", 25);
        logRes(res);
    }

    @Test
    public void body() {
        MockPostApi api = flare().create(MockPostApi.class);
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        Res<User> res = api.body(user);
        logRes(res);
    }

    @Test
    public void upload() {
        MockPostApi api = flare().create(MockPostApi.class);
        File file = new File("/home/neo/Downloads/sample1.webp");
        Res<String> res = api.upload(file);
        logRes(res);
    }

    @Test
    public void uploadBytes() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        File file = new File("/home/neo/Downloads/sample1.webp");
        Res<String> res = api.uploadBytes(FileUtils.readFileToByteArray(file));
        logRes(res);
    }

    @Test
    public void uploadStream() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        Res<String> res = api.uploadStream(fis);
        fis.close();
        logRes(res);
    }

    @Test
    public void partForm() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        File file = new File("/home/neo/Downloads/sample1.webp");
        FileInputStream fis = new FileInputStream(file);
        PartForm form = new PartForm();
        form.setName("test");
        form.setFile(file);
        form.setBytesFile(FileUtils.readFileToByteArray(file));
        form.setTempInputStreamFile(fis);
        Res<String> res = api.partForm(form);
        fis.close();
        logRes(res);
    }

    @Test
    public void uploadError() throws IOException {
        // MockPostApi api = flare().create(MockPostApi.class);
        // File file = new File("/home/neo/Downloads/sample1.webp");
        // FileInputStream fis = new FileInputStream(file);
        // Res<String> res = api.uploadError(fis);
        // fis.close();
        // logRes(res);
    }

    @Test
    public void uploadBinary() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        File file = new File("/home/neo/Downloads/sample1.webp");
        Res<String> res = api.uploadBinary(FileUtils.readFileToByteArray(file));
        logRes(res);
    }

    private Flare flare() {
        return new Flare.Builder()
                .baseUrl(MockPostApi.BASE_URL)
                .logEnabled(true)
                .build();
    }

    private <T> void logRes(Res<T> res) {
        log.info("res: {}", res);
        Assert.isTrue(res.ok(), res.message());
        log.info("data: {}", res.data());
    }
}
