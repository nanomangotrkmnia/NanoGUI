#!/usr/bin/env bash
#
# Builds the native font renderer for every supported platform.
#
# The binaries are written to native/font-renderer/dist/<platform>/ and are
# picked up by the Gradle build, which packages them into the mod jar.
#
# Requirements:
#   * Rust (stable, with the `rustup` targets installed)
#   * Zig, used as the cross linker for the Linux and Windows targets
#
# Usage:
#   ./native/build-native.sh                 # build every target
#   ./native/build-native.sh linux-x86_64    # build a single target
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CRATE_DIR="$SCRIPT_DIR/font-renderer"
DIST_DIR="$CRATE_DIR/dist"

# platform-id | rust-target | output file name
TARGETS=(
  "macos-x86_64|x86_64-apple-darwin|libnanoarray_font.dylib"
  "macos-aarch64|aarch64-apple-darwin|libnanoarray_font.dylib"
  "windows-x86_64|x86_64-pc-windows-gnu|nanoarray_font.dll"
  "windows-aarch64|aarch64-pc-windows-msvc|nanoarray_font.dll"
  "linux-x86_64|x86_64-unknown-linux-gnu|libnanoarray_font.so"
  "linux-aarch64|aarch64-unknown-linux-gnu|libnanoarray_font.so"
)

if ! command -v cargo >/dev/null 2>&1; then
  if [ -f "$HOME/.cargo/env" ]; then
    # shellcheck disable=SC1091
    source "$HOME/.cargo/env"
  else
    echo "error: cargo was not found. Install Rust from https://rustup.rs" >&2
    exit 1
  fi
fi

has_zig() {
  command -v zig >/dev/null 2>&1
}

build_target() {
  local platform="$1"
  local target="$2"
  local output="$3"

  echo "==> building $platform ($target)"

  rustup target list --installed | grep -qx "$target" || {
    echo "    installing Rust target $target"
    rustup target add "$target"
  }

  if [ "$target" = "aarch64-apple-darwin" ]; then
    # Native build, the host toolchain links it directly.
    (cd "$CRATE_DIR" && cargo build --release --features jni --target "$target")
  elif [ "$target" = "x86_64-apple-darwin" ]; then
    # The Apple SDK is universal, clang cross compiles without extra tooling.
    (cd "$CRATE_DIR" && cargo build --release --features jni --target "$target")
  elif [ "$target" = "aarch64-pc-windows-msvc" ]; then
    # Windows on ARM64 has no GNU toolchain, cargo-xwin provides the MSVC
    # import libraries and uses the linker that ships with Rust.
    if ! command -v cargo-xwin >/dev/null 2>&1; then
      echo "    error: cargo-xwin is required to build $target" >&2
      echo "    install it with: cargo install cargo-xwin" >&2
      return 1
    fi
    (cd "$CRATE_DIR" && cargo xwin build --release --features jni --target "$target")
  else
    if ! has_zig; then
      echo "    error: zig is required to cross compile $target" >&2
      echo "    install it with: brew install zig" >&2
      return 1
    fi
    (cd "$CRATE_DIR" && cargo zigbuild --release --features jni --target "$target")
  fi

  local built="$CRATE_DIR/target/$target/release/$output"
  if [ ! -f "$built" ]; then
    echo "    error: expected $built to exist" >&2
    return 1
  fi

  mkdir -p "$DIST_DIR/$platform"
  cp "$built" "$DIST_DIR/$platform/$output"
  echo "    -> $DIST_DIR/$platform/$output"
}

mkdir -p "$DIST_DIR"

if [ "$#" -gt 0 ]; then
  selected=()
  for wanted in "$@"; do
    found=false
    for entry in "${TARGETS[@]}"; do
      if [ "${entry%%|*}" = "$wanted" ]; then
        selected+=("$entry")
        found=true
        break
      fi
    done
    if [ "$found" = false ]; then
      echo "error: unknown platform '$wanted'" >&2
      echo "available: ${TARGETS[*]%%|*}" >&2
      exit 1
    fi
  done
else
  selected=("${TARGETS[@]}")
fi

for entry in "${selected[@]}"; do
  IFS='|' read -r platform target output <<<"$entry"
  build_target "$platform" "$target" "$output"
done

echo
echo "All requested targets were built:"
find "$DIST_DIR" -type f | sort
