#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export NODE_OPTIONS="${NODE_OPTIONS:-} --max-old-space-size=${NODE_MAX_OLD_SPACE_SIZE:-8192}"

./gradlew :komga:prepareThymeLeaf build "$@"

LIBS_DIR="$ROOT_DIR/komga/build/libs"
LAUNCHER_PATH="$ROOT_DIR/scripts/komga.sh"
DIST_DIR="${DIST_DIR:-$ROOT_DIR/build/server-package}"

if [[ ! -f "$LAUNCHER_PATH" ]]; then
  echo "Launcher not found: $LAUNCHER_PATH" >&2
  exit 1
fi

jars=()
while IFS= read -r jar; do
  jars+=("$jar")
done < <(find "$LIBS_DIR" -maxdepth 1 -type f -name "komga-*.jar" ! -name "*-plain.jar" | sort)

if [[ "${#jars[@]}" -ne 1 ]]; then
  echo "Expected exactly one executable jar in $LIBS_DIR, found ${#jars[@]}." >&2
  printf '  %s\n' "${jars[@]}" >&2
  exit 1
fi

jar_path="${jars[0]}"
jar_name="$(basename "$jar_path")"
version="${jar_name#komga-}"
version="${version%.jar}"
zip_name="${ZIP_NAME:-komga-server-$version.zip}"

if [[ "$zip_name" = /* ]]; then
  zip_path="$zip_name"
else
  zip_path="$DIST_DIR/$zip_name"
fi

mkdir -p "$(dirname "$zip_path")"

package_dir="$(mktemp -d "${TMPDIR:-/tmp}/komga-server-package.XXXXXX")"
trap 'rm -rf "$package_dir"' EXIT

cp "$jar_path" "$package_dir/$jar_name"

while IFS= read -r line || [[ -n "$line" ]]; do
  case "$line" in
    JAR_NAME=*)
      printf 'JAR_NAME="%s"\n' "$jar_name"
      ;;
    *)
      printf '%s\n' "$line"
      ;;
  esac
done < "$LAUNCHER_PATH" > "$package_dir/komga.sh"

chmod +x "$package_dir/komga.sh"
rm -f "$zip_path"

(
  cd "$package_dir"
  zip -q -9 "$zip_path" "$jar_name" komga.sh
)

echo "Created: $zip_path"
