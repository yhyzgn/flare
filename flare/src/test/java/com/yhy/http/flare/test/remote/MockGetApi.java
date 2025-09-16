package com.yhy.http.flare.test.remote;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.annotation.param.Path;
import com.yhy.http.flare.annotation.param.Query;
import com.yhy.http.flare.test.custom.AuthorizationDynamicHeader;
import com.yhy.http.flare.test.custom.TestTagInterceptor;
import com.yhy.http.flare.test.model.Res;
import com.yhy.http.flare.test.model.User;
import okhttp3.ResponseBody;

/**
 * <a href="https://jsonplaceholder.typicode.com/">测试站点</a>
 * <p>
 * Created on 2025-09-15 17:26
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MockGetApi {
    String BASE_URL = "http://localhost:8080/get";

    @Get("/index")
    @Header(dynamic = AuthorizationDynamicHeader.class)
    @Interceptor(TestTagInterceptor.class)
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
}
