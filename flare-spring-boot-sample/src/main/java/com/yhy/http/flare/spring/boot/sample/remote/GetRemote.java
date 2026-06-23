package com.yhy.http.flare.spring.boot.sample.remote;

import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.exception.Catcher;
import com.yhy.http.flare.annotation.exception.Catchers;
import com.yhy.http.flare.annotation.exception.ErrorIgnored;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.annotation.param.Path;
import com.yhy.http.flare.annotation.param.Query;
import com.yhy.http.flare.annotation.param.Url;
import com.yhy.http.flare.spring.boot.sample.header.GetDynamicHeader;
import com.yhy.http.flare.spring.boot.sample.interceptor.GetInterceptor;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.boot.sample.resolver.TimeoutExceptionResolver;
import com.yhy.http.flare.spring.starter.annotation.Flare;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

/**
 * GET 请求远程接口
 * <p>
 * Created on 2025-09-18 11:33
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Flare(
    baseUrl = "${flare.remote-host}/get",
    header = {
        @Header(pairName = "Get-Header", pairValue = "Get-Value"),
        @Header(dynamic = GetDynamicHeader.class)
    },
    interceptor = {
        @Interceptor(GetInterceptor.class)
    }
)
public interface GetRemote {

    /**
     * 首页接口。
     *
     * @return 响应结果
     */
    @Get("/index")
    Res<String> index();

    /**
     * 指定完整 URL 的首页接口。
     *
     * @param url 完整请求地址
     * @return 响应结果
     */
    // 此时这里的相对 url 会被忽略
    @Get("/asdf")
    Res<String> indexByUrl(@Url String url);

    /**
     * 查询接口。
     *
     * @param name 名称
     * @param age 年龄
     * @return 响应结果
     */
    @ErrorIgnored
    @Get("/query")
    Res<String> query(@Query("name") String name, @Query("age") int age);

    /**
     * 路径查询接口。
     *
     * @param name 名称
     * @param age 年龄
     * @return 响应结果
     */
    @Get("/query/{name}/{age}")
    Res<String> queryPath(@Path("name") String name, @Path("age") int age);

    /**
     * 用户查询接口。
     *
     * @param user 用户参数
     * @return 响应结果
     */
    @Catcher(throwable = TimeoutException.class, resolver = TimeoutExceptionResolver.class)
    @Get("/queryUser")
    Res<User> queryUser(@Query User user);

    /**
     * 默认查询接口。
     *
     * @param name 名称
     * @param age 年龄
     * @return 响应结果
     */
    @Catchers({
        @Catcher(throwable = TimeoutException.class, resolver = TimeoutExceptionResolver.class)
    })
    @Get("query")
    Res<String> queryDefault(@Query String name, @Query int age);

    /**
     * 获取原始响应体。
     *
     * @return 原始响应体
     */
    @Get("/index")
    ResponseBody forBody();

    /**
     * 获取字节数组响应。
     *
     * @return 字节数组
     */
    @Catcher(resolver = TimeoutExceptionResolver.class)
    @Get("/index")
    byte[] forBytes();

    /**
     * 获取输入流响应。
     *
     * @return 输入流
     */
    @Get("/index")
    InputStream forInputStream();

    /**
     * 获取文件响应。
     *
     * @return 文件
     */
    @Get("/index")
    File forFile();

    /**
     * 下载文件响应。
     *
     * @return 下载后的文件
     */
    @Get("/index")
    @Download(filePath = "${flare.download-dir}/ddddddddd.txt", overwrite = true)
    File forFileDownload();

    /**
     * 执行无返回文件下载。
     */
    @Get("/index")
    @Download(filePath = "${flare.download-dir}/void.txt")
    void forVoidFileDownload();
}
