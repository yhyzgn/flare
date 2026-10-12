# 任务 004：全量补全并标准化 Javadoc

- 状态：已完成
- 规模：大
- 计划：`.context/plans/004_javadoc_completion.md`
- 生产行为变更：无，仅注释

## 目标

为全部 `src/main` Java 源（及 `flare` / `flare-spring-starter-abstract` 测试源中新增的类）补全并标准化 Javadoc：清除占位符描述、补齐类级/方法级文档与 `@param` / `@return` / `@throws` 标签，格式符合 Javadoc 标准。

## 范围

- 6 个模块全部 `src/main` 源文件（125 个），重点为 4 个可发布库模块（flare、flare-spring、flare-spring-starter-abstract、flare-spring-boot-starter）。
- 5 个缺 "Created on" 头的文件补齐标准头部（日期取 git 首次提交时间）。
- 现有 `@deprecated` 契约文字（since 2.0.2 / 3.0.0 移除）保持原样。

## 非目标

- 不改动任何代码、注解、导入、注解处理配置或格式化。
- 不改动 `.context/` 既有文档（003 及之前）。
- 不提交、不推送。

## 验证

- 修改前后 `.class` 文件 SHA-256 清单完全一致（证明代码零改动）。
- `./gradlew javadoc`（4 个库模块）无错误。
- `./gradlew build -x test` 通过。
- `./scripts/run-tests.sh` 通过。
- `git diff` 仅注释块增删。

## 风险与回滚

- 风险：误改代码导致行为变化。缓解：哈希对比 + 逐文件 diff 复核。
- 风险：`@link`/`@code` 等标签引用不存在的符号导致 javadoc 任务失败。缓解：javadoc 任务验证。
- 回滚点：`git checkout -- <受影响文件>`，恢复到 da9c078 基线。

## 完成记录

- 133 个 Java 文件（125 个 `src/main` 全覆盖 + 8 个测试源）Javadoc 补全并标准化：清除空类描述（约 30 处）、占位参数描述（"值""处理结果"等约 150 处）、方法名复读式描述（约 60 处）、破损 Javadoc 块格式（`/** ↵ * * text` 双星号行，约 10 处）；补齐 `@param` / `@return` / `@throws` / `@param <T>` 标签。
- 5 个缺失 "Created on" 头的文件（InstanceFactory、ConstructorInstanceFactory、LegacyDelegateInstanceFactory、SpringInstanceFactory、Opt）按 git 首次提交时间补齐标准头部，`@since` 合并去重。
- 所有既有 `@deprecated` 契约文字（since 2.0.2 / 3.0.0 移除）、`@author 颜洪毅`、`@version`、`@since` 历史值逐字保留。
- SSL 三件套（VoidSSL*）补充 trust-all 安全警告；Spring 装配语义（`@ConditionalOnMissingBean` 让位、`getBeanProvider` 按接口查找、配置优先级）写入类级文档。
- 零代码改动验证：133 个文件剥离 Javadoc 块后与 HEAD 逐字符一致；flare 模块全部 143 个类（85 顶级 + 58 内部类）`javap -c -p` 指令级一致（`.class` 哈希差异仅来自行号表 LineNumberTable 随注释行数平移，非语义变化）。
- 验证命令均通过：4 个库模块 `javadoc` 任务、`./gradlew build -x test`、`./scripts/run-tests.sh`（34 个测试 0 失败 0 错误 0 跳过）、`git diff --check`。
- 未提交、未推送。
- 已知残留（非本次范围）：GuavaCallAdapter 内部 record 组件的类级 Javadoc 未补（私有内部类型）；诊断面板中 `log cannot be resolved`、PartForm setter "未定义" 等为语言服务器对 Lombok 的误报，Gradle 编译无任何错误。
