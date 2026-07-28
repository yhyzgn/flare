# 003 One-Shot Multipart Logging Fix

## Objective

Fix and preserve the behavior of `@FormData` + `@Multipart InputStream` uploads when HTTP logging is enabled.

## Problem statement

A client method like:

```java
@FormData
@Post("/upload")
Res<String> uploadStream(@Multipart(filename = "input-stream.webp") FileInputStream file);
```

could reach Spring MVC as a valid multipart part with `getOriginalFilename()` populated, while `MultipartFile#getSize()` and the uploaded stream content were `0`.

## Root cause

`HttpLoggerInterceptor` rendered request bodies by calling `RequestBody.writeTo(Buffer)` before `chain.proceed(request)`. For multipart `InputStream` parts this consumes the caller-owned stream before OkHttp sends the real request.

## Implemented fix

- Skip body rendering for `multipart/*` request content in `HttpLoggerInterceptor`.
- Skip body rendering when `RequestBody#isOneShot()` is `true`.
- Mark InputStream-backed multipart and binary bodies as one-shot.
- Add a regression assertion to `FlarePostTest#uploadStream` that checks server-reported upload size matches the source temp file size.
- Extend the mock upload endpoint response to include received size for test evidence.
- Record the pitfall in `.memory/risk_register.md`.

## Validation

Required before claiming completion:

```bash
git diff --check
./gradlew compileJava compileTestJava
./scripts/run-tests.sh
```

Expected evidence:

- `FlarePostTest > uploadStream() PASSED`
- Mock server log shows `file=input-stream.webp, size=<non-zero>`.

## Acceptance criteria

- Multipart filename remains intact.
- Multipart stream body is sent exactly once during the actual OkHttp request.
- Logging remains safe for one-shot bodies and does not consume upload streams.
- Full helper test flow passes.
