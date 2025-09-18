#!/usr/bin/env bash
set -euo pipefail

# Simple CI helper script:
# 1. Start mock server in background
# 2. Wait for health endpoint to respond
# 3. Run module tests
# 4. Stop mock server

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

GRADLE_CMD="./gradlew"
MOCK_TASK=":flare-mock-server:bootRun"
HEALTH_URL="http://localhost:8080/get/index"
TIMEOUT=60

echo "[run-tests] Starting mock server with Gradle task $MOCK_TASK"
$GRADLE_CMD $MOCK_TASK &
MOCK_PID=$!

echo "[run-tests] Mock server PID: $MOCK_PID"

echo "[run-tests] Waiting up to ${TIMEOUT}s for mock server to become healthy at $HEALTH_URL"
for i in $(seq 1 $TIMEOUT); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || echo "000")
  if [ "$code" = "200" ]; then
    echo "[run-tests] Mock server healthy"
    break
  fi
  if [ $i -eq $TIMEOUT ]; then
    echo "[run-tests] Mock server did not become healthy after ${TIMEOUT}s (last HTTP code: $code)"
    echo "[run-tests] Killing mock server (PID $MOCK_PID)"
    kill $MOCK_PID || true
    exit 1
  fi
  sleep 1
done

# Run tests (pass through any extra args)
echo "[run-tests] Running Gradle tests"
$GRADLE_CMD :flare:test "$@"
TEST_EXIT=$?

echo "[run-tests] Tests finished with exit code $TEST_EXIT"

echo "[run-tests] Stopping mock server (PID $MOCK_PID)"
kill $MOCK_PID || true
wait $MOCK_PID 2>/dev/null || true

exit $TEST_EXIT

