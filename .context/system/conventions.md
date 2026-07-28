# 代码规范与上下文规则

## 已验证约定

- 以 Gradle Groovy DSL 为主，Java 编译使用 UTF-8，且编译参数启用 `-parameters`，因为参数名会影响无注解参数、查询参数和反射注解解析。
- 核心包根为 `com.yhy.http.flare`；注解按 `annotation.method`、`annotation.param`、`annotation.exception` 分层；内部实现主要落在 `http.request`、`http.request.param`、`such.*`、`delegate.*` 与 `spring.*`。
- `Flare.Builder`、公共注解、`InternalResponse` / `HttpHeader` / `Invocation` 等导出类型都应视作公共 API，修改前先做全引用搜索。
- 请求解析约定：`@Field` 只在表单语义下使用，`@Multipart` 只在 `@FormData` 下使用，`@Body` / `@Binary` 不能混入表单编码；原始 `Map` / `Iterable` / 未解析泛型应尽早失败。
- Spring 适配默认使用 `spring-boot-starter-webmvc`，`@EnableFlare` 负责导入扫描注册与默认基础设施。
- 测试以 JUnit 为主，当前集成测试依赖 mock 服务；新增上传 / 下载场景优先使用临时文件，避免写死本机路径。
- 发布版本当前为 `2.0.1`；GitHub Release 名称应与 tag 一致，即 `v${version}` 形式，不带 `Release ` 前缀。

## 迁移来源原文（历史留存）

### 历史迁移附录

### .memory/code_conventions.md U-7f53004a3a97-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-7f53004a3a97-01" sha256="7f53004a3a97faac9f98d0ecffe598e6131798a54c0fbd40564ccaf0c753093c" -->
# Code Conventions Memory

Date: 2026-06-23

### .memory/code_conventions.md U-928bc80fc31a-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-928bc80fc31a-01" sha256="928bc80fc31a156401344da9afdaaad51b25d6c79829752666feeee9574c29fa" -->
## Language and build conventions

- Java 21 source/target.
- Gradle Groovy DSL.
- UTF-8 Java compile encoding.
- Compiler uses `-parameters`; do not remove this because runtime annotation parsing may depend on parameter names.
- Lombok is used for logging and model boilerplate; annotation processing is configured per module.
- Public library modules create normal jars, sources jars, and javadoc jars. Sample/mock modules disable publishing tasks.

### .memory/code_conventions.md U-ba9da2c83764-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-ba9da2c83764-01" sha256="ba9da2c837644efa5e049da7f01dc51dce127615912bce49ed6816e58c18bb96" -->
## Package conventions

- Core package root: `com.yhy.http.flare`.
- Annotation packages:
  - HTTP/method annotations: `annotation.method`.
  - Parameter annotations: `annotation.param`.
  - Exception annotations: `annotation.exception`.
- Internal mechanics:
  - `http.request` and `http.request.param` parse and build requests.
  - `such.*` contains default implementation classes (converters, delegates, SSL, provider, interceptor).
  - `delegate.*` defines construction/lookup extension points.
  - Spring extensions live under `com.yhy.http.flare.spring`.

### .memory/code_conventions.md U-66377aa6be80-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-66377aa6be80-01" sha256="66377aa6be80a48a50adacfdff2f0a52d9a0ea06542345f548462ac229597a9b" -->
## API compatibility rules

- Treat annotation names, defaults, and parameter meanings as public API.
- Treat `Flare.Builder` methods as public API.
- Treat model records/classes such as `InternalResponse`, `HttpHeader`, and `Invocation` as externally visible if exported from `flare`.
- Avoid changing method signatures or package names without a migration prompt.

### .memory/code_conventions.md U-ff933b9934a1-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-ff933b9934a1-01" sha256="ff933b9934a152205c3424ea939f7f23d140fd44cf3c64083feda4300d9cbfb6" -->
## Testing conventions

- Existing tests are integration-style JUnit tests in `flare/src/test/java`.
- Tests use `Assert.isTrue(res.ok(), res.message())` rather than fluent assertions.
- `scripts/run-tests.sh` starts mock server and then runs `:flare:test`; prefer it for full test behavior.
- Use temp files for new file/upload/download tests. Do not introduce new absolute developer-machine paths.

### .memory/code_conventions.md U-2eaa26f22b47-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-2eaa26f22b47-01" sha256="2eaa26f22b4729d4d52c0dab27e0c4b0d3761ca984f6d381240f60c764d4965f" -->
## Documentation conventions

- Keep docs bilingual-aware: root/module READMEs exist in English and Chinese.
- `prompt.md` is the AI handover entry point.
- `.memory/` stores durable project memory and constraints.
- `.prompt/` stores staged task prompts. Append a new numbered prompt per phase and repair the chain when rework changes assumptions.

### .memory/code_conventions.md U-2ad46129501b-01
<!-- ctx-migration source=".memory/code_conventions.md" unit="U-2ad46129501b-01" sha256="2ad46129501bf2ad43fa86b163eee840bb7aac2c3682562515222e40f383b105" -->
## Release conventions

- Current intended Flare version for the Boot 4 migration line is `2.0.0`.
- GitHub release names should be exactly the tag, e.g. `v2.0.0`, without a `Release ` prefix.
- Do not run Maven Central publish tasks during local migration validation.
