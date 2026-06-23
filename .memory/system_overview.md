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
- Spring Boot plugin: 3.5.11.
- Spring framework version property: 6.2.11.
- OkHttp BOM: 5.3.2.
- Jackson: 2.21.1.
- Gson: 2.13.2.
- JUnit: 6.0.3.
- Lombok: 1.18.42.

## Database / persistence

No database, ORM, migration, datasource, or repository layer was found in the scanned project.

## Verification commands

- `./gradlew clean compileJava`
- `./gradlew build -x test`
- `./scripts/run-tests.sh`
- `./gradlew :flare-mock-server:bootRun`
- `./gradlew :flare-spring-boot-sample:bootRun`
