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

    /**
     * 首页接口。
     *
     * @return 响应结果
     */
    @Post("/index")
    Res<String> index();

    /**
     * 表单接口。
     *
     * @param name 名称
     * @param age 年龄
     * @param headerValue 请求头值
     * @return 响应结果
     */
    @FormData
    @Post("/form")
    @Interceptor(FormInterceptor.class)
    @Header(pairName = "Form-Header", pairValue = "Form-Value")
    Res<String> form(@Field("name") String name, @Field("age") int age, @Header("Param-Header") String headerValue);

    /**
     * 用户表单接口。
     *
     * @param user 用户参数
     * @return 响应结果
     */
    @FormData
    @Post("/formUser")
    Res<User> formUser(@Field User user);

    /**
     * 默认表单接口。
     *
     * @param name 名称
     * @param age 年龄
     * @return 响应结果
     */
    @X3WFormUrlEncoded
    @Post("form")
    Res<String> formDefault(@Field String name, @Field int age);

    /**
     * 请求体接口。
     *
     * @param user 用户参数
     * @return 响应结果
     */
    @Post("/body")
    Res<User> body(@Body User user);

    /**
     * 文件上传接口。
     *
     * @param file 文件
     * @return 响应结果
     */
    @FormData
    @Post("/upload")
    Res<String> upload(@Multipart File file);

    /**
     * 字节上传接口。
     *
     * @param fileData 文件字节
     * @return 响应结果
     */
    @FormData
    @Post("/upload")
    Res<String> uploadBytes(@Multipart(value = "file", filename = "bytes.webp") byte[] fileData);

    /**
     * 流上传接口。
     *
     * @param file 文件输入流
     * @return 响应结果
     */
    @FormData
    @Post("/upload")
    Res<String> uploadStream(@Multipart(filename = "input-stream.webp") FileInputStream file);

    /**
     * 多段表单接口。
     *
     * @param form 表单参数
     * @return 响应结果
     */
    @FormData
    @Post("/partForm")
    Res<String> partForm(@Field PartForm form);

    // @X3WFormUrlEncoded
    // @Post("/uploadError")
    // Res<String> uploadError(@Multipart(filename = "input-stream.webp") FileInputStream file);

    /**
     * 二进制上传接口。
     *
     * @param data 二进制数据
     * @return 响应结果
     */
    @Post("/uploadBinary")
    Res<String> uploadBinary(@Binary byte[] data);

    /**
     * 二进制文件上传接口。
     *
     * @param file 文件
     * @return 响应结果
     */
    @Post("/uploadBinary")
    Res<String> uploadBinaryFile(@Binary File file);

    /**
     * 二进制输入流上传接口。
     *
     * @param inputStream 输入流
     * @return 响应结果
     */
    @Post("/uploadBinary")
    Res<String> uploadBinaryInputStream(@Binary InputStream inputStream);
}
