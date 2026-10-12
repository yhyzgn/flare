package com.yhy.http.flare.mock.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Flare 集成测试 mock 服务应用。
 * <p>
 * 默认监听 8080 端口（可用 {@code -Dserver.port} 覆盖），为 {@code flare}
 * 模块的集成测试提供 GET/POST 模拟端点；由 {@code scripts/run-tests.sh}
 * 启动并配合测试 JVM 使用。
 * <p>
 * Created on 2025-09-16 11:46
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class Application {

    /**
     * 应用入口。
     *
     * @param args 命令行参数，透传给 Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
