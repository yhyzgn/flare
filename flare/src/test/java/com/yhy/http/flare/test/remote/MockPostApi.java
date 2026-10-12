package com.yhy.http.flare.test.remote;

import com.yhy.http.flare.annotation.FormData;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.X3WFormUrlEncoded;
import com.yhy.http.flare.annotation.method.Post;
import com.yhy.http.flare.annotation.param.Binary;
import com.yhy.http.flare.annotation.param.Body;
import com.yhy.http.flare.annotation.param.Field;
import com.yhy.http.flare.annotation.param.Multipart;
import com.yhy.http.flare.test.custom.AuthorizationDynamicHeader;
import com.yhy.http.flare.test.custom.TestTagInterceptor;
import com.yhy.http.flare.test.model.PartForm;
import com.yhy.http.flare.test.model.Res;
import com.yhy.http.flare.test.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 指向本地 mock-server {@code /post} 端点的 POST 测试接口。
 * <p>
 * 覆盖动态请求头、方法级拦截器、表单提交、请求体、文件上传
 * （{@code File} / {@code byte[]} / {@code FileInputStream}）、多段表单与二进制上传等场景；
 * 端口可用系统属性 {@value #MOCK_PORT_PROPERTY} 覆盖，默认 {@code 8080}。
 * <p>
 * Created on 2025-09-15 17:26
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MockPostApi {
    String MOCK_PORT_PROPERTY = "flare.mock.port";
    String BASE_URL = "http://localhost:" + System.getProperty(MOCK_PORT_PROPERTY, "8080") + "/post";

    @Post("/index")
    @Header(dynamic = AuthorizationDynamicHeader.class)
    @Interceptor(TestTagInterceptor.class)
    Res<String> index();

    @FormData
    @Post("/form")
    Res<String> form(@Field("name") String name, @Field("age") int age);

    @FormData
    @Post("/formUser")
    Res<User> formUser(@Field User user);

    @X3WFormUrlEncoded
    @Post("form")
    Res<String> formDefault(@Field String name, @Field int age);

    @Post("/body")
    Res<User> body(@Body User user);

    @FormData
    @Post("/upload")
    Res<String> upload(@Multipart File file);

    @FormData
    @Post("/upload")
    Res<String> uploadBytes(@Multipart(value = "file", filename = "bytes.webp") byte[] fileData);

    @FormData
    @Post("/upload")
    Res<String> uploadStream(@Multipart(filename = "input-stream.webp") FileInputStream file);

    @FormData
    @Post("/partForm")
    Res<String> partForm(@Field PartForm form);

    // @X3WFormUrlEncoded
    // @Post("/uploadError")
    // Res<String> uploadError(@Multipart(filename = "input-stream.webp") FileInputStream file);

    @Post("/uploadBinary")
    Res<String> uploadBinary(@Binary byte[] data);

    @Post("/uploadBinary")
    Res<String> uploadBinaryFile(@Binary File file);

    @Post("/uploadBinary")
    Res<String> uploadBinaryInputStream(@Binary InputStream inputStream);
}
