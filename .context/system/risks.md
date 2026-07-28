# 风险登记

## R-01：注解解析与参数处理（高）

- 证据：`flare/src/main/java/com/yhy/http/flare/http/request/RequestFactory.java`、`flare/src/main/java/com/yhy/http/flare/http/request/param/ParameterHandler.java`、`flare/src/main/java/com/yhy/http/flare/http/request/RequestBuilder.java`。
- 影响范围：HTTP 注解组合、参数名回退、查询参数和表单参数绑定、路径编码。
- 触发条件：修改 `@Field` / `@Multipart` / `@Body` / `@Binary` 语义或参数解析顺序。
- 失败模式：已有 API 生成的请求结构变化，导致全量用户请求失效。
- 缓解措施：先补特征化测试，再改解析逻辑；保持参数名和编码行为稳定。
- 回滚点：恢复解析实现或删除本次变更并回退到最近可工作的提交。

## R-02：调用适配与同步/异步语义（中）

- 证据：`flare/src/main/java/com/yhy/http/flare/http/HttpHandlerAdapter.java`、`flare/src/main/java/com/yhy/http/flare/such/adapter/GuavaCallAdapter.java`。
- 影响范围：返回类型适配、HTTP 非 2xx 处理、同步阻塞语义。
- 触发条件：修改 `CallAdapter`、`InternalResponse` 或异常传播逻辑。
- 失败模式：表面异步、实际阻塞或异常类型变化。
- 缓解措施：先加同步/异步特征化测试，再动适配层。
- 回滚点：恢复调用适配器实现和相关测试。

## R-03：OkHttp 构建器与拦截器生命周期（高）

- 证据：`Flare.Builder.build()`、`RequestFactory.create(...)`、`OkCaller.newBuilder()`。
- 影响范围：全局/局部拦截器顺序、SSL 注入、请求级别 builder 复用。
- 触发条件：调整拦截器合并顺序或 builder 生命周期。
- 失败模式：认证、日志或重试行为改变。
- 缓解措施：修改前先断言拦截器顺序和 builder 复用策略。
- 回滚点：恢复原合并顺序与 SSL 注入逻辑。

## R-04：Spring 扫描与 FactoryBean 生命周期（高）

- 证据：`flare-spring-starter-abstract/src/main/java/com/yhy/http/flare/spring/starter/register/AbstractFlareAutoRegister.java`、`flare-spring-starter-abstract/src/main/java/com/yhy/http/flare/spring/starter/register/FlareFactoryBean.java`、`flare-spring-boot-starter/src/main/java/com/yhy/http/flare/spring/starter/annotation/EnableFlare.java`。
- 影响范围：`@Flare` 扫描、Bean 别名、懒加载委托、Spring 环境解析。
- 触发条件：修改 starter 扫描、BeanDefinition 组装或默认基础设施 Bean。
- 失败模式：sample 启动或自动装配失败。
- 缓解措施：starter 改动后必须跑编译并做 sample/mock smoke。
- 回滚点：恢复扫描注册与默认 Bean 导入。

## R-05：文件、流和下载（中）

- 证据：`flare/src/test/java/com/yhy/http/flare/test/remote/MockGetApi.java`、`flare/src/test/java/com/yhy/http/flare/test/remote/MockPostApi.java`、`flare/src/test/java/com/yhy/http/flare/test/FlarePostTest.java`。
- 影响范围：上传流、下载落盘、临时文件清理。
- 触发条件：修改 `InputStream` / `MultipartFile` / 下载路径处理。
- 失败模式：流被提前消费、下载写到仓库外、测试残留文件。
- 缓解措施：仅用临时文件和显式清理；不要在日志里提前读一遍 one-shot body。
- 回滚点：恢复原流处理并删掉相关测试改动。

## R-06：发布与文档漂移（中）

- 证据：`README.md`、`README_zh.md`、`.github/workflows/gradle-publish.yml`、`publish`、`publish-local`。
- 影响范围：版本徽章、Release 名称、Maven Central 发布路径、版本替换脚本。
- 触发条件：修改版本、tag 规则或发布工作流。
- 失败模式：README、GitHub Release 和实际版本不一致，或发布流程失效。
- 缓解措施：发布前先核对 tag、版本号和工作流参数，正常接手阶段不要跑发布脚本。
- 回滚点：恢复工作流和脚本中的版本相关改动。

## 迁移来源原文（历史留存）

### 历史迁移附录

### .memory/risk_register.md U-9a70097e19b1-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-9a70097e19b1-01" sha256="9a70097e19b140ebf005541f4c2bd7d71bcd2fc0e4eb0381bc734d146f427f94" -->
# Risk Register / 雷区备忘录

Date: 2026-06-23

### .memory/risk_register.md U-5c5190b31561-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-5c5190b31561-01" sha256="5c5190b315612c5206068154940c781292bddbd65dfb59bbcd48de57911a7e1e" -->
## High-risk code paths

### .memory/risk_register.md U-2fd865058761-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-2fd865058761-01" sha256="2fd865058761c54ee73c52aef0e2b47efca3fe7e84a48c72c99ba8e6193336e0" -->
### 1. Annotation parser and parameter handling

Files:

- `flare/src/main/java/com/yhy/http/flare/http/request/RequestFactory.java`
- `flare/src/main/java/com/yhy/http/flare/http/request/param/ParameterHandler.java`
- `flare/src/main/java/com/yhy/http/flare/http/request/RequestBuilder.java`

Risks:

- Raw `Map`/`Iterable` and unresolved generic types intentionally fail early.
- Unannotated parameters become query parameters using Java parameter names.
- `@Field` requires `@FormData` or `@X3WFormUrlEncoded`; `@Multipart` requires `@FormData`; `@Body` and `@Binary` reject form encodings.
- Path/query encoding and relative URL slash behavior are subtle and covered only indirectly.

Safe rule: add characterization tests before changing parser behavior.

### .memory/risk_register.md U-01105a43cb86-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-01105a43cb86-01" sha256="01105a43cb863bf65fd153125a13063690fcbbfc2fe2c6824b06682572dd547b" -->
### 2. Generic return and call adapter behavior

Files:

- `flare/src/main/java/com/yhy/http/flare/http/HttpHandlerAdapter.java`
- `flare/src/main/java/com/yhy/http/flare/such/adapter/GuavaCallAdapter.java`

Risks:

- Unresolvable method return types are rejected.
- `GuavaCallAdapter` blocks on `future.get()` even though it uses `ListenableFuture` internally.
- HTTP non-2xx becomes `HttpException` unless `ignoreHttpStatus` is enabled or caller returns `InternalResponse<T>`.

Safe rule: do not change sync/async semantics without explicit tests.

### .memory/risk_register.md U-e65b7713434f-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-e65b7713434f-01" sha256="e65b7713434f44dc70ece8f04da2c34802c027b1c2d7f037f10a414e37be6d5a" -->
### 3. OkHttp client and interceptor lifecycle

Files:

- `Flare.Builder.build()`
- `RequestFactory.create(...)`
- `OkCaller.newBuilder()`

Risks:

- Global and local interceptors are merged, reversed/ordered, and added both at build time and per-call builder creation.
- Reordering can break auth/logging behavior.
- SSL uses custom factory/manager/verifier only when all are provided.

Safe rule: assert interceptor order before editing.

### .memory/risk_register.md U-820158aef6e3-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-820158aef6e3-01" sha256="820158aef6e35cc3409426c8203be38e0b32094286be5d2d4c906167bfdf1912" -->
### 4. Spring scanner and FactoryBean lifecycle

Files:

- `flare-spring-starter-abstract/.../AbstractFlareAutoRegister.java`
- `flare-spring-starter-abstract/.../FlareFactoryBean.java`
- `flare-spring-boot-starter/.../EnableFlare.java`

Risks:

- `@Flare` only supports interfaces.
- Placeholder resolution happens through Spring `Environment`.
- Bean aliases use qualifier/name/className fallback.
- Missing infrastructure beans may return null after logged `NoSuchBeanDefinitionException`, causing later failures.
- `@EnableFlare` imports specific default infrastructure beans.

Safe rule: after starter edits, run compile and at least a sample/context smoke check.

### .memory/risk_register.md U-db304d5a701a-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-db304d5a701a-01" sha256="db304d5a701a1c652f1b98c88c714058dd623eddfe942db1c9d9368f6b2f5cd4" -->
### 5. Files, streams, and downloads

Files/examples:

- `MockGetApi` download annotations contain `/home/neo/Downloads/...`.
- `flare-spring-boot-sample` upload controllers use `/home/neo/Downloads/sample1.webp`.
- Multipart `InputStream` bodies are caller-owned and read during request write.

Risks:

- Non-portable tests/sample behavior.
- Resource lifecycle issues if streams close too early/late.
- Downloads can write outside repo.

Safe rule: future fixes should switch to temp/project-configured paths in a dedicated compatibility-safe phase.

### .memory/risk_register.md U-c80558c9bae8-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-c80558c9bae8-01" sha256="c80558c9bae8451171e7c2af6e4c4410ddecbf7da6e980673e2f3fa6977e9289" -->
### 6. Release and documentation drift

Risks:

- README badges mention Spring Boot `3.5.5`, while root Gradle plugin is `3.5.11`.
- `publish` and `publish-local` contain `echopublishing...`, likely a shell typo.
- GitHub release workflow mutates `ext.gradle` and `Version.java` on tags.

Safe rule: do not run publish scripts during normal handover validation.

### .memory/risk_register.md U-41f403823f09-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-41f403823f09-01" sha256="41f403823f091e9fa6e5f5f5d45ea96ed7963f481be0f893fb1356c007abc658" -->
### 7. Build packaging tasks for library modules

Files:

- `flare-spring/build.gradle`
- `flare-spring-boot-starter/build.gradle`

Risk discovered during handover validation:

- Applying the Spring Boot plugin to library/starter modules makes Gradle create `bootJar` tasks.
- Library modules do not have a main class, so `./gradlew build -x test` can fail while resolving `:flare-spring:bootJar` or `:flare-spring-boot-starter:bootJar`.

Current safe baseline:

- `bootJar { enabled = false }` is set for `flare-spring` and `flare-spring-boot-starter`.
- Normal `jar` remains enabled for publishable library artifacts.

Rollback point: remove those two `bootJar` disable blocks if future packaging requirements change and add explicit main class or plugin separation.

### .memory/risk_register.md U-4fb763b42653-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-4fb763b42653-01" sha256="4fb763b426531de6f56f8b997b2cfa172a9e6720e8bdc68c97310ab6adb4b16a" -->
### 8. Mock-server test script and port ownership

File:

- `scripts/run-tests.sh`

Risk discovered during handover validation:

- The script was not executable and failed as `Permission denied` when run as `./scripts/run-tests.sh`.
- If port `8080` is occupied by an unrelated service, the script used to wait against whatever responded at `/get/index`, hiding the real root cause.

Current safe baseline:

- Script mode is executable.
- Script checks whether port `8080` is already listening before starting `:flare-mock-server:bootRun` and exits with port-owner evidence when occupied.

Environment observation on 2026-06-23:

- Port `8080` was occupied by a Java debug process whose cwd was `/home/neo/Projects/recycloud/strip/ops`. The test helper now avoids this by selecting a free fallback port and forwarding it to the mock server and test JVM.

### .memory/risk_register.md U-2f4ff34fbc29-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-2f4ff34fbc29-01" sha256="2f4ff34fbc29ae46185e30025cbf4c208cac53090fb483845c57bc19e24ce0e9" -->
### 9. Test fixture portability and cleanup

Files:

- `flare/src/test/java/com/yhy/http/flare/test/remote/MockGetApi.java`
- `flare/src/test/java/com/yhy/http/flare/test/remote/MockPostApi.java`
- `flare/src/test/java/com/yhy/http/flare/test/FlarePostTest.java`
- `flare/build.gradle`
- `scripts/run-tests.sh`

Risk discovered during handover validation:

- Tests hard-coded mock server port `8080`, which conflicts with other local services.
- `-Dflare.mock.port` passed to Gradle does not automatically reach the forked `test` JVM unless `test { systemProperty ... }` forwards it.
- If classpath sample resource `/samples/sample1.webp` is missing, writing a zero-byte fallback file causes binary upload tests to fail because Spring treats an empty request body as missing.
- Download tests write fixed files under `${HOME}/Downloads`; existing files can make `@Download(overwrite = false)` tests fail.

Current safe baseline:

- Test APIs read `System.getProperty("flare.mock.port", "8080")`.
- `flare/build.gradle` forwards `flare.mock.port` into the Gradle test JVM.
- `scripts/run-tests.sh` auto-selects a free port if default 8080 is occupied, passes that port to mock server and tests, cleans known download-test artifacts, and traps exit to stop the mock server.
- `FlarePostTest#createTempSampleFile` writes non-empty fallback bytes when the classpath sample file is absent.

### .memory/risk_register.md U-7185311d1fa7-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-7185311d1fa7-01" sha256="7185311d1fa72fdce2a0e6a9fe4cbebcc36c6dc9e33f2dc56e556de45aae0570" -->
### 10. Spring Boot 4 / Jackson 3 migration

Files:

- `flare/src/main/java/com/yhy/http/flare/Flare.java`
- `flare/src/main/java/com/yhy/http/flare/such/convert/JacksonConverterFactory.java`
- `flare-spring/src/main/java/com/yhy/http/flare/spring/convert/JsonMapperConverterFactory.java`
- `flare-spring-starter-abstract/.../FlareFactoryBean.java`
- `flare-spring-boot-sample/.../model/Res.java`

Risks:

- `ObjectMapper` -> `JsonMapper` is not a pure rename; Jackson 3 constructor inference is stricter. Private final-field classes without explicit creator metadata can fail at runtime with `InvalidDefinitionException`.
- Jackson annotations remain `com.fasterxml.jackson.annotation` in Jackson 3, while core/databind imports move to `tools.jackson.*`; do not mass-rename annotations to `tools.jackson.annotation`.
- Spring Boot 4 starter split requires MVC apps to use `spring-boot-starter-webmvc`; using the old starter name may hide dependency shape changes.
- Boot 4.1 BOM resolves Jackson 3 to `3.1.4`; forcing a newer Jackson line should be a separate compatibility task.

Safe rule: after any Jackson/Spring starter change, run compile, `build -x test`, `scripts/run-tests.sh`, and sample GET/POST smoke through the starter proxy.

### .memory/risk_register.md U-fb7ebdf77638-01
<!-- ctx-migration source=".memory/risk_register.md" unit="U-fb7ebdf77638-01" sha256="fb7ebdf77638c1151381663278531ab2ac13d5b852ebe814cf699724a82a5fe6" -->
### 10. Logging interceptor and one-shot request bodies

Files:

- `flare/src/main/java/com/yhy/http/flare/such/interceptor/HttpLoggerInterceptor.java`
- `flare/src/main/java/com/yhy/http/flare/http/request/RequestBuilder.java`
- `flare/src/main/java/com/yhy/http/flare/http/request/param/ParameterHandler.java`

Risk discovered on 2026-06-24:

- `HttpLoggerInterceptor` previously logged request bodies by calling `RequestBody.writeTo(Buffer)` before `chain.proceed(request)`.
- Multipart `InputStream` parts are one-shot. Pre-reading a `multipart/form-data` body consumes the caller-provided stream, so the server can still parse multipart headers and `filename`, but `MultipartFile#getSize()` and the actual stream content become `0`.

Current safe baseline:

- `HttpLoggerInterceptor` skips `RequestBody#isOneShot()` bodies; repeatable `multipart/*` field-only forms and `application/x-www-form-urlencoded` forms may still be rendered for logging.
- InputStream-backed multipart and binary request bodies override `isOneShot()` and return `true`.
- `FlarePostTest#uploadStream` asserts the server receives the same byte size as the temp file.

Safe rule: never log/debug request bodies by writing one-shot bodies before the real OkHttp send. If body inspection is needed, only inspect known repeatable bodies such as strings, bytes, files, field-only multipart forms, or x-www-form-urlencoded forms; otherwise wrap streams with an explicit buffering strategy and tests.

## R-11：配置优先级继承与覆盖（高）

- 证据：`flare-spring-starter-abstract/src/main/java/com/yhy/http/flare/spring/starter/register/AbstractFlareAutoRegister.java`、`flare-spring-boot-starter/src/main/java/com/yhy/http/flare/spring/starter/annotation/Flare.java`、`flare/src/main/java/com/yhy/http/flare/http/request/RequestFactory.java`。
- 影响范围：`@EnableFlare` 全局配置、`@Flare` 接口级配置、方法级 / 参数级 `@Header` 与动态 Header。
- 稳定规则：越靠近实际代理方法越优先；`@EnableFlare` 只作为兜底，`@Flare` 显式配置覆盖全局，方法级和参数级 Header 覆盖 Builder / Spring 外层配置。
- 触发条件：修改 Header 合并、动态 Header 顺序、`timeout` / `logEnabled` 默认值、Spring 注册器属性合并或 OkHttp Request 构建顺序。
- 失败模式：认证 Header、租户 Header、日志开关或超时配置被外层全局配置反向覆盖，导致调用方局部配置失效。
- 当前安全基线：`@Flare#timeout` 与 `@Flare#logEnabled` 使用空字符串表示继承全局配置；`RequestFactory` 先应用全局 Header，再重新应用方法/参数 Header。
- 缓解措施：修改相关逻辑时必须保留/扩展 `RequestFactoryHeaderPriorityTest` 与 `AbstractFlareAutoRegisterPriorityTest`。
- 回滚点：恢复上述三个生产文件及新增优先级测试，回到修改前的合并顺序。
