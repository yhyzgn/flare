# System Overview Memory

Date: 2026-06-23

## Identity

Flare is a Java 21 Gradle multi-module HTTP client library. It creates OkHttp-backed dynamic proxies from annotated Java interfaces and ships Spring/Spring Boot integration modules.

## Modules

- `flare`: core annotations, dynamic proxy, request factory, request builder, OkHttp caller, converters, call adapter, exception dispatch, tests.
- `flare-spring`: Spring converter/delegate implementations.
- `flare-spring-starter-abstract`: common scanner/registrar and `FlareFactoryBean`.
- `flare-spring-boot-starter`: `@EnableFlare`, `@Flare`, starter auto-configuration.
- `flare-mock-server`: Spring Boot mock server on port `8080` for integration tests.
- `flare-spring-boot-sample`: sample app on port `8082` that calls the mock server.

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

## Database / persistence

No database, ORM, migration, datasource, or repository layer was found in the scanned project.

## Verification commands

- `./gradlew clean compileJava`
- `./gradlew build -x test`
- `./scripts/run-tests.sh`
- `./gradlew :flare-mock-server:bootRun`
- `./gradlew :flare-spring-boot-sample:bootRun`


## Spring Boot 4 migration memory

- Boot 4 starter split: use `spring-boot-starter-webmvc` for servlet MVC apps/starter paths instead of the old broad `spring-boot-starter-web`.
- Spring Boot 4.1.0 runtime uses Tomcat 11 and Spring Framework 7.0.x.
- Jackson 3 package migration: databind/core classes are under `tools.jackson.*`; annotation artifacts remain under `com.fasterxml.jackson.annotation`.
- Boot 4.1 BOM currently aligns Jackson 3 to `3.1.4`; keep project property aligned unless deliberately overriding and revalidating all Boot modules.
