# Risk Register / 雷区备忘录

Date: 2026-06-23

## High-risk code paths

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

### 2. Generic return and call adapter behavior

Files:

- `flare/src/main/java/com/yhy/http/flare/http/HttpHandlerAdapter.java`
- `flare/src/main/java/com/yhy/http/flare/such/adapter/GuavaCallAdapter.java`

Risks:

- Unresolvable method return types are rejected.
- `GuavaCallAdapter` blocks on `future.get()` even though it uses `ListenableFuture` internally.
- HTTP non-2xx becomes `HttpException` unless `ignoreHttpStatus` is enabled or caller returns `InternalResponse<T>`.

Safe rule: do not change sync/async semantics without explicit tests.

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

### 6. Release and documentation drift

Risks:

- README badges mention Spring Boot `3.5.5`, while root Gradle plugin is `3.5.11`.
- `publish` and `publish-local` contain `echopublishing...`, likely a shell typo.
- GitHub release workflow mutates `ext.gradle` and `Version.java` on tags.

Safe rule: do not run publish scripts during normal handover validation.

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
