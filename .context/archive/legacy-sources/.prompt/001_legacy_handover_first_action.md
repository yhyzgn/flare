# 001 Legacy Handover First Action

## Objective

Establish and verify a safe legacy handover baseline for Flare before any source-code refactor or feature work.

## Scope

Allowed:

- Read project files.
- Update handover documentation: `prompt.md`, `.memory/`, `.prompt/`.
- Run compile/test/smoke commands.
- Commit/push only documentation/handover assets after verification passes.

Not allowed by default:

- Modify existing Java source, CI, release scripts, tests, or README content in this phase.
- Rename packages/modules.
- Upgrade/downgrade dependencies.
- Reformat old code.

Exception discovered during execution:

- Minimal build-tooling fixes are allowed when validation cannot run because the existing build/test command is broken. State risk and rollback first, keep the write set narrow, and rerun validation.

## Risk and rollback point

Initial risk was low because this phase was expected to add documentation and memory assets only. During validation, two existing tooling defects were found and fixed narrowly: library modules had Spring Boot `bootJar` enabled without main classes, and `scripts/run-tests.sh` lacked executable mode / port preflight. Rollback point: delete handover docs, remove the two `bootJar { enabled = false }` blocks, restore `scripts/run-tests.sh` mode/content, or revert the handover commit.

## Required actions

1. Read `prompt.md` and all files in `.memory/`.
2. Confirm no source-code edits are needed for the handover baseline.
3. Run:
   - `./gradlew clean compileJava`
   - `./gradlew build -x test`
   - `./scripts/run-tests.sh`
4. If runtime validation is required, start mock server on port `8080` and sample app on port `8082`, then smoke at least one sample endpoint.
5. Record validation results in the final handover response.
6. If all required validations pass, commit only the handover/tooling assets with a message containing:
   - Reason
   - Impact scope
   - Verification result
   - Known risk
7. Push after local verification passes.

## Acceptance criteria

- `prompt.md` exists and describes current state, topology, boundaries, constraints, risks, incremental plan, and acceptance standard.
- `.memory/system_overview.md`, `.memory/code_conventions.md`, and `.memory/risk_register.md` exist.
- `.prompt/001_legacy_handover_first_action.md` exists.
- Compile passes.
- Full helper test flow passes or any failure is documented with exact blocker evidence.
- No Java business/source files are modified in this phase.
- Mock-backed tests pass even when port `8080` is occupied by an unrelated external process, because the helper script can select a free fallback port and forward it into tests.

## Execution result

Completed on 2026-06-23. Handover assets were created and the smallest required build/test tooling fixes were applied. Validation passed with `git diff --check`, `./gradlew clean compileJava`, `./gradlew build -x test`, and `./scripts/run-tests.sh` (24 tests passed using fallback mock port 18080 because host port 8080 was occupied).
