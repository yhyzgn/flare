# flare 接管入口

## 强制读取

开始任何代码或文档修改前，按顺序阅读：`AGENTS.md`、`.context/README.md`、当前计划、当前任务、与目标模块相关的 `.context/system/` 文档，再以源码、测试和实际命令输出核对事实。

## 项目摘要

Flare 是一个 Java 21 的 Gradle 多模块 HTTP 客户端库，核心通过 OkHttp 动态代理把带注解的 Java 接口转换为请求执行器；Spring 相关模块负责 `@EnableFlare` / `@Flare` 扫描注册，当前发布版本为 `2.0.1`。

## 核心业务边界

- 允许：只做与当前任务明确关联的最小增量、上下文更新、文档修正、测试补充和可回滚的构建修复。
- 禁止：无证据的大范围重构、包名搬迁、公共注解语义漂移、无授权的依赖升级/降级、数据库相关引入。
- 当前系统是库/运行时集成项目，不是带持久化领域模型的业务应用。

## 历史约束

- 旧入口 `prompt.md`、`.memory/`、`.prompt/` 仅作为迁移来源，不再作为规范事实源。
- 任何遗留内容必须先进入 `.context/migration-state.json`，再按语义迁移或拒绝；不得机械改名。
- 迁移完成后，旧入口应归档到 `.context/archive/legacy-sources/`，并保持可追溯。

## 不可违反的规则

- 一个任务只做一个可验证目标。
- 修改导出符号前必须先搜全引用。
- 证据、推断和未知项必须分开记录。
- 未验证事实不得写入系统全景、规范或风险正文。
- 所有面向人员的上下文文档必须使用中文；路径、命令和技术标识符除外。

## 技术和数据红线

- 不新增数据库 View / Function / Procedure / Trigger / Event。
- 不改变公共接口、持久化数据形状、状态字符串、租户或权限语义，除非当前任务明确覆盖。
- 不把编译、打包或容器启动成功描述成业务测试通过。
- Spring / Jackson 迁移优先使用 `JsonMapper` 体系，只有确有兼容需求时才回退 `ObjectMapper` 兼容层。

## 生命周期与上下文传播

稳定事实写入 `.context/system/`；阶段顺序写入 `.context/plans/`；边界明确的单次变更写入 `.context/tasks/`。事实变化更新 system，阶段变化更新 plan，执行变化更新 task，并同步当前工作指针。

## 外部基础设施限制

不在测试、启动探针或扫描中连接真实共享数据库、缓存、消息队列、对象存储、Webhook 或第三方服务，除非当前任务明确声明并已获授权。

## 安全验证

优先使用仓库内已有脚本和最小安全命令。当前已知的基础检查命令包括：`python scripts/context_bootstrap.py validate --root /home/neo/Projects/neo/lib/flare`、`python scripts/context_bootstrap.py audit-migration --root /home/neo/Projects/neo/lib/flare`，以及需要时的 `./gradlew clean compileJava`、`./gradlew build -x test`、`./scripts/run-tests.sh`、`./gradlew :flare-mock-server:bootRun`、`./gradlew :flare-spring-boot-sample:bootRun`。

## 变更前记录

```text
目的：
影响路径：
兼容性：接口 / 数据 / 状态 / 租户 / 权限
外部副作用：
回滚点：
验证场景：
```

## 审查、提交和推送

未经用户明确要求，不提交、不推送。需要提交时，提交信息必须写明变更原因、影响范围、验证结果和已知风险；推送前重新执行约定的验证门禁。

## 遗留迁移覆盖

存在 `.context/migration-state.json` 时，必须先运行 `audit-migration`。覆盖率、来源哈希、分类目标、完整内容和唯一来源标记必须全部通过后，才能归档旧入口、提交或推送。

## 当前工作指针

- 计划：`.context/plans/002_configuration_priority.md`
- 任务：`.context/tasks/002_configuration_priority.md`

## 交付要求

每个任务必须包含范围、非目标、预期文件、验收标准、验证、风险与回滚、完成记录。报告必须区分已验证事实、推断和未知项。
