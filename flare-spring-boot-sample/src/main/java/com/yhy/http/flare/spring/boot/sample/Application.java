package com.yhy.http.flare.spring.boot.sample;

import com.yhy.http.flare.annotation.Header;
import com.yhy.http.flare.annotation.Interceptor;
import com.yhy.http.flare.spring.boot.sample.header.GlobalDynamicHeader;
import com.yhy.http.flare.spring.boot.sample.interceptor.GlobalInterceptor;
import com.yhy.http.flare.spring.starter.annotation.EnableFlare;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * app
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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
