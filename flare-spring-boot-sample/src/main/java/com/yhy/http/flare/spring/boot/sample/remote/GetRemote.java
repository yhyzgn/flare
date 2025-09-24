package com.yhy.http.flare.spring.boot.sample.remote;

import com.yhy.http.flare.annotation.Download;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.annotation.param.Path;
import com.yhy.http.flare.annotation.param.Query;
import com.yhy.http.flare.spring.boot.sample.header.GetDynamicHeader;
import com.yhy.http.flare.spring.boot.sample.interceptor.GetInterceptor;
import com.yhy.http.flare.spring.boot.sample.model.Res;
import com.yhy.http.flare.spring.boot.sample.model.User;
import com.yhy.http.flare.spring.starter.annotation.Flare;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.InputStream;

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

    @Get("/index")
    Res<String> index();

    @Get("/query")
    Res<String> query(@Query("name") String name, @Query("age") int age);

    @Get("/query/{name}/{age}")
    Res<String> queryPath(@Path("name") String name, @Path("age") int age);

    @Get("/queryUser")
    Res<User> queryUser(@Query User user);

    @Get("query")
    Res<String> queryDefault(@Query String name, @Query int age);

    @Get("/index")
    ResponseBody forBody();

    @Get("/index")
    byte[] forBytes();

    @Get("/index")
    InputStream forInputStream();

    @Get("/index")
    File forFile();

    @Get("/index")
    @Download(filePath = "${flare.download-dir}/ddddddddd.txt", overwrite = true)
    File forFileDownload();

    @Get("/index")
    @Download(filePath = "${flare.download-dir}/void.txt")
    void forVoidFileDownload();
}
