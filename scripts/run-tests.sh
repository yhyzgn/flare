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
MOCK_PORT="${FLARE_MOCK_PORT:-8080}"

port_in_use() {
  local port="$1"
  if command -v ss >/dev/null 2>&1; then
    ss -ltn "sport = :${port}" | grep -q ":${port}"
  elif command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"${port}" -sTCP:LISTEN -P -n >/dev/null 2>&1
  else
    return 1
  fi
}

if port_in_use "$MOCK_PORT" && [ -z "${FLARE_MOCK_PORT:-}" ]; then
  for candidate in $(seq 18080 18120); do
    if ! port_in_use "$candidate"; then
      MOCK_PORT="$candidate"
      break
    fi
  done
elif port_in_use "$MOCK_PORT"; then
  echo "[run-tests] Configured port ${MOCK_PORT} is already in use." >&2
  ss -ltnp "sport = :${MOCK_PORT}" 2>/dev/null || lsof -iTCP:"${MOCK_PORT}" -sTCP:LISTEN -P -n || true
  exit 1
fi

HEALTH_URL="http://localhost:${MOCK_PORT}/get/index"
TIMEOUT=60

echo "[run-tests] Starting mock server with Gradle task $MOCK_TASK on port $MOCK_PORT"
$GRADLE_CMD $MOCK_TASK --args="--server.port=${MOCK_PORT}" &
MOCK_PID=$!

echo "[run-tests] Mock server PID: $MOCK_PID"

cleanup() {
  if kill -0 "$MOCK_PID" 2>/dev/null; then
    echo "[run-tests] Stopping mock server (PID $MOCK_PID)"
    kill "$MOCK_PID" || true
    wait "$MOCK_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

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
echo "[run-tests] Removing known download-test artifacts"
rm -f "${HOME}/Downloads/void.txt" "${HOME}/Downloads/ttttttttt.txt"

echo "[run-tests] Running Gradle tests"
$GRADLE_CMD :flare:test -Dflare.mock.port="$MOCK_PORT" "$@"
TEST_EXIT=$?

echo "[run-tests] Tests finished with exit code $TEST_EXIT"

exit $TEST_EXIT

