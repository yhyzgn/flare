package com.yhy.http.flare.test;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.test.model.Cat;
import com.yhy.http.flare.test.model.Res;
import com.yhy.http.flare.test.model.User;
import com.yhy.http.flare.test.remote.MockGetApi;
import com.yhy.http.flare.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;

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
public class FlareGetTest {

    @Test
    public void index() {
        MockGetApi api = flare().create(MockGetApi.class);
        Res<String> res = api.index();
        logRes(res);
    }

    @Test
    public void query() {
        MockGetApi api = flare().create(MockGetApi.class);
        Res<String> res = api.query("李万姬", 25);
        logRes(res);
    }

    @Test
    public void queryPath() {
        MockGetApi api = flare().create(MockGetApi.class);
        Res<String> res = api.queryPath("李/万姬", 25);
        logRes(res);
    }

    @Test
    public void queryUser() {
        MockGetApi api = flare().create(MockGetApi.class);
        Cat cat = new Cat("Tom", "white");
        User user = new User(1L, "李万姬", 25, cat);
        Res<User> res = api.queryUser(user);
        logRes(res);
    }

    @Test
    public void queryDefault() {
        MockGetApi api = flare().create(MockGetApi.class);
        Res<String> res = api.queryDefault("李万姬", 25);
        logRes(res);
    }

    @Test
    public void forBody() {
        MockGetApi api = flare().create(MockGetApi.class);
        okhttp3.ResponseBody body = api.forBody();
        log.info("body: {}", body);
    }

    @Test
    public void forBytes() {
        MockGetApi api = flare().create(MockGetApi.class);
        byte[] bytes = api.forBytes();
        log.info("bytes: {}", bytes);
    }

    @Test
    public void forInputStream() {
        MockGetApi api = flare().create(MockGetApi.class);
        InputStream inputStream = api.forInputStream();
        log.info("inputStream: {}", inputStream);
    }

    @Test
    public void forFile() {
        MockGetApi api = flare().create(MockGetApi.class);
        File file = api.forFile();
        log.info("file: {}", file);
    }

    @Test
    public void forFileDownload() {
        MockGetApi api = flare().create(MockGetApi.class);
        File file = api.forFileDownload();
        log.info("file: {}", file);
    }

    @Test
    public void forVoidFileDownload() {
        MockGetApi api = flare().create(MockGetApi.class);
        api.forVoidFileDownload();
    }

    private Flare flare() {
        return new Flare.Builder()
                .baseUrl(MockGetApi.BASE_URL)
                .logEnabled(true)
                .build();
    }

    private <T> void logRes(Res<T> res) {
        log.info("res: {}", res);
        Assert.isTrue(res.ok(), res.message());
        log.info("data: {}", res.data());
    }
}
