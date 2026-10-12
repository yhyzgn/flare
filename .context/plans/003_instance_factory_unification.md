# 计划 003：统一实例创建工厂

- 状态：已完成
- 当前任务：`.context/tasks/003_instance_factory_unification.md`

## 目标

将只负责对象创建方式的四组 Delegate 收敛为一个通用 `InstanceFactory`，保留 `MethodAnnotationDelegate` 作为独立的注解解析扩展点。

## 阶段

1. 新增构造方法和 Spring 两种统一实例工厂。
2. 将核心请求解析、异常分发、DispatcherProvider 和 Spring FactoryBean 接入统一工厂。
3. 为旧 Delegate API 增加废弃注释和 `@Deprecated(since = "2.0.2")`，文档标明计划在 `3.0.0` 移除；过渡期不设置 `forRemoval = true`。
4. 增加兼容适配器与定向测试。
5. 执行完整构建和集成测试。

## 退出标准

- 新下游可通过 `Flare.Builder#instanceFactory` 统一决定实例创建方式。
- 旧 Delegate 配置在过渡期仍可工作。
- 方法注解解析语义不变。
- 定向测试、完整构建和集成测试通过。
