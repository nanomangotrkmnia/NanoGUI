package dev.nanomango.gui.render.nvg;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.font.TextSink;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NanoVGRenderBackendTest {

    private RecordingNanoVG nvg;
    private NanoVGRenderBackend backend;
    private DrawList list;
    private FontRegistry fonts;
    private RenderContext context;

    @BeforeEach
    void setUp() {
        nvg = new RecordingNanoVG();
        backend = new NanoVGRenderBackend(nvg);
        list = new DrawList();
        fonts = new FontRegistry();
        context = new RenderContext().setViewport(800, 600).setScale(2.0F);
    }

    private void draw() {
        backend.beginFrame(context);
        backend.render(context, list, fonts);
        backend.endFrame(context);
    }

    @Test
    @DisplayName("a frame starts and ends exactly once")
    void frameLifecycle() {
        draw();
        assertEquals(List.of("beginFrame 800.0 600.0 2.0", "endFrame"), nvg.calls);
    }

    @Test
    @DisplayName("a rectangle becomes a NanoVG fill")
    void fillRect() {
        list.fillRect(1.0F, 2.0F, 30.0F, 40.0F, 0xFF112233);
        draw();

        assertEquals(List.of("fill 1.0 2.0 30.0 40.0 " + Integer.toString(0xFF112233)),
                nvg.calls.subList(1, 2));
    }

    @Test
    @DisplayName("rounded rectangles, gradients and outlines reach NanoVG")
    void roundedShapes() {
        list.fillRounded(1.0F, 2.0F, 30.0F, 40.0F, 6.0F, 0xFF112233);
        list.fillGradient(1.0F, 2.0F, 30.0F, 40.0F, 6.0F, 0xFF000000, 0xFFFFFFFF);
        list.strokeRounded(1.0F, 2.0F, 30.0F, 40.0F, 6.0F, 2.0F, 0xFF445566);
        draw();

        assertTrue(nvg.calls.stream().anyMatch(call -> call.startsWith("fillRounded 1.0 2.0 30.0 40.0 6.0")));
        assertTrue(nvg.calls.stream().anyMatch(call -> call.startsWith("fillRoundedGradient 1.0 2.0 30.0 40.0 6.0")));
        assertTrue(nvg.calls.stream().anyMatch(call -> call.startsWith("strokeRounded 1.0 2.0 30.0 40.0 6.0 2.0")));
    }

    @Test
    @DisplayName("a shadow is layered from large and transparent to small and opaque")
    void shadow() {
        list.shadow(10.0F, 10.0F, 100.0F, 50.0F, 8.0F, 12.0F, 0x80000000);
        draw();

        List<String> layers = nvg.calls.stream()
                .filter(call -> call.startsWith("fillRounded"))
                .toList();
        assertEquals(4, layers.size());

        // The first layer is the largest one and has the smallest alpha.
        assertTrue(alpha(layers.get(0)) < alpha(layers.get(3)),
                "the outer layers must be more transparent: " + layers);

        // The layers shrink continuously.
        float firstExpand = expand(layers.get(0));
        float lastExpand = expand(layers.get(3));
        assertTrue(firstExpand > lastExpand,
                "the outer layer must be larger: " + layers);
    }

    @Test
    @DisplayName("a font with a NanoVG handle is drawn as a text run at the baseline")
    void textWithNanoVGFont() {
        TestFont font = new TestFont(7, 12.0F, 4.0F);
        fonts.register(font);
        backend.fontResolver(id -> id == 7 ? new NanoVGFont(42, 13.0F) : null);

        list.text(10.0F, 20.0F, 7, "Hello", 0xFFFFFFFF);
        draw();

        assertTrue(nvg.calls.contains("text 42 Hello 10.0 24.0 13.0 -1"), nvg.calls.toString());
    }

    @Test
    @DisplayName("a font without a NanoVG handle falls back to the glyph atlas")
    void textFallsBackToGlyphs() {
        TestFont font = new TestFont(7, 12.0F, 4.0F);
        fonts.register(font);
        backend.imageResolver(id -> id == 3 ? new NanoVGImage(9, 256, 256) : null);

        list.text(10.0F, 20.0F, 7, "Hi", 0xFFFFFFFF);
        draw();

        // The test font emits one glyph quad per character.
        assertEquals(2, nvg.calls.stream().filter(call -> call.startsWith("image 9")).count(),
                nvg.calls.toString());
        assertTrue(nvg.calls.stream().noneMatch(call -> call.startsWith("text")), nvg.calls.toString());
    }

    @Test
    @DisplayName("a glyph without a registered atlas is skipped")
    void unknownGlyphAtlas() {
        TestFont font = new TestFont(7, 12.0F, 4.0F);
        fonts.register(font);
        backend.imageResolver(id -> null);

        list.text(0.0F, 0.0F, 7, "Hi", 0xFFFFFFFF);
        draw();

        assertTrue(nvg.calls.stream().noneMatch(call -> call.startsWith("image")), nvg.calls.toString());
    }

    @Test
    @DisplayName("a texture uses the normalised source rectangle")
    void texture() {
        backend.imageResolver(id -> id == 5 ? new NanoVGImage(3, 64, 64) : null);
        list.texture(5, 0.25F, 0.5F, 0.5F, 0.25F, 10.0F, 20.0F, 40.0F, 10.0F, 0xFFFFFFFF);
        draw();

        assertTrue(nvg.calls.contains("image 3 10.0 20.0 40.0 10.0 0.25 0.5 0.75 0.75 -1"),
                nvg.calls.toString());
    }

    @Test
    @DisplayName("clips are balanced with save, scissor and restore")
    void clipping() {
        list.clipPush(0.0F, 0.0F, 100.0F, 100.0F);
        list.fillRect(0.0F, 0.0F, 10.0F, 10.0F, 0xFFFFFFFF);
        list.clipPop();
        draw();

        assertEquals(List.of("beginFrame 800.0 600.0 2.0", "save", "scissor 0.0 0.0 100.0 100.0",
                "fill 0.0 0.0 10.0 10.0 -1", "restore", "endFrame"), nvg.calls);
    }

    @Test
    @DisplayName("an unbalanced clip pop is ignored")
    void unbalancedClipPop() {
        list.clipPop();
        draw();

        assertTrue(nvg.calls.stream().noneMatch(call -> call.equals("restore")), nvg.calls.toString());
    }

    @Test
    @DisplayName("an unclosed clip is unwound at the end of the frame")
    void unclosedClip() {
        list.clipPush(0.0F, 0.0F, 100.0F, 100.0F);
        draw();

        assertTrue(nvg.calls.contains("restore"), nvg.calls.toString());
    }

    @Test
    @DisplayName("lines, triangles and arcs reach NanoVG")
    void primitives() {
        list.line(1.0F, 2.0F, 3.0F, 4.0F, 0xFF000000);
        list.triangle(1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 0xFFFFFFFF);
        list.arc(10.0F, 20.0F, 30.0F, 0.0F, 3.14F, 15.0F, 0xFF00FF00);
        draw();

        assertTrue(nvg.calls.contains("line 1.0 2.0 3.0 4.0 1.0 -16777216"));
        assertTrue(nvg.calls.contains("triangle 1.0 2.0 3.0 4.0 5.0 6.0 -1"));
        assertTrue(nvg.calls.contains("arc 10.0 20.0 30.0 15.0 0.0 3.14 -16711936"));
    }

    @Test
    @DisplayName("a NanoVG font needs a positive handle and size")
    void fontValidation() {
        assertThrows(IllegalArgumentException.class, () -> new NanoVGFont(0, 12.0F));
        assertThrows(IllegalArgumentException.class, () -> new NanoVGFont(-1, 12.0F));
        assertThrows(IllegalArgumentException.class, () -> new NanoVGFont(1, 0.0F));
        assertTrue(new NanoVGFont(1, 12.0F).valid());
    }

    @Test
    @DisplayName("a NanoVG image needs a positive handle and dimensions")
    void imageValidation() {
        assertThrows(IllegalArgumentException.class, () -> new NanoVGImage(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new NanoVGImage(1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new NanoVGImage(1, 1, 0));
        assertEquals(3, new NanoVGImage(1, 2, 3).height());
    }

    @Test
    @DisplayName("the backend rejects a null implementation")
    void nullImplementation() {
        assertThrows(IllegalArgumentException.class, () -> new NanoVGRenderBackend(null));
    }

    @Test
    @DisplayName("the backend exposes its NanoVG implementation")
    void exposesImplementation() {
        assertEquals(nvg, backend.nanoVG());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static int alpha(String layer) {
        String[] parts = layer.split(" ");
        return Integer.parseInt(parts[parts.length - 1]) >>> 24;
    }

    private static float expand(String layer) {
        String[] parts = layer.split(" ");
        // The width of the layer is inflate + width. The source width is 100.
        return (Float.parseFloat(parts[3]) - 100.0F) * 0.5F;
    }

    /**
     * A font that pushes one glyph quad per character so that the fallback
     * path of the backend can be observed.
     */
    private static final class TestFont implements Font {

        private final int id;
        private final float height;
        private final float ascent;

        private TestFont(int id, float height, float ascent) {
            this.id = id;
            this.height = height;
            this.ascent = ascent;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public String name() {
            return "TestFont";
        }

        @Override
        public float height() {
            return height;
        }

        @Override
        public float ascent() {
            return ascent;
        }

        @Override
        public float width(String text) {
            return text.length() * 6.0F;
        }

        @Override
        public float width(CharSequence text, int start, int end) {
            return (end - start) * 6.0F;
        }

        @Override
        public void draw(TextSink sink, String text, float x, float y, int argb) {
            float cursor = x;
            for (int i = 0; i < text.length(); i++) {
                sink.glyph(id, 3, 0.0F, 0.0F, 0.5F, 0.5F, cursor, y, 6.0F, height, argb);
                cursor += 6.0F;
            }
        }
    }

    /**
     * Records every NanoVG call as a human readable string.
     */
    private static final class RecordingNanoVG implements NanoVG {

        private final List<String> calls = new ArrayList<>();

        private static String colour(int argb) {
            return argb == 0xFFFFFFFF ? "-1" : Integer.toString(argb);
        }

        @Override
        public void beginFrame(float width, float height, float pixelRatio) {
            calls.add("beginFrame " + width + " " + height + " " + pixelRatio);
        }

        @Override
        public void endFrame() {
            calls.add("endFrame");
        }

        @Override
        public void save() {
            calls.add("save");
        }

        @Override
        public void restore() {
            calls.add("restore");
        }

        @Override
        public void translate(float x, float y) {
            calls.add("translate " + x + " " + y);
        }

        @Override
        public void scissor(float x, float y, float width, float height) {
            calls.add("scissor " + x + " " + y + " " + width + " " + height);
        }

        @Override
        public void resetScissor() {
            calls.add("resetScissor");
        }

        @Override
        public void fill(float x, float y, float width, float height, int argb) {
            calls.add("fill " + x + " " + y + " " + width + " " + height + " " + colour(argb));
        }

        @Override
        public void fillRounded(float x, float y, float width, float height, float radius, int argb) {
            calls.add("fillRounded " + x + " " + y + " " + width + " " + height + " " + radius
                    + " " + colour(argb));
        }

        @Override
        public void fillRoundedGradient(float x, float y, float width, float height, float radius,
                                        int topArgb, int bottomArgb) {
            calls.add("fillRoundedGradient " + x + " " + y + " " + width + " " + height + " " + radius
                    + " " + colour(topArgb) + " " + colour(bottomArgb));
        }

        @Override
        public void strokeRounded(float x, float y, float width, float height, float radius,
                                  float thickness, int argb) {
            calls.add("strokeRounded " + x + " " + y + " " + width + " " + height + " " + radius
                    + " " + thickness + " " + colour(argb));
        }

        @Override
        public void line(float x1, float y1, float x2, float y2, float thickness, int argb) {
            calls.add("line " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + thickness + " " + colour(argb));
        }

        @Override
        public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
            calls.add("triangle " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x3 + " " + y3
                    + " " + colour(argb));
        }

        @Override
        public void arc(float centerX, float centerY, float outerRadius, float innerRadius,
                        float startAngle, float endAngle, int argb) {
            calls.add("arc " + centerX + " " + centerY + " " + outerRadius + " " + innerRadius
                    + " " + startAngle + " " + endAngle + " " + colour(argb));
        }

        @Override
        public void text(int fontHandle, String text, float x, float baseline, float size, int argb) {
            calls.add("text " + fontHandle + " " + text + " " + x + " " + baseline + " " + size
                    + " " + colour(argb));
        }

        @Override
        public void image(int imageHandle, float x, float y, float width, float height,
                          float u0, float v0, float u1, float v1, int argb) {
            calls.add("image " + imageHandle + " " + x + " " + y + " " + width + " " + height
                    + " " + u0 + " " + v0 + " " + u1 + " " + v1 + " " + colour(argb));
        }
    }
}
