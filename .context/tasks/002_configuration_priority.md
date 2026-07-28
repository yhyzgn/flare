# 任务 002：修正 Flare 配置优先级

- 状态：已完成
- 计划：`.context/plans/002_configuration_priority.md`
- 规模：中
- 依赖：`.context/tasks/001_context_bootstrap.md`
- 生产行为变更：有，修正配置覆盖顺序

## 任务目标

把 `@EnableFlare` 的全局配置改为兜底配置，保证 `@Flare` 和实际代理方法/参数上的配置优先级更高。

## 范围

- `flare/src/main/java/com/yhy/http/flare/http/request/RequestFactory.java`
- `flare-spring-starter-abstract/src/main/java/com/yhy/http/flare/spring/starter/register/AbstractFlareAutoRegister.java`
- `flare-spring-boot-starter/src/main/java/com/yhy/http/flare/spring/starter/annotation/Flare.java`
- 必要的定向测试和测试运行所需的最小 Gradle 测试依赖修正
- `.context/system/risks.md` 中沉淀优先级规则风险

## 非目标

- 不发布版本。
- 不提交、不推送。
- 不重构整个请求解析器、Spring 扫描器或 OkHttp 调用层。
- 不引入新的生产依赖。

## 预期文件

- 修改上述生产代码和测试代码。
- 必要时仅为测试 classpath 增加现有 Spring Boot BOM 约束。
- 更新当前计划/任务指针和风险文档。

## 验收标准

- Builder 全局 Header 不覆盖方法级/参数级 Header。
- Builder 全局动态 Header 不覆盖方法级动态 Header。
- `@EnableFlare` 的 Header、timeout、logEnabled 未被 `@Flare` 配置时作为兜底。
- `@Flare` 显式配置 Header、timeout、logEnabled 时覆盖 `@EnableFlare`。
- 变更具备可回滚点和定向测试证据。

## 验证

- `./gradlew :flare:test --tests com.yhy.http.flare.http.request.RequestFactoryHeaderPriorityTest`
- `./gradlew :flare-spring-starter-abstract:test --tests com.yhy.http.flare.spring.starter.register.AbstractFlareAutoRegisterPriorityTest`
- 通过后补充必要的编译 / 构建检查结果。

## 风险与回滚

- 风险：`@Flare#logEnabled` / `@Flare#timeout` 使用空字符串表示继承，属于公共注解默认值语义变化。
- 风险：Header 重新应用会改变同名 Header 的最终值，但这是本任务目标。
- 回滚：恢复本任务修改的生产文件，删除新增测试和测试 classpath 修正，恢复 AGENTS 当前指针到 001。

## 完成记录

2026-07-28 已完成配置优先级修正：

- `RequestFactory` 先应用 Builder / Spring 外层 Header 兜底，再重新应用方法级和参数级 Header，避免全局 Header 反向覆盖动态代理方法配置。
- `AbstractFlareAutoRegister` 合并 Header 时先全局、后局部；动态 Header 列表先全局、后局部；`logEnabled` 和 `timeout` 支持局部显式覆盖、未配置时继承全局。
- `@Flare#logEnabled` 和 `@Flare#timeout` 默认值调整为空字符串，用作“未配置/继承全局”的哨兵值。
- 新增优先级测试覆盖核心请求构建链路和 Spring 注册器链路。
- 验证命令：
  - `./gradlew :flare:test --tests com.yhy.http.flare.http.request.RequestFactoryHeaderPriorityTest :flare-spring-starter-abstract:test --tests com.yhy.http.flare.spring.starter.register.AbstractFlareAutoRegisterPriorityTest` → `BUILD SUCCESSFUL`。
  - `./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`。
  - `./gradlew build -x test` → `BUILD SUCCESSFUL`。
  - `./scripts/run-tests.sh` → `Tests finished with exit code 0`。
- 未提交、未推送。
