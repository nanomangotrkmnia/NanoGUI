# Developing NanoGUI

This document explains how the framework is put together, how to add a
Minecraft version, how to rebuild the native font renderer and how to add a
component.

## Contents

1. [Design rules](#design-rules)
2. [Rendering pipeline](#rendering-pipeline)
3. [Text and fonts](#text-and-fonts)
4. [Input, theming and persistence](#input-theming-and-persistence)
5. [Project layout and build](#project-layout-and-build)
6. [Adding a Minecraft version](#adding-a-minecraft-version)
7. [Building the native font renderer](#building-the-native-font-renderer)
8. [Adding a component](#adding-a-component)
9. [Testing](#testing)
10. [Conventions](#conventions)
11. [Known limitations](#known-limitations)

## Design rules

1. **`common/` must not import a Minecraft type.** It is compiled with
   `--release 21` and consumed by every version module. If something needs a
   Minecraft class it belongs in `versions/`, behind an interface.
2. **The component tree never talks to a graphics API.** It appends commands to
   a `DrawList`; a `RenderBackend` replays them.
3. **Version differences are isolated.** Files in `versions/shared/` are shared
   by all versions, files in `versions/shared-26x/` only by the 26.x line, and
   the per-version module contains only what genuinely differs (screen
   management, render API, mod metadata).
4. **No placeholder behaviours.** A feature either works or is documented as a
   limitation. The known limitations are listed at the end of this file.
5. **No silent version substitution.** An unknown `mcVersion` fails the build.

## Rendering pipeline

```
Component.render(GuiContext)
        │  context.draw().fillRounded(...), .text(...), .clipPush(...)
        ▼
DrawList                       flat command stream, parallel primitive arrays
        │  RenderBackend.render(context, drawList, fonts)
        ▼
MinecraftRenderBackend  or  NanoVGRenderBackend  or  ImGuiRenderBackend
        │
        ▼
GuiGraphics / GuiGraphicsExtractor / NanoVG / ImDrawList
```

### `DrawList`

`common/src/main/java/dev/nanomango/gui/render/DrawList.java` stores each frame
in three parallel arrays (`opcodes`, `floats`, `ints`) plus a small string pool.
It never allocates while a frame is being recorded unless it has to grow. The
opcodes are:

| Opcode | Arguments |
| ------ | --------- |
| `OP_FILL_RECT` | `x, y, w, h`, colour |
| `OP_FILL_ROUNDED` | `x, y, w, h, radius`, colour |
| `OP_FILL_GRADIENT` | `x, y, w, h, radius`, top colour, bottom colour |
| `OP_STROKE_ROUNDED` | `x, y, w, h, radius, thickness`, colour |
| `OP_SHADOW` | `x, y, w, h, radius, blur`, colour |
| `OP_TEXT` | `x, y`, font id, colour, string index |
| `OP_TEXTURE` | `u0, v0, u1, v1, x, y, w, h`, texture id, colour |
| `OP_LINE` | `x1, y1, x2, y2`, colour |
| `OP_TRIANGLE` | `x1, y1, x2, y2, x3, y3`, colour |
| `OP_ARC` | `x, y, radius, startAngle, endAngle, innerRadius, reserved`, colour |
| `OP_CLIP_PUSH` | `x, y, w, h` |
| `OP_CLIP_POP` | — |

Backends walk the list with a command cursor and two argument cursors.
`DrawList.floatArgCount(opcode)` and `intArgCount(opcode)` give the stride, which
is also how an unknown opcode is skipped safely.

### Rounded corners

Minecraft's GUI has no path renderer, so rounded rectangles are composed from
primitives:

- two axis-aligned fills for the straight edges, and
- four anti-aliased corner sprites.

`CornerSprites` generates the sprites from the exact geometry of a disc. Each
pixel is sampled on a 4×4 grid and the covered fraction becomes its alpha. Four
orientations (top left, top right, bottom left, bottom right) are stored in one
texture. `RoundedCornerAtlas` uploads one texture per radius and per outline
thickness, keyed so that a texture is only generated once per session.
`RoundedRectangles` then draws a rounded rectangle with six draw calls and an
outline with eight.

The stroke sprites are generated as a ring between the outer radius and
`radius - thickness`. A thick stroke (thickness equal to the radius) degenerates
to the filled corner, which is what the tests assert.

Shadows are approximated: Minecraft has no blurred primitive, so two concentric
translucent rounded rectangles are stacked. NanoVG and ImGui use a four-layer
version of the same idea because their anti-aliased fills hide the layering.

### Backends

- **`MinecraftRenderBackend`** uses `NanoGUIGraphics`, a thin interface over
  `GuiGraphics` (1.21.11) and `GuiGraphicsExtractor` (26.x). Glyphs are blitted
  from per-font `DynamicTexture` atlases. Text that uses Minecraft's own font is
  drawn by Minecraft.
- **`NanoVGRenderBackend`** maps each command to a NanoVG path. Fonts can either
  be registered with NanoVG (`NanoVGFont`, one text run per string) or fall back
  to glyph quads. `LwjglNanoVG` is the LWJGL binding; it is `compileOnly`
  because Minecraft does not ship `lwjgl-nanovg`.
- **`ImGuiRenderBackend`** maps each command to `ImGuiDrawList`, the Java
  boundary of a native ImGui binding.

All three are tested against recording implementations, so the translation is
covered without a GPU.

## Text and fonts

`Font` is the unit of text measurement and drawing:

- `height()`, `ascent()`, `descent()`
- `width(String)` and `width(CharSequence, start, end)`
- `draw(TextSink, text, x, y, argb)`

The `(x, y)` origin is the **top left of the line box**. The baseline is
`y + ascent()`. Every backend and component follows this rule.

`FontLoader.create(...)` picks an implementation:

1. `NativeFont` when the platform library for the current OS and architecture
   can be loaded.
2. `AwtFont` otherwise.

Both share `MeasurementCache`, a bounded cache keyed by the text. That makes the
steady state of `width()` allocation free.

The atlas is rasterised at `size * rasterScale`, where `rasterScale` is the
Minecraft GUI scale (`NanoGUICore` passes it to `FontLoader`). Metrics and
advances are divided back into GUI units, so layout is unchanged and each glyph
maps one to one onto physical pixels. Without this, a 13 px atlas is upscaled
two or three times on a Retina display and the text looks soft.

`NativeFont` calls the Rust renderer through JNI. The native side keeps a glyph
atlas; `nShape` returns quads in the layout
`x, y, u0, v0, u1, v1, width, height`, where `y` is relative to the **baseline**
and the texture coordinates are normalised. `FontTexture` exposes the atlas to
the backends and carries a `revision()` counter so an atlas is only re-uploaded
when it changes.

## Input, theming and persistence

- `Gui` owns a `GuiContext`, which exposes the theme, the input state, the draw
  list and the font registry to components.
- `InputState` is the single source of truth for modifier keys, mouse buttons,
  scroll and the current cursor position. Component events are routed from the
  root; the root covers the viewport, so clicks cannot fall through.
- `Gui.close()` calls `InputState.reset()`, which prevents stuck keys after the
  GUI is closed while a modifier is held.
- `ThemeManager` holds the dark, light and custom themes. `Theme` is immutable;
  `Builder.copyOf(theme)` and `withAccent(...)` create variations without
  mutating the original. Fonts are injected with `setFonts(...)`, which rebuilds
  every theme and preserves their ids and order.
- `ConfigManager` writes JSON with Gson. Writes are debounced and the loader
  tolerates a missing, truncated or corrupt file by falling back to defaults.

## Project layout and build

```
common/                     pure Java framework, no Minecraft
versions/shared/            integration shared by all Minecraft versions
versions/shared-26x/        integration shared by the 26.x line
versions/1.21.11/           remapped, official Mojang mappings
versions/26.1.2/            no remapping
versions/26.2/              no remapping
versions/26.3/              no remapping
native/font-renderer/       Rust font engine and prebuilt binaries
native/build-native.sh      cross compilation script
```

Each version module:

- applies `splitEnvironmentSourceSets()`,
- adds `versions/shared/...` (and `versions/shared-26x/...` on 26.x) to the
  `client` source set,
- depends on `:common` as `compileOnly` and flattens the common jar into the
  mod jar, so a release is a single file,
- pins the LWJGL version to the one Minecraft ships, because two versions of the
  same native library on one classpath do not work.

```bash
./gradlew buildAll                 # every version
./gradlew -PmcVersion=26.2 build  # one version
./gradlew :common:test             # framework tests
./gradlew testAll                  # tests of every version project
./gradlew :common:benchmark        # native vs Java2D font benchmark
```

## Adding a Minecraft version

1. Add the properties to `gradle.properties`:

   ```properties
   minecraft.26_4.version=26.4
   minecraft.26_4.java=25
   minecraft.26_4.lwjgl=3.4.3
   minecraft.26_4.fabricApi=<fabric api build>
   minecraft.26_4.loomPlugin=net.fabricmc.fabric-loom
   ```

2. Add the directory name to `versionDirs` in `settings.gradle.kts`.

3. Copy an existing version module, for example
   `cp -R versions/26.3 versions/26.4`, then update its `build.gradle.kts`
   property keys (`minecraft.26_3_*` becomes `minecraft.26_4_*`). Pick the Loom
   plugin based on the mappings:

   - `fabric-loom-remap` plus `mappings(loom.officialMojangMappings())` when the
     version is obfuscated (the 1.21.x line),
   - `fabric-loom` without a mapping line for the unobfuscated 26.x line.

4. Update the client entry point (`NanoGUIClient`) and the platform adapter
   (`NanoGUIPlatform<version>`) if the screen API moved. Compare
   `NanoGUIPlatform1211`, `NanoGUIPlatform2612`, `NanoGUIPlatform262` and
   `NanoGUIPlatform263`.

5. If the render API changed, add or reuse a graphics adapter. 1.21.11 uses
   `GuiGraphicsAdapter`; 26.x uses `GuiGraphicsExtractorAdapter` from
   `versions/shared-26x`. Both implement `NanoGUIGraphics`.

6. Update `fabric.mod.json` if the Java requirement or the Fabric API range
   changed.

7. Run `./gradlew -PmcVersion=<version> build`, then `./gradlew buildAll`.

If the version is unknown, Gradle fails with an explicit error instead of
building a jar against the wrong Minecraft version.

## Building the native font renderer

### Requirements

- Rust (stable) with `rustup`
- Zig, used as the cross linker for Linux and Windows x86_64
- `cargo-xwin` for Windows on ARM64 (MSVC toolchain)

```bash
rustup target add x86_64-apple-darwin aarch64-apple-darwin \
    x86_64-pc-windows-gnu aarch64-pc-windows-msvc \
    x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu
cargo install cargo-zigbuild cargo-xwin
brew install zig   # or the package manager of your platform
```

### Build

```bash
./gradlew buildNative                 # all six targets
./native/build-native.sh linux-x86_64 # a single target
./gradlew checkNative                 # assert that all six binaries exist
```

The script writes the libraries to `native/font-renderer/dist/<platform>/`.
`common:jar` copies that directory into the jar under
`dev/nanomango/native/`, so the shipped mod contains every binary and end users
never need a compiler.

The Rust crate has a `jni` feature. The distributed libraries are built with
`--features jni`; the plain `cargo build` without the feature exposes only the C
ABI, which is useful for embedding the renderer outside Java.

## Adding a component

1. Create a class in `common/src/main/java/dev/nanomango/gui/components/` that
   extends `Component`.
2. Implement `render(GuiContext)` and append to `context.draw()`.
3. Override `preferredWidth` / `preferredHeight` so layouts can size it.
4. Override `mouseClicked`, `mouseReleased`, `keyPressed`, or `charTyped` only
   when the component needs them, and return `true` when the event was
   consumed.
5. Use the theme helpers (`backgroundColor`, `textColor`, `radius`, `padding`)
   instead of hard-coded colours or sizes.
6. Add a test in `common/src/test/java/dev/nanomango/gui/components/`.

Minimal example:

```java
package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

public final class Badge extends Component {

    private final String label;

    public Badge(String label) {
        this.label = label;
    }

    @Override
    public void render(GuiContext context) {
        context.draw().fillRounded(x(), y(), width(), height(), radius(context), accentColor(context));
        context.draw().text(x() + 6.0F, y() + 4.0F, context.fonts().defaultFontId(),
                label, textColor(context));
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return context.fonts().defaultFont().width(label) + 12.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.fonts().defaultFont().height() + 8.0F;
    }
}
```

## Testing

```bash
./gradlew :common:test        # 174 Java tests
./gradlew testAll             # the same suite from every version project
cd native/font-renderer && cargo test --release
```

- Java tests use JUnit 5. `common/build.gradle.kts` passes the host native
  library through `-Dnanogui.native.path`, so the native path is exercised on
  the build machine.
- Backends are tested against recording implementations (`RecordingNanoVG`,
  `RecordingImGui`), which assert the exact call that a draw list command
  produces.
- `FontBenchmark` is run with `./gradlew :common:benchmark`. It prints the
  numbers quoted in the README; do not copy benchmark numbers into
  documentation without running it on the machine that produced them.

When you change a backend, add a test that observes the generated call rather
than only checking that nothing throws. The recording adapters exist precisely
so the translation is covered without a GPU.

## Conventions

- **Coordinates** are GUI units (not physical pixels). The root container is
  resized to the viewport every frame unless `Gui.autoSizeRoot(false)` is set.
- **Colours** are packed `0xAARRGGBB`. Use `Colors` for conversion and
  `MathUtil.lerpColor` for interpolation; both round instead of truncating.
- **Minecraft rectangle bounds** are exclusive at the bottom right, matching
  `GuiGraphics.fill`. A rectangle from `x` to `x + w` is `w` wide.
- **Text origin** is the top left of the line box; the baseline is
  `y + font.ascent()`.
- **Delta time** drives every animation. Do not assume a fixed frame rate.
- **Avoid allocation in `render`.** Reuse objects, use the `DrawList` primitives
  and the measurement cache.
- **Public API is documented with Javadoc.** New interfaces and components need
  it.

## Known limitations

- **ImGui cannot be rendered through Minecraft's public GUI API.** The
  translation layer is complete and tested, but `GuiGraphics` and
  `GuiGraphicsExtractor` only accept rectangles and textures. Drawing triangles
  would require a custom `RenderPipeline`. The in-game GUI uses
  `MinecraftRenderBackend`; the ImGui backend is available to hosts that can
  submit its draw list themselves. This is not hidden with a fake renderer.
- **NanoVG needs an OpenGL context.** 26.x can run on Vulkan and 26.3 uses SDL3
  instead of GLFW. `LwjglNanoVG` is optional and not used by `NanoGUICore`.
- **The native font renderer is tested on macOS arm64.** The other five
  binaries are cross compiled and their architectures checked with `file`; the
  Java2D fallback covers every platform when the native library is unavailable.
- **Shadows are approximations.** No supported backend exposes a real blur
  primitive; the backends stack translucent rounded rectangles.
