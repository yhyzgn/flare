package com.yhy.http.flare.spring.boot.sample;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.spring.boot.sample.header.GlobalDynamicHeader;
import com.yhy.http.flare.spring.boot.sample.interceptor.GlobalInterceptor;
import com.yhy.http.flare.spring.starter.annotation.EnableFlare;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 示例应用启动类，通过 {@code @EnableFlare} 注册全局固定请求头、动态请求头和全局拦截器。
 * <p>
 * 该类是 flare-spring-boot-sample 的入口，启动后监听 8082 端口，
 * 通过 {@code flare.remote-host}（默认 {@code http://localhost:8080}）调用 mock-server。
 * <p>
 * Created on 2025-09-16 11:46
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableFlare(
    timeout = "${flare.timeout}",
    header = {
        @Header(pairName = "Global-Fixed-Header", pairValue = "123456"),
        @Header(pairName = "App-Name", pairValue = "${spring.application.name}"),
        @Header(dynamic = GlobalDynamicHeader.class)
    },
    interceptor = {
        @Interceptor(GlobalInterceptor.class)
    }
)
@SpringBootApplication
public class Application {

    /**
     * 应用入口。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
