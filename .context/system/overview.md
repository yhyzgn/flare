# 系统全景：flare

## 已验证事实

- 仓库远端：`origin` 指向 `git@github.com:yhyzgn/flare.git`；当前分支 `main` 跟踪 `origin/main`。
- 构建系统：Gradle Groovy DSL 的多模块工程，包装器版本为 `9.5.1`（`gradle/wrapper/gradle-wrapper.properties`）。
- Java 基线：`build.gradle` 将 `sourceCompatibility` / `targetCompatibility` 设为 `JavaVersion.VERSION_21`。
- 版本与发布坐标：`ext.gradle` 当前版本是 `2.0.1`，group 为 `com.yhyzgn.http`；`flare/src/main/java/com/yhy/http/flare/utils/Version.java` 也写入 `2.0.1`。
- Spring / Jackson 基线：根 `build.gradle` 使用 Spring Boot Gradle 插件 `4.1.0`，`ext.gradle` 固定 Spring Framework `7.0.8` 与 Jackson `3.1.4`；核心代码与 Spring 适配代码使用 `tools.jackson.databind.json.JsonMapper`。
- 依赖栈：OkHttp BOM `5.4.0`、Gson `2.14.0`、Guava `33.6.0-jre`、JUnit `6.1.0`、Lombok `1.18.46`、SLF4J `2.0.18`、Caffeine `3.2.4`、Transmittable Thread Local `2.14.5` 等（见 `build.gradle` / `ext.gradle`）。
- 没有数据库、ORM、迁移脚本或 datasource 配置（仓库扫描未发现相关层）。

## 模块与职责

- `flare`：核心注解、动态代理、请求构建、调用适配、转换器、文件/下载工具和测试。
- `flare-spring`：Spring 适配的转换器、委托和 `JsonMapperConverterFactory`。
- `flare-spring-starter-abstract`：Spring 扫描注册与 `FlareFactoryBean` 基础层。
- `flare-spring-boot-starter`：`@EnableFlare`、`@Flare`、自动注册与默认导入。
- `flare-mock-server`：集成测试 mock 服务，默认端口 `8080`。
- `flare-spring-boot-sample`：starter 示例应用，默认端口 `8082`，通过 `flare.remote-host` 访问 mock 服务。

## 核心调用链

```text
Java 接口 API
  -> `Flare.create(Class)`
  -> Java Proxy
  -> `HttpHandlerAdapter`
  -> `RequestFactory` + `ParameterHandler[]`
  -> `RequestBuilder`
  -> `OkCaller`
  -> `OkHttpClient` / `Call`
  -> `BodyConverter` / `InternalResponse`
```

Spring 路径：

```text
@EnableFlare
  -> `FlareAutoRegister`
  -> 扫描 `@Flare` 接口
  -> 注册 `FlareFactoryBean`
  -> 构建 `Flare`
  -> 输出代理 Bean
```

## 构建 / 测试 / 运行

- 编译：`./gradlew clean compileJava`
- 构建：`./gradlew build -x test`
- 集成测试：`./scripts/run-tests.sh`（脚本会在 8080 被占用时自动挑选空闲端口并透传给 mock/test JVM）
- mock 服务：`./gradlew :flare-mock-server:bootRun`
- sample 应用：`./gradlew :flare-spring-boot-sample:bootRun`

## 覆盖现状

- 当前测试主要集中在 `flare/src/test/java`。
- 主要测试类：`FlareGetTest`、`FlarePostTest`、`HttpLoggerInterceptorTest`。
- `flare-spring`、`flare-spring-starter-abstract`、`flare-spring-boot-starter`、`flare-mock-server`、`flare-spring-boot-sample` 未发现独立测试目录。

## 外部基础设施

- `flare-mock-server/src/main/resources/application.yml` 固定端口 `8080`。
- `flare-spring-boot-sample/src/main/resources/application.yml` 固定端口 `8082`，并默认把远端主机指向 `http://localhost:8080`。
- 代码与脚本默认不依赖外部数据库或第三方在线服务。

## 迁移来源原文（历史留存）

### 历史迁移附录

### .memory/system_overview.md U-e3f9aa5865b3-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-e3f9aa5865b3-01" sha256="e3f9aa5865b32343cc022c300317d368263cf3efcfb0732da90c6313c9bdaae0" -->
# System Overview Memory

Date: 2026-06-23

### .memory/system_overview.md U-dff57b52e388-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-dff57b52e388-01" sha256="dff57b52e38843c55b888e78f9c86ab2419692fa5a0f7d349a980d692383ae35" -->
## Identity

Flare is a Java 21 Gradle multi-module HTTP client library. It creates OkHttp-backed dynamic proxies from annotated Java interfaces and ships Spring/Spring Boot integration modules.

### .memory/system_overview.md U-c9e434d90732-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-c9e434d90732-01" sha256="c9e434d9073236d2e383abb2a7b44eed521c6eadc91e9fff19f8a1be168238b6" -->
## Modules

- `flare`: core annotations, dynamic proxy, request factory, request builder, OkHttp caller, converters, call adapter, exception dispatch, tests.
- `flare-spring`: Spring converter/delegate implementations.
- `flare-spring-starter-abstract`: common scanner/registrar and `FlareFactoryBean`.
- `flare-spring-boot-starter`: `@EnableFlare`, `@Flare`, starter auto-configuration.
- `flare-mock-server`: Spring Boot mock server on port `8080` for integration tests.
- `flare-spring-boot-sample`: sample app on port `8082` that calls the mock server.

### .memory/system_overview.md U-73dbc9b6a16e-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-73dbc9b6a16e-01" sha256="73dbc9b6a16ec7b73c02fcf95a7deb3a46a15160b9166d73ad3709627eeb5289" -->
## Runtime topology

```text
User API interface
  -> Flare.create(Class)
  -> Java Proxy
  -> HttpHandlerAdapter
  -> RequestFactory + ParameterHandler[]
  -> RequestBuilder
  -> OkCaller
  -> OkHttpClient / OkHttp Call
  -> BodyConverter / InternalResponse
```

Spring topology:

```text
@EnableFlare
  -> FlareAutoRegister
  -> scan @Flare interfaces
  -> register FlareFactoryBean
  -> build Flare with Spring delegates/converters
  -> expose proxy bean
```

### .memory/system_overview.md U-744dacd2f806-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-744dacd2f806-01" sha256="744dacd2f806720a54136fb0ddff69deeb499ec40fa02b14a4bd0092ef9f4fe2" -->
## Build facts

- Gradle wrapper: 8.14.4.
- Java: 21.
- Spring Boot plugin: 4.1.0.
- Spring framework version property: 7.0.8.
- OkHttp BOM: 5.4.0.
- Jackson: 3.1.4 (`tools.jackson.*`, `JsonMapper`; annotations remain `com.fasterxml.jackson.annotation`).
- Gson: 2.14.0.
- JUnit: 6.1.0.
- Lombok: 1.18.46.

### .memory/system_overview.md U-539ec7bba156-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-539ec7bba156-01" sha256="539ec7bba156ed22576f87cd1f22d0fd2a7fd902a3cdd82f98fa155552a92515" -->
## Database / persistence

No database, ORM, migration, datasource, or repository layer was found in the scanned project.

### .memory/system_overview.md U-61d995e89e2a-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-61d995e89e2a-01" sha256="61d995e89e2a5287b3e5d5483b4a5e65f33de6d0d0056e79c73bfcc18a23dd96" -->
## Verification commands

- `./gradlew clean compileJava`
- `./gradlew build -x test`
- `./scripts/run-tests.sh`
- `./gradlew :flare-mock-server:bootRun`
- `./gradlew :flare-spring-boot-sample:bootRun`

### .memory/system_overview.md U-5b588847e50b-01
<!-- ctx-migration source=".memory/system_overview.md" unit="U-5b588847e50b-01" sha256="5b588847e50ba7f8635984ae581ec6ff930efacfd01a8df4fecafd1839c1874b" -->
## Spring Boot 4 migration memory

- Boot 4 starter split: use `spring-boot-starter-webmvc` for servlet MVC apps/starter paths instead of the old broad `spring-boot-starter-web`.
- Spring Boot 4.1.0 runtime uses Tomcat 11 and Spring Framework 7.0.x.
- Jackson 3 package migration: databind/core classes are under `tools.jackson.*`; annotation artifacts remain under `com.fasterxml.jackson.annotation`.
- Boot 4.1 BOM currently aligns Jackson 3 to `3.1.4`; keep project property aligned unless deliberately overriding and revalidating all Boot modules.
