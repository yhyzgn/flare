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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    public void upload() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try {
            File file = tmp.toFile();
            Res<String> res = api.upload(file);
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void uploadBytes() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try {
            Res<String> res = api.uploadBytes(FileUtils.readFileToByteArray(tmp.toFile()));
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void uploadStream() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try (FileInputStream fis = new FileInputStream(tmp.toFile())) {
            Res<String> res = api.uploadStream(fis);
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void partForm() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try (FileInputStream fis = new FileInputStream(tmp.toFile())) {
            PartForm form = new PartForm();
            File file = tmp.toFile();
            form.setName("test");
            form.setFile(file);
            form.setBytesFile(FileUtils.readFileToByteArray(file));
            form.setTempInputStreamFile(fis);
            Res<String> res = api.partForm(form);
            fis.close();
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void uploadError() throws IOException {
        // MockPostApi api = flare().create(MockPostApi.class);
        // Path tmp = createTempSampleFile();
        // try (FileInputStream fis = new FileInputStream(tmp.toFile())) {
        //     Res<String> res = api.uploadError(fis);
        //     logRes(res);
        // } finally {
        //     Files.deleteIfExists(tmp);
        // }
    }

    @Test
    public void uploadBinary() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try {
            Res<String> res = api.uploadBinary(FileUtils.readFileToByteArray(tmp.toFile()));
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void uploadBinaryFile() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try {
            Res<String> res = api.uploadBinaryFile(tmp.toFile());
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void uploadBinaryInputStream() throws IOException {
        MockPostApi api = flare().create(MockPostApi.class);
        Path tmp = createTempSampleFile();
        try (FileInputStream fis = new FileInputStream(tmp.toFile())) {
            Res<String> res = api.uploadBinaryInputStream(fis);
            logRes(res);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    // Helper: create a temporary sample file from classpath resource or empty placeholder
    private Path createTempSampleFile() throws IOException {
        Path tmp = Files.createTempFile("sample1-", ".webp");
        try (InputStream is = getClass().getResourceAsStream("/samples/sample1.webp")) {
            if (is != null) {
                Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.write(tmp, new byte[0]);
            }
        }
        return tmp;
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
