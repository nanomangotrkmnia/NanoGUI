# NanoArray

A modern, high performance GUI framework for Minecraft, written in Java with an
optional Rust font renderer. One shared framework, several Minecraft versions,
built from a single Gradle project.

Press **Right Shift** in game to open the GUI.

## Features

- **One code base, four Minecraft versions.** The framework itself is pure Java
  and contains no Minecraft type. Each version only contributes a thin adapter
  for the render and screen APIs that changed between releases.
- **Modern visual language.** Rounded corners, anti-aliased geometry, shadows,
  borders, accent colours, gradients and easing animations.
- **Resolution independent.** Everything is laid out in GUI units and scaled at
  draw time. Fonts are rasterised at the exact pixel size, so text stays crisp
  on HiDPI displays and at every Minecraft GUI scale.
- **A reusable component library.** More than forty components, from labels and
  buttons to data tables, graphs, colour pickers and modal dialogs.
- **Centralised input.** The keybind, keyboard, mouse, clipboard and drag state
  live in one place. Closing the GUI releases every held key and button, so
  nothing gets stuck and no click leaks into the world.
- **Themes and persistence.** Dark, light and custom themes, stored as JSON.
  Writes are debounced and a corrupt file is recovered instead of crashing.
- **A real native font renderer.** A Rust library (`ttf-parser`, `rustybuzz`,
  `ab_glyph`) with a stable JNI API, prebuilt for six platforms. The pure Java
  Java2D rasteriser is kept as a fallback, so the framework works even when the
  native library cannot be loaded.
- **Three interchangeable render backends.** Minecraft's own GUI primitives,
  NanoVG, and a Dear ImGui draw list translation. The component code never
  touches a graphics API.

## Supported Minecraft versions

| Minecraft | Java | Fabric API         | Loom plugin               |
| --------- | ---- | ------------------ | ------------------------- |
| 1.21.11   | 21   | 0.141.6+1.21.11    | `fabric-loom-remap`       |
| 26.1.2    | 25   | 0.155.3+26.1.2     | `fabric-loom`             |
| 26.2      | 25   | 0.161.0+26.2       | `fabric-loom`             |
| 26.3      | 25   | 0.166.2+26.3       | `fabric-loom`             |

1.21.11 is remapped with the official Mojang mappings. The 26.x line is built
without remapping, because Yarn mappings are not published for it. Every
version is built from the same sources; there is no silent substitution and an
unknown version is rejected with an error.

## Requirements

- A JDK for the Minecraft versions you want to build:
  - Java 21 or newer for 1.21.11
  - Java 25 or newer for 26.x
- The Gradle wrapper is included, no separate Gradle installation is needed.
- Building the native font renderer additionally needs Rust, Zig and, for
  Windows on ARM64, `cargo-xwin`. End users never need a compiler: the release
  jars already contain the compiled libraries.

## Build

```bash
# build every version
./gradlew buildAll

# build a single version
./gradlew -PmcVersion=26.3 :versions:26.3:build

# run the framework tests
./gradlew :common:test

# run the tests of every version project
./gradlew testAll
```

The mod jars are written to `versions/<minecraft>/build/libs/`.

```bash
# rebuild the native font renderer for all six platforms
./gradlew buildNative

# check that all six binaries are present
./gradlew checkNative

# measure the native renderer against the Java2D fallback
./gradlew :common:benchmark
```

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for the Minecraft version.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) into `mods/`.
3. Put the matching `nanoarray-<version>+mc<minecraft>.jar` into `mods/`.
4. Start the game and press **Right Shift**.

## Project layout

```
common/                     the Minecraft independent framework
  src/main/java/.../render/ DrawList, RenderBackend, corner sprites, NanoVG, ImGui
  src/main/java/.../font/   native, Java2D and Minecraft fonts, registry, cache
  src/main/java/.../components/  the component library
  src/main/java/.../core/   Gui, GuiContext, input plumbing
  src/main/java/.../theme/  themes and theme manager
  src/main/java/.../config/ JSON persistence
  src/main/resources/fonts/ bundled Inter family (SIL OFL)
  src/test/java/            the test suite

versions/shared/            Minecraft integration shared by all versions
versions/shared-26x/        integration specific to the 26.x line
versions/<minecraft>/       per version adapter, entry point and mod metadata
native/font-renderer/       the Rust font engine
  dist/<platform>/          prebuilt binaries, packaged into the mod jar
```

## Architecture

The framework records a frame into a flat command stream and lets a backend
replay it.

```
component tree ──▶ DrawList ──▶ RenderBackend ──▶ Minecraft / NanoVG / ImGui
        ▲                │
        └── input, theme, fonts
```

- **`DrawList`** is an allocation-free stream of primitive commands: rectangles,
  rounded rectangles, gradients, outlines, shadows, text, textured quads, lines,
  triangles, arcs and clipping. Float and int arguments live in parallel arrays;
  strings are interned per frame.
- **`RenderBackend`** owns the graphics API. `MinecraftRenderBackend` is used in
  game; `NanoVGRenderBackend` and `ImGuiRenderBackend` translate the same stream
  for vector or immediate-mode renderers. Backends are implemented and unit
  tested against a recording adapter, without needing a GPU.
- **Rounded corners** are drawn as two crisp axis-aligned fills plus four
  analytic anti-aliased corner sprites. The sprites are generated from the exact
  geometry of a disc with four-by-four supersampling, then uploaded once per
  radius. No staircase, no blurry scaling.
- **Fonts** are addressed by a small integer id. `FontLoader` creates a
  `NativeFont` when the platform library is available and an `AwtFont`
  otherwise. Both share the same measurement cache, so the hot path performs no
  allocation. `MinecraftFont` is an adapter around Minecraft's own font.
- **Input** is centralised in `InputState`. The GUI root is a `Container` that
  covers the viewport, so a click on a widget can never fall through to the
  world. `Gui.close()` resets the input state to prevent stuck keys.
- **Animation** is driven by delta time. `Animation` and `Easing` provide the
  interpolation used by hover, press, open and close transitions.

## Components

Labels and rich text, buttons, icon buttons, toggles, checkboxes, radio groups,
sliders, number, text and password inputs, dropdowns, multi-select dropdowns,
search fields, colour pickers, keybind selectors, panels, cards, tabs, scroll
views, vertical, horizontal, grid and stack layouts, collapsible sections,
separators, tooltips, context menus, modal dialogs, popovers, searchable lists,
data tables, graphs, progress bars, loading indicators, colour gradients,
images, toasts and notifications, draggable and resizable windows.

## Native font renderer

The Rust crate in `native/font-renderer` exposes a C ABI and a JNI boundary. It
shapes text with HarfBuzz-compatible rules (`rustybuzz`), rasterises glyphs with
`ab_glyph` and maintains a glyph atlas. When the library cannot be loaded,
`FontLoader` silently falls back to Java2D.

The benchmark (`./gradlew :common:benchmark`, 16 px Inter, 38 characters) on
macOS arm64:

| Measurement        | Native   | Java2D   | Speedup |
| ------------------ | -------- | -------- | ------- |
| Cold (uncached)    | 45 964 ns | 1 342 931 ns | 29.2× |
| Warm (cached)      | 8 ns     | 18 ns     | 2.25×  |
| Measured width     | 330.68 px | 331.66 px | — |

Cold measurements create a fresh font and measure a string that has never been
seen before. Warm measurements hit the measurement cache, which is the steady
state of a running GUI. The two implementations agree on the width to within
one pixel.

## Known limitations

These are documented rather than hidden:

- **Dear ImGui inside Minecraft.** The draw-list translation in
  `common/.../render/imgui/` is complete and tested, but Minecraft's public GUI
  API only accepts rectangles and textures, not arbitrary triangles. Rendering
  an ImGui draw list in game therefore needs a custom `RenderPipeline`, which
  the mod does not fake. The in-game GUI uses `MinecraftRenderBackend`.
- **NanoVG is OpenGL only.** Minecraft 26.x can run on Vulkan, and 26.3 moved
  from GLFW to SDL3. `LwjglNanoVG` is provided as an optional backend for hosts
  that have an OpenGL context; the in-game GUI does not depend on it.
- **The native font renderer is verified on macOS.** The six binaries are cross
  compiled and their architectures were checked, but only the macOS arm64
  binary is exercised by the automated tests on the machine that produced this
  repository. The Java2D fallback is always available.
- **Shadows and blur are approximations.** Minecraft's GUI API has no blurred
  primitive, so shadows are drawn as a small stack of translucent rounded
  rectangles. NanoVG and ImGui use the same approximation.

## Documentation

`docs/DEVELOPING.md` covers the architecture in more detail, how to add a new
Minecraft version, how to rebuild the native libraries, and how to add
components.

## License

MIT. The bundled Inter font is licensed under the SIL Open Font License; see
`common/src/main/resources/fonts/Inter-LICENSE.txt`.
