# 任务 003：统一实例创建委托

- 状态：已完成
- 规模：中
- 计划：`.context/plans/003_instance_factory_unification.md`
- 生产行为变更：有，统一实例创建扩展点；保留旧 API 兼容路径

## 目标

将动态请求头、拦截器、异常解析器和 DispatcherProvider 的实例创建策略统一为 InstanceFactory，同时保留旧 Delegate API 并标记为待移除。

## 范围

- 新增核心 InstanceFactory 与构造方法实现。
- 新增 Spring SpringInstanceFactory。
- 核心请求解析、异常分发、DispatcherProvider 创建和 Spring FactoryBean 改用统一工厂。
- 旧四组 Delegate 接口、构造实现、Spring 实现及 Builder 入口保留兼容并标记 `@Deprecated(since = "2.0.2")`；Javadoc 明确计划在 `3.0.0` 移除。
- MethodAnnotationDelegate 保持独立，不纳入实例创建工厂。

## 非目标

- 不改变方法注解解析的反射 / Spring 合并语义。
- 不删除当前版本中的旧公共 API。
- 不升级依赖，不调整数据库或发布配置。

## 兼容与移除约定

- 旧 API 自 2.0.2 起废弃。
- 旧 API 计划在 3.0.0 移除；在此之前通过兼容适配器继续生效。

## 验证

- ./gradlew :flare:compileJava :flare-spring:compileJava :flare-spring-starter-abstract:compileJava :flare-spring-boot-starter:compileJava
- ./gradlew :flare:test --tests com.yhy.http.flare.InstanceFactoryTest --tests com.yhy.http.flare.http.request.RequestFactoryHeaderPriorityTest :flare-spring-starter-abstract:test --tests com.yhy.http.flare.spring.starter.register.AbstractFlareAutoRegisterPriorityTest
- ./gradlew build -x test
- ./scripts/run-tests.sh
- `git diff --check`

## 完成记录

- 新增 InstanceFactory、构造方法工厂、Spring 工厂和旧 Delegate 兼容适配器。
- 显式配置 InstanceFactory 时，Flare 的旧 Delegate getter 和 Builder 字段反向桥接到同一个统一工厂；Spring FactoryBean 不再用默认旧 Delegate 覆盖统一工厂。
- Spring FactoryBean 按 `InstanceFactory` 接口查找容器 Bean，支持下游自定义工厂覆盖默认 Spring 实现。
- 补齐默认 SSL 信任所有实现：修复 HostnameVerifier 返回值、SocketFactory 空返回值，并让核心 `Flare.Builder` 默认安装 trust-all SSL 组件。
- `FlareFactoryBean#getInstance` 改为 `getBeanProvider(...).getIfAvailable()`，并对转换器、实例工厂、注解 Delegate、DispatcherProvider 按接口查找，避免绕过下游自定义 Bean。
- @EnableFlare 默认只注册统一 Spring 工厂；方法注解解析器仍单独注册。
- 验证命令均通过，未提交、未推送。
- `context_bootstrap.py` 在当前仓库不存在，因此上下文脚本校验未执行；其余构建、定向测试、集成测试和差异检查均通过。
- 后续复核修复了默认 trust-all SSL 三件套、部分 SSL 配置快速失败，以及 Spring 基础设施按接口查找问题；追加定向测试和集成测试均通过。
