# 计划 004：全量补全并标准化 Javadoc

- 状态：已完成
- 当前任务：`.context/tasks/004_javadoc_completion.md`

## 目标

为全部 6 个模块的 `src/main` Java 源文件（含 `flare` 与 `flare-spring-starter-abstract` 的测试源）补全、修正并标准化 Javadoc 注释，不改动任何代码、注解、导入或格式。

## 阶段

1. 建立基线：全量编译并记录 `.class` 文件哈希，作为"代码零改动"的验证基准。
2. 按模块补全 Javadoc：`flare`（核心，85 文件）、`flare-spring`（9）、`flare-spring-starter-abstract`（2+1 测试）、`flare-spring-boot-starter`（4）、`flare-mock-server`（7）、`flare-spring-boot-sample`（18）。
3. 统一风格：沿用现有中文 Javadoc 基线（描述 + `Created on` + `@author 颜洪毅` + `@version` + `@since`），清除占位符描述（如"值""处理结果"），补齐 `@param` / `@return` / `@throws`。
4. 验证：重新编译并比对 `.class` 哈希与基线完全一致；`./gradlew javadoc` 通过；`./gradlew build -x test` 通过；`./scripts/run-tests.sh` 通过；`git diff` 仅含注释行。

## 退出标准

- 所有 `src/main` 公共类型与公共/保护成员具备完整、准确、格式标准的 Javadoc。
- 编译产物字节码与基线完全一致（证明零代码改动）。
- Javadoc 任务、构建与集成测试全部通过。
