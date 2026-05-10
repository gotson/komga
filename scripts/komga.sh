#!/usr/bin/env bash
set -euo pipefail

APP_NAME="komga"
JAR_NAME="komga-1.24.4.jar"
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$APP_DIR/$JAR_NAME"
PID_FILE="$APP_DIR/$APP_NAME.pid"
LOG_FILE="$APP_DIR/$APP_NAME.out.log"
STOP_TIMEOUT_SECONDS="${STOP_TIMEOUT_SECONDS:-30}"
START_TIMEOUT_SECONDS="${START_TIMEOUT_SECONDS:-60}"

usage() {
  echo "Usage: $0 {start|status|stop|restart}"
}

is_running() {
  [[ -f "$PID_FILE" ]] || return 1

  local pid
  pid="$(cat "$PID_FILE")"
  [[ -n "$pid" ]] || return 1

  kill -0 "$pid" 2>/dev/null
}

read_pid() {
  cat "$PID_FILE"
}

make_secret_config() {
  local password="$1"
  local temp_dir temp_config secret_config escaped_password

  if [[ -d /dev/shm && -w /dev/shm ]]; then
    temp_dir="/dev/shm"
  else
    temp_dir="${TMPDIR:-/tmp}"
  fi

  umask 077
  temp_config="$(mktemp "${temp_dir%/}/$APP_NAME-secret.XXXXXX")"
  secret_config="$temp_config.yml"
  mv "$temp_config" "$secret_config"
  escaped_password="${password//\\/\\\\}"
  escaped_password="${escaped_password//\"/\\\"}"

  {
    echo "komga:"
    echo "  media-file-decryption:"
    printf "    password: \"%s\"\n" "$escaped_password"
  } > "$secret_config"

  echo "$secret_config"
}

wait_for_startup() {
  local pid="$1"
  local start_line="$2"

  for ((i = 0; i < START_TIMEOUT_SECONDS; i++)); do
    if ! kill -0 "$pid" 2>/dev/null; then
      return 1
    fi

    if tail -n +"$start_line" "$LOG_FILE" 2>/dev/null | grep -q "Started .*Application"; then
      return 0
    fi

    sleep 1
  done

  return 0
}

delete_secret_config() {
  local secret_config="$1"

  if command -v shred >/dev/null 2>&1; then
    shred -u "$secret_config" 2>/dev/null || rm -f "$secret_config"
  else
    rm -f "$secret_config"
  fi
}

start() {
  if [[ ! -f "$JAR_PATH" ]]; then
    echo "Jar not found: $JAR_PATH" >&2
    exit 1
  fi

  if is_running; then
    echo "$APP_NAME is already running. pid=$(read_pid)"
    exit 0
  fi

  rm -f "$PID_FILE"

  local java_bin java_opts komga_opts pid password secret_config log_start_line
  java_bin="${JAVA_BIN:-java}"
  java_opts="${JAVA_OPTS:-}"
  komga_opts="${KOMGA_OPTS:-}"

  read -r -s -p "Media file decryption password (leave empty to skip): " password
  echo

  secret_config=""
  if [[ -n "$password" ]]; then
    secret_config="$(make_secret_config "$password")"
    komga_opts="--spring.config.additional-location=file:$secret_config $komga_opts"
  fi
  unset password

  cd "$APP_DIR"

  log_start_line=1
  if [[ -f "$LOG_FILE" ]]; then
    log_start_line="$(($(wc -l < "$LOG_FILE") + 1))"
  fi

  nohup "$java_bin" $java_opts -jar "$JAR_PATH" $komga_opts >> "$LOG_FILE" 2>&1 &
  pid="$!"
  echo "$pid" > "$PID_FILE"

  if [[ -n "$secret_config" ]]; then
    wait_for_startup "$pid" "$log_start_line" || true
    delete_secret_config "$secret_config"
  fi

  if ! kill -0 "$pid" 2>/dev/null; then
    rm -f "$PID_FILE"
    echo "$APP_NAME failed to start. check log: $LOG_FILE" >&2
    exit 1
  fi

  echo "$APP_NAME started. pid=$pid"
  echo "log: $LOG_FILE"
}

status() {
  if is_running; then
    echo "$APP_NAME is running. pid=$(read_pid)"
    exit 0
  fi

  if [[ -f "$PID_FILE" ]]; then
    rm -f "$PID_FILE"
    echo "$APP_NAME is not running. stale pid file removed."
    exit 1
  fi

  echo "$APP_NAME is not running."
  exit 1
}

stop() {
  if ! is_running; then
    if [[ -f "$PID_FILE" ]]; then
      rm -f "$PID_FILE"
      echo "$APP_NAME is not running. stale pid file removed."
    else
      echo "$APP_NAME is not running."
    fi
    return 0
  fi

  local pid
  pid="$(read_pid)"

  kill "$pid"

  for ((i = 0; i < STOP_TIMEOUT_SECONDS; i++)); do
    if ! kill -0 "$pid" 2>/dev/null; then
      rm -f "$PID_FILE"
      echo "$APP_NAME stopped. pid=$pid"
      return 0
    fi

    sleep 1
  done

  echo "$APP_NAME did not stop within ${STOP_TIMEOUT_SECONDS}s. sending SIGKILL. pid=$pid"
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$PID_FILE"
  echo "$APP_NAME stopped. pid=$pid"
}

case "${1:-}" in
  start)
    start
    ;;
  status)
    status
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac
