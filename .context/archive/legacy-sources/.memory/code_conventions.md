# Code Conventions Memory

Date: 2026-06-23

## Language and build conventions

- Java 21 source/target.
- Gradle Groovy DSL.
- UTF-8 Java compile encoding.
- Compiler uses `-parameters`; do not remove this because runtime annotation parsing may depend on parameter names.
- Lombok is used for logging and model boilerplate; annotation processing is configured per module.
- Public library modules create normal jars, sources jars, and javadoc jars. Sample/mock modules disable publishing tasks.

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

## API compatibility rules

- Treat annotation names, defaults, and parameter meanings as public API.
- Treat `Flare.Builder` methods as public API.
- Treat model records/classes such as `InternalResponse`, `HttpHeader`, and `Invocation` as externally visible if exported from `flare`.
- Avoid changing method signatures or package names without a migration prompt.

## Testing conventions

- Existing tests are integration-style JUnit tests in `flare/src/test/java`.
- Tests use `Assert.isTrue(res.ok(), res.message())` rather than fluent assertions.
- `scripts/run-tests.sh` starts mock server and then runs `:flare:test`; prefer it for full test behavior.
- Use temp files for new file/upload/download tests. Do not introduce new absolute developer-machine paths.

## Documentation conventions

- Keep docs bilingual-aware: root/module READMEs exist in English and Chinese.
- `prompt.md` is the AI handover entry point.
- `.memory/` stores durable project memory and constraints.
- `.prompt/` stores staged task prompts. Append a new numbered prompt per phase and repair the chain when rework changes assumptions.


## Release conventions

- Current intended Flare version for the Boot 4 migration line is `2.0.0`.
- GitHub release names should be exactly the tag, e.g. `v2.0.0`, without a `Release ` prefix.
- Do not run Maven Central publish tasks during local migration validation.
