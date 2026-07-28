# 002 Spring Boot 4 Migration Review

## Objective

Human-review and, if accepted, commit the Spring Boot 4 / Jackson 3 migration currently staged in the working tree.

## Scope

Review-only first. Do not add new features or broad refactors.

Included migration surfaces:

- Dependency upgrades and Spring Boot 4.1.0 plugin move.
- Spring Framework 7.0.8 property.
- Boot starter split to `spring-boot-starter-webmvc`.
- Jackson 3 `ObjectMapper` -> `JsonMapper` migration.
- Spring converter rename to `JsonMapperConverterFactory`.
- Sample DTO `Res` explicit Jackson creator metadata.
- Flare version bump to `2.0.0`.
- GitHub Release name format changed to `v${version}` via `${{ env.TAG_NAME }}`.

## Review checklist

1. Confirm public annotation APIs and package names outside converter rename remain stable.
2. Inspect Jackson 3 behavior: annotations remain `com.fasterxml.jackson.annotation`, databind/core use `tools.jackson.*`.
3. Confirm Boot 4.1 BOM-aligned Jackson `3.1.4` is acceptable rather than forcing `3.2.0`.
4. Confirm `JsonMapper.builderWithJackson2Defaults().build()` is the desired core default.
5. Confirm GitHub Release name should be exactly the tag, e.g. `v2.0.0`.
6. Ensure untracked `AGENTS.md` is not included unless intentionally adopted.

## Required validation before commit

```bash
git diff --check
./gradlew clean compileJava
./gradlew build -x test
./scripts/run-tests.sh
./gradlew :flare-mock-server:bootRun --args='--server.port=18080'
./gradlew :flare-spring-boot-sample:bootRun --args='--server.port=18082 --flare.remote-host=http://localhost:18080 --flare.download-dir=/tmp'
curl -fsS http://localhost:18082/get/index
curl -fsS -X POST http://localhost:18082/post/index
```

## Acceptance criteria

- All validation passes.
- Reviewer accepts known Jackson 3 constructor inference risk.
- Commit message includes reason, impact scope, validation results, and known risks.
- Do not push until reviewer approves.
