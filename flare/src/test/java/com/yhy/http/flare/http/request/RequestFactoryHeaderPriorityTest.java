package com.yhy.http.flare.http.request;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.method.Get;
import com.yhy.http.flare.model.HttpHeader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RequestFactory 请求头优先级测试。
 *
 * @author Neo
 * @version 1.0.0
 * @since 2.0.2
 */
public class RequestFactoryHeaderPriorityTest {

    /**
     * 参数级请求头应覆盖方法级和构建器全局请求头。
     *
     * @throws Exception 调用异常
     */
    @Test
    public void parameterHeaderOverridesMethodAndBuilderHeaders() throws Exception {
        Flare flare = new Flare.Builder()
            .baseUrl("http://localhost:8080")
            .header("X-Scope", "builder")
            .header("X-Builder-Only", "builder-only")
            .build();
        Method method = HeaderPriorityApi.class.getDeclaredMethod("parameterWins", String.class);

        Request request = RequestFactory.parseAnnotations(flare, method).create(new OkHttpClient.Builder(), new Object[]{"parameter"});

        assertEquals("parameter", request.header("X-Scope"));
        assertEquals("builder-only", request.header("X-Builder-Only"));
    }

    /**
     * 方法级动态请求头应覆盖构建器全局动态请求头。
     *
     * @throws Exception 调用异常
     */
    @Test
    public void methodDynamicHeaderOverridesBuilderDynamicHeader() throws Exception {
        Flare flare = new Flare.Builder()
            .baseUrl("http://localhost:8080")
            .header(method -> HttpHeader.of("X-Dynamic", "builder"))
            .build();
        Method method = HeaderPriorityApi.class.getDeclaredMethod("methodDynamicWins");

        Request request = RequestFactory.parseAnnotations(flare, method).create(new OkHttpClient.Builder(), new Object[0]);

        assertEquals("method", request.header("X-Dynamic"));
    }

    private interface HeaderPriorityApi {

        /**
         * 参数请求头覆盖方法请求头。
         *
         * @param scope 请求头值
         * @return 响应
         */
        @Get("/index")
        @Header(pairName = "X-Scope", pairValue = "method")
        String parameterWins(@Header("X-Scope") String scope);

        /**
         * 方法动态请求头覆盖全局动态请求头。
         *
         * @return 响应
         */
        @Get("/index")
        @Header(dynamic = MethodDynamicHeader.class)
        String methodDynamicWins();
    }

    /**
     * 方法级动态请求头。
     */
    public static class MethodDynamicHeader implements Header.Dynamic {

        /**
         * 构造动态请求头。
         *
         * @param method 动态代理类中的接口方法反射对象
         * @return 请求头
         */
        @Override
        public HttpHeader header(Method method) {
            return HttpHeader.of("X-Dynamic", "method");
        }
    }
}
