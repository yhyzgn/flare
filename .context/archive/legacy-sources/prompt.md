# Flare Legacy Handover Prompt

_Last updated: 2026-06-23_

## 0. Mandatory first-read rule

Any AI or human contributor taking over this repository must read this file, then the files under `./.memory/`, before changing Java, Gradle, CI, or release code.

This is an existing legacy-style multi-module library. Do not perform broad refactors, dependency churn, package moves, annotation contract changes, or test rewrites without an explicit incremental task prompt and a rollback point.

## 1. Current system state

- Repository: `flare`, upstream `git@github.com:yhyzgn/flare.git`, branch `main` tracking `origin/main`.
- Build system: Gradle multi-project build with wrapper `gradle-8.14.4`.
- Java baseline: Java 21 (`sourceCompatibility`/`targetCompatibility` both set to `JavaVersion.VERSION_21`).
- Publish coordinates: `group = com.yhyzgn.http`, `version = 2.0.0` in `ext.gradle`.
- Primary product: annotation-driven Java HTTP client based on OkHttp, Jackson/Gson conversion, and dynamic proxy interfaces.
- Spring product: Spring helper module plus Spring Boot starter that scans `@Flare` interfaces and registers proxy beans.
- Database: none identified. No persistence layer or datasource configuration was found.
- Middleware/runtime dependencies: OkHttp, Spring/Spring Boot Web/Autoconfigure, Jackson, Gson, Guava, Caffeine, Lombok, SLF4J, Apache Commons, Transmittable Thread Local, JRebel plugin.
- Local OMX state: `.omx/` is ignored through `.git/info/exclude` and is not part of source control.

## 2. Module topology

| Module | Type | Role | Depends on |
| --- | --- | --- | --- |
| `flare` | Java library | Core HTTP client, annotations, proxy, request builder, converters, call adapters, tests | OkHttp, Jackson, Gson, Guava, Caffeine, Commons, TTL, Lombok |
| `flare-spring` | Java/Spring library | Spring-aware converters and delegates | `flare`, Spring Core/Context/Beans, Boot autoconfigure |
| `flare-spring-starter-abstract` | Java library | Shared Spring `ImportBeanDefinitionRegistrar` and `FactoryBean` registration foundation | `flare`, `flare-spring`, Spring |
| `flare-spring-boot-starter` | Spring Boot starter | `@EnableFlare`, `@Flare`, auto-registration and default imports | `flare-spring-starter-abstract`, Boot starter web/autoconfigure |
| `flare-mock-server` | Spring Boot app | Test mock endpoints on port `8080` | Boot web/autoconfigure |
| `flare-spring-boot-sample` | Spring Boot app | Starter sample app on port `8082` calling mock server | `flare-spring-boot-starter`, Boot web/actuator |

## 3. Core business boundary

This repository is a library/runtime integration project, not an application with persistent domain data.

Core business responsibilities:

1. Let users define HTTP remote APIs as Java interfaces with annotations such as `@Get`, `@Post`, `@Query`, `@Path`, `@Body`, `@Field`, `@Multipart`, `@Binary`, `@Header`, `@Interceptor`, `@Download`, and exception annotations.
2. Turn those interfaces into dynamic proxies through `Flare.Builder().baseUrl(...).build().create(Api.class)`.
3. Parse interface/method/parameter annotations into an OkHttp `Request`.
4. Convert request and response bodies with Jackson by default, optional Gson, Spring `ObjectMapper`, raw `ResponseBody`, `byte[]`, `InputStream`, and `File` handling.
5. Support dynamic headers, interceptors, SSL customization, timeout configuration, virtual-thread dispatching, and exception resolver dispatch.
6. Provide Spring Boot registration so `@EnableFlare` can scan `@Flare` interfaces and inject proxy beans.

Out of scope unless a future prompt says otherwise:

- Adding database persistence.
- Replacing OkHttp.
- Replacing Gradle or publishing infrastructure.
- Changing public annotation semantics in a breaking way.
- Reorganizing package names or module boundaries.

## 4. Core call chain

Core non-Spring path:

1. User creates `Flare.Builder`, sets `baseUrl`, optional headers/interceptors/timeouts/SSL/converters.
2. `Flare.Builder.build()` fills defaults: `GuavaCallAdapter`, `JacksonConverterFactory`, `StringConverterFactory`, `FormFieldConverterFactory`, constructor-based delegates, `VirtualThreadDispatcherProvider`, and OkHttp builder configuration.
3. `Flare.create(Api.class)` validates the interface and returns a Java dynamic proxy.
4. Proxy invocation calls `HttpHandlerAdapter.parseAnnotations(...)`.
5. `RequestFactory.parseAnnotations(...)` parses HTTP method annotations, method headers/interceptors/base URL, and parameter handlers.
6. `OkCaller.createRawCall()` creates a cloned OkHttp builder, asks `RequestFactory` for a request, then executes/enqueues the OkHttp call.
7. `OkCaller.parseResponse(...)` applies status handling and response body conversion.
8. `GuavaCallAdapter` currently wraps asynchronous OkHttp callback execution in a `ListenableFuture` and blocks with `future.get()` for the final return.

Spring Boot path:

1. `@EnableFlare` imports `FlareAutoRegister`, auto configuration, converter/delegate components, and `SpringDispatcherProvider`.
2. `AbstractFlareAutoRegister` reads global annotation attributes and scans base packages for `@Flare` interfaces.
3. For each candidate interface, it registers a `FlareFactoryBean` bean definition with base URL, headers, interceptors, timeout, SSL, logging, and `ignoreHttpStatus` properties.
4. `FlareFactoryBean` resolves Spring beans/delegates lazily, builds a `Flare` instance, and returns the proxy target.

## 5. Build, test, and run commands

Use these commands from repository root.

### Build / compile

```bash
./gradlew clean compileJava
./gradlew build -x test
```

### Tests

Core integration tests need the mock server on port `8080`.

```bash
# Recommended helper: starts mock server, waits for /get/index, then runs :flare:test
./scripts/run-tests.sh

# If mock server is already running
./gradlew :flare:test
```

Current test coverage is concentrated in `flare/src/test/java` only:

- `FlareGetTest`: GET, query, path, object query, raw body/bytes/inputstream/file/download.
- `FlarePostTest`: POST form, object form, JSON body, multipart file/bytes/stream/form, binary upload.

No tests were found in the Spring integration, starter, mock server, or sample modules.

### Run services

```bash
# Mock server, port 8080
./gradlew :flare-mock-server:bootRun

# Spring Boot sample, port 8082; expects mock server at http://localhost:8080
./gradlew :flare-spring-boot-sample:bootRun
```

### Publishing

- `publish` and `publish-local` shell scripts exist, but both contain `echopublishing...` typo-like lines; treat them as risky until validated.
- GitHub Actions release workflow triggers on `v*` tags, updates `ext.gradle` and `Version.java`, then publishes to Maven Central with Vanniktech plugin credentials.

## 6. Historical constraints and implicit contracts

- Public annotations are the API surface. Any change to annotation defaults or parsing behavior is potentially breaking.
- Parameter names matter because Gradle adds `-parameters`; unannotated parameters and annotations without explicit values can use Java parameter names.
- Generic type validation is intentional in request and response parsing. Raw `Map`, raw `Iterable`, wildcard, and unresolved types can fail early.
- `@Flare` interfaces must be interfaces. Generic API interfaces are rejected.
- Tests assume mock server endpoints and ports (`localhost:8080`) unless using the helper script.
- Download examples still contain absolute `/home/neo/Downloads/...` paths in API annotations and sample controllers; treat them as local-environment coupling.
- README badges mention Spring Boot `3.5.5`, while `build.gradle` applies Spring Boot `4.1.0`. Treat docs/version mismatch as a known documentation risk.

## 7. Risk areas

| Area | Risk | Evidence/impact | First safe action |
| --- | --- | --- | --- |
| Request parsing | Annotation combinations, generic type rules, parameter name fallback | Small behavior changes can break all users | Add focused tests before changing parser logic |
| URL/path encoding | Path/query encoding and slash handling are subtle | `@Path("name")` tests use `李/万姬` | Preserve current encoded output unless tests define otherwise |
| OkHttp builder lifecycle | Interceptors are merged and builders cloned per request | Duplicate interceptor/order regressions possible | Write assertions around interceptor order before edits |
| Call adapter semantics | `GuavaCallAdapter` internally async-enqueues then blocks on `future.get()` | Return behavior may look synchronous despite adapter name | Document or test before changing async behavior |
| Spring registration | BeanDefinition aliases, primary flag, placeholder resolution, lazy delegate lookup | Startup failures may surface only in sample app | Compile and boot sample with mock server after starter edits |
| Files/streams | Multipart and binary upload stream lifecycle depends on caller-provided streams | Resource leaks or closed-stream regressions possible | Use temp files and try-with-resources in tests |
| Downloads | Absolute paths in tests/sample annotations | Non-portable and potentially writes outside repo | Convert only in a dedicated compatibility-safe task |
| Release scripts | Shell typo and Maven Central credentials | Publishing can fail or mutate versions | Do not run publish in normal validation |

## 8. Incremental plan

1. **Handover baseline**: maintain `prompt.md`, `.memory/`, and `.prompt/`; verify compile/test/run without source edits.
2. **CI-safe tests**: remove or isolate absolute local paths in tests/sample through temp-directory-based fixtures, preserving public APIs.
3. **Spring starter smoke tests**: add minimal Spring context/sample startup tests for `@EnableFlare` scanning and proxy bean creation.
4. **Request parser characterization**: add tests for generic rejection, default parameter name behavior, duplicate `@Tag`, method annotation exclusivity, and interceptor/header order.
5. **Documentation correction**: align README version claims, run commands, jar paths, and known prerequisites with actual Gradle config.
6. **Only then consider refactors**: any code cleanup must preserve public API and be covered by tests first.

## 9. Acceptance standard for future work

Before modifying old code:

- State the exact risk and rollback point.
- Identify the smallest source/test surface touched.
- Add or confirm characterization tests for changed behavior.

After modifying code:

- Run compile.
- Run relevant tests.
- Run a smoke service if runtime behavior changed.
- Report verification evidence and known risks.
- Commit with reason, impact scope, validation result, and known risk.
- Push only after local verification passes and no unrelated files are included.

## 10. Handover validation result (2026-06-23)

The first handover increment completed with no Java production source changes. Minimal tooling/test changes were required to make the advertised verification commands actually runnable in this environment.

Changed code/tooling assets:

- `flare-spring/build.gradle`: disabled `bootJar` for a library module without a main class.
- `flare-spring-boot-starter/build.gradle`: disabled `bootJar` for a starter/library module without a main class.
- `flare/build.gradle`: forwards `flare.mock.port` into the forked test JVM.
- `scripts/run-tests.sh`: executable, auto-selects a free mock port when 8080 is occupied, passes the port to mock server/tests, removes known download-test artifacts, and stops mock server on exit.
- `flare/src/test/java/.../MockGetApi.java` and `MockPostApi.java`: test base URLs can read `-Dflare.mock.port`.
- `flare/src/test/java/.../FlarePostTest.java`: fallback upload sample is non-empty when `/samples/sample1.webp` is absent.

Verification passed:

```bash
git diff --check
./gradlew clean compileJava
./gradlew build -x test
./scripts/run-tests.sh
```

Observed environment note: on 2026-06-23, port 8080 was occupied by an unrelated Java debug process from `/home/neo/Projects/recycloud/strip/ops`; the helper script selected port 18080 and all 24 `flare` integration tests passed.


## 11. Spring Boot 4 / Jackson 3 migration state (2026-06-23)

Current migration work is intentionally left uncommitted for human review.

Key changes staged in the working tree:

- Project version is now `2.0.0` in `ext.gradle` and `Version.java`.
- Spring Boot Gradle plugin is `4.1.0`; Spring Framework property is `7.0.8`.
- Boot web starter usages were changed from `spring-boot-starter-web` to `spring-boot-starter-webmvc`.
- Jackson core/databind imports moved from `com.fasterxml.jackson.databind.ObjectMapper` to `tools.jackson.databind.json.JsonMapper`; Jackson annotations still use the `com.fasterxml.jackson.annotation` namespace, as required by Jackson 3 artifacts.
- Spring converter was renamed from `ObjectMapperConverterFactory` to `JsonMapperConverterFactory`; `FlareFactoryBean` now resolves `JsonMapper`.
- Default non-Spring `Flare.Builder` now creates `JsonMapper.builderWithJackson2Defaults().build()` to reduce Jackson 2 -> 3 behavior drift.
- Sample `Res` DTO has explicit `@JsonCreator` / `@JsonProperty` constructor metadata because Jackson 3 does not infer its private final-field constructor.
- GitHub Release workflow now uses release name `${{ env.TAG_NAME }}` (for tags like `v2.0.0`) without the old `Release ` prefix.
- Vanniktech publish plugin 0.37.0 required the `mavenPublishing.configure(new JavaLibrary(...))` call to avoid the old Groovy closure coercion failure.

Validation already passed after migration:

```bash
git diff --check
./gradlew clean compileJava
./gradlew build -x test
./scripts/run-tests.sh
# smoke:
# mock:   ./gradlew :flare-mock-server:bootRun --args='--server.port=18080'
# sample: ./gradlew :flare-spring-boot-sample:bootRun --args='--server.port=18082 --flare.remote-host=http://localhost:18080 --flare.download-dir=/tmp'
# curl -fsS http://localhost:18082/get/index
# curl -fsS -X POST http://localhost:18082/post/index
```

Smoke responses observed:

```json
{"code":0,"message":"OK","data":"GET 请求 /get/index"}
{"code":0,"message":"OK","data":"POST 请求 /post/index"}
```

Known review risks:

- Boot 4.1 BOM resolves Jackson 3 to `3.1.4`; do not force `3.2.0` unless the whole Boot BOM is verified with that override.
- Jackson 3 no longer constructs private final-field DTOs without explicit creator metadata; downstream users may need `@JsonCreator`, records, public constructors, or custom mapper configuration.
- `AGENTS.md` is untracked local orchestration context and should not be included in release commits unless intentionally adopted.
