#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export NODE_OPTIONS="${NODE_OPTIONS:-} --max-old-space-size=${NODE_MAX_OLD_SPACE_SIZE:-8192}"

./gradlew :komga:prepareThymeLeaf build "$@"
