package com.yhy.http.flare.test.remote;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.test.custom.AuthorizationDynamicHeader;
import com.yhy.http.flare.test.custom.TestTagInterceptor;

/**
 * <a href="https://jsonplaceholder.typicode.com/">测试站点</a>
 * <p>
 * Created on 2025-09-15 17:26
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TypeCodeApi {
    String BASE_URL = "https://jsonplaceholder.typicode.com";

    @Get("posts")
    @Header(dynamic = AuthorizationDynamicHeader.class)
    @Interceptor(TestTagInterceptor.class)
    String posts();
}
