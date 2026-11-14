# NanoArray

A client GUI framework for Fabric. One shared Java code base with a small
adapter per Minecraft version.

Supported versions: 1.21.11, 26.1.2, 26.2, 26.3.

## requirements

- JDK 21 for 1.21.11, JDK 25 for 26.x.
- Use the included Gradle wrapper.

## build

```bash
./gradlew buildAll
./gradlew -PmcVersion=26.3 :versions:26.3:build
```

Jars are written to `versions/<minecraft>/build/libs/`.

## install

1. Install Fabric Loader for the matching Minecraft version.
2. Put Fabric API in `mods/`.
3. Put `nanoarray-<minecraft>-<version>.jar` in `mods/`.
4. Press Right Shift in game to open the GUI. Escape closes it.

## tests

```bash
./gradlew :common:test
cd native/font-renderer && cargo test --release
```

## config

Settings are saved to `.minecraft/config/nanoarray.json`. The selected theme is
stored under the `theme` key.

The native font renderer is prebuilt for macOS, Windows and Linux on x86_64 and
ARM64. If it cannot load, text falls back to Java2D.
