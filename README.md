# NanoArray

A client GUI framework for Fabric. One shared Java code base with a small
adapter per Minecraft version.

Supported versions: 1.21.11, 26.1.2, 26.2, 26.3.

## how it works

- Components record a frame into a flat `DrawList`. A backend replays it. In
  game that backend uses Minecraft's own fill, blit and scissor calls.
- Rounded corners are two fills plus four anti-aliased corner sprites generated
  from a disc. Minecraft has no path renderer, so this avoids a staircase.
- Fonts are addressed by id. `FontLoader` uses the bundled Rust renderer through
  JNI when its library loads, otherwise Java2D. Both share a measurement cache.
- Input goes through one `InputState`. The root covers the viewport, so clicks
  cannot reach the world. Closing the GUI resets held keys.
- Themes are immutable and saved to `.minecraft/config/nanoarray.json`. A
  corrupt file is moved to `.broken` and the client still starts.

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

## notes

- The native font renderer is prebuilt for macOS, Windows and Linux on x86_64
  and ARM64. If it cannot load, text falls back to Java2D.
- There are NanoVG and Dear ImGui translation backends in
  `common/src/main/java/dev/nanomango/gui/render/`, but they are not wired into
  the game. Minecraft's public GUI API cannot submit arbitrary triangles.
