package com.yhy.http.flare.spring.boot.sample.remote;

import com.yhy.http.flare.annotation.FormData;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.X3WFormUrlEncoded;
import com.yhy.http.flare.annotation.method.Post;
import com.yhy.http.flare.annotation.param.Binary;
import com.yhy.http.flare.annotation.param.Body;
import com.yhy.http.flare.annotation.param.Field;
import com.yhy.http.flare.annotation.param.Multipart;
import com.yhy.http.flare.spring.boot.sample.header.PostDynamicHeader;
import com.yhy.http.flare.spring.boot.sample.interceptor.FormInterceptor;
import com.yhy.http.flare.spring.boot.sample.interceptor.PostInterceptor;
import com.yhy.http.flare.spring.boot.sample.model.PartForm;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.starter.annotation.Flare;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * POST 请求远程服务
 * <p>
 * Created on 2025-09-18 14:04
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Flare(
        baseUrl = "${flare.remote-host}/post",
        timeout = "8000",
        header = {
                @Header(pairName = "Post-Header", pairValue = "Post-Value"),
                @Header(dynamic = PostDynamicHeader.class)
        },
        interceptor = {
                @Interceptor(PostInterceptor.class)
        }
)
public interface PostRemote {

    @Post("/index")
    Res<String> index();

    @FormData
    @Post("/form")
    @Interceptor(FormInterceptor.class)
    @Header(pairName = "Form-Header", pairValue = "Form-Value")
    Res<String> form(@Field("name") String name, @Field("age") int age, @Header("Param-Header") String headerValue);

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
