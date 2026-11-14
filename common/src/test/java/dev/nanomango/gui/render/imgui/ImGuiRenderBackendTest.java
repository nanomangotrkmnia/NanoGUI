package dev.nanomango.gui.render.imgui;

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

class ImGuiRenderBackendTest {

    private RecordingImGui imgui;
    private ImGuiRenderBackend backend;
    private DrawList list;
    private FontRegistry fonts;

    @BeforeEach
    void setUp() {
        imgui = new RecordingImGui();
        backend = new ImGuiRenderBackend(imgui);
        list = new DrawList();
        fonts = new FontRegistry();
    }

    private void draw() {
        RenderContext context = new RenderContext().setViewport(800, 600);
        backend.beginFrame(context);
        backend.render(context, list, fonts);
        backend.endFrame(context);
    }

    @Test
    @DisplayName("a rectangle becomes a filled ImGui rectangle")
    void fillRect() {
        list.fillRect(1.0F, 2.0F, 30.0F, 40.0F, 0xFF112233);
        draw();

        assertEquals(List.of("addRectFilled 1.0 2.0 31.0 42.0 " + Integer.toString(0xFF112233) + " 0.0"),
                imgui.calls);
    }

    @Test
    @DisplayName("rounded rectangles keep their radius")
    void rounded() {
        list.fillRounded(0.0F, 0.0F, 10.0F, 20.0F, 4.0F, 0xFFFFFFFF);
        list.strokeRounded(0.0F, 0.0F, 10.0F, 20.0F, 4.0F, 2.0F, 0xFF000000);
        draw();

        assertTrue(imgui.calls.contains("addRectFilled 0.0 0.0 10.0 20.0 -1 4.0"), imgui.calls.toString());
        assertTrue(imgui.calls.contains("addRect 0.0 0.0 10.0 20.0 -16777216 4.0 2.0"), imgui.calls.toString());
    }

    @Test
    @DisplayName("a gradient uses one colour per corner")
    void gradient() {
        list.fillGradient(0.0F, 0.0F, 10.0F, 20.0F, 3.0F, 0xFF000000, 0xFFFFFFFF);
        draw();

        assertEquals(List.of("addRectFilledMultiColor 0.0 0.0 10.0 20.0 -16777216 -16777216 -1 -1"),
                imgui.calls);
    }

    @Test
    @DisplayName("a shadow is layered from large and transparent to small and opaque")
    void shadow() {
        list.shadow(10.0F, 10.0F, 100.0F, 50.0F, 8.0F, 12.0F, 0x80000000);
        draw();

        assertEquals(4, imgui.calls.size());
        assertTrue(alpha(imgui.calls.get(0)) < alpha(imgui.calls.get(3)), imgui.calls.toString());
        assertTrue(width(imgui.calls.get(0)) > width(imgui.calls.get(3)), imgui.calls.toString());
    }

    @Test
    @DisplayName("text carries the font id and its size")
    void text() {
        TestFont font = new TestFont(9, 14.0F);
        fonts.register(font);
        list.text(5.0F, 6.0F, 9, "Hello", 0xFFFFFFFF);
        draw();

        assertEquals(List.of("addText 9 14.0 5.0 6.0 -1 Hello"), imgui.calls);
    }

    @Test
    @DisplayName("a glyph becomes a textured quad")
    void glyph() {
        list.fillRect(0.0F, 0.0F, 0.0F, 0.0F, 0x00000000); // keep the list non empty
        draw();
        imgui.calls.clear();

        backend.beginFrame(new RenderContext());
        backend.glyph(1, 3, 0.0F, 0.0F, 0.5F, 0.5F, 10.0F, 20.0F, 5.0F, 6.0F, 0xFFFFFFFF);
        backend.endFrame(new RenderContext());

        assertEquals(List.of("addImage 3 10.0 20.0 15.0 26.0 0.0 0.0 0.5 0.5 -1"), imgui.calls);
    }

    @Test
    @DisplayName("textures use the normalised source rectangle")
    void texture() {
        list.texture(4, 0.25F, 0.5F, 0.5F, 0.25F, 10.0F, 20.0F, 40.0F, 10.0F, 0xFFFFFFFF);
        draw();

        assertEquals(List.of("addImage 4 10.0 20.0 50.0 30.0 0.25 0.5 0.75 0.75 -1"), imgui.calls);
    }

    @Test
    @DisplayName("clips are pushed and popped in order")
    void clipping() {
        list.clipPush(0.0F, 0.0F, 100.0F, 50.0F);
        list.fillRect(0.0F, 0.0F, 1.0F, 1.0F, 0xFFFFFFFF);
        list.clipPop();
        draw();

        assertEquals(List.of("pushClipRect 0.0 0.0 100.0 50.0 true",
                "addRectFilled 0.0 0.0 1.0 1.0 -1 0.0",
                "popClipRect"), imgui.calls);
    }

    @Test
    @DisplayName("an unbalanced clip pop is ignored")
    void unbalancedClipPop() {
        list.clipPop();
        draw();
        assertTrue(imgui.calls.isEmpty(), imgui.calls.toString());
    }

    @Test
    @DisplayName("an unclosed clip is unwound at the end of the frame")
    void unclosedClip() {
        list.clipPush(0.0F, 0.0F, 10.0F, 10.0F);
        draw();
        assertEquals(List.of("pushClipRect 0.0 0.0 10.0 10.0 true", "popClipRect"), imgui.calls);
    }

    @Test
    @DisplayName("lines, triangles and arcs reach the draw list")
    void primitives() {
        list.line(1.0F, 2.0F, 3.0F, 4.0F, 0xFF000000);
        list.triangle(1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 0xFFFFFFFF);
        list.arc(10.0F, 20.0F, 30.0F, 0.0F, 3.14F, 15.0F, 0xFF00FF00);
        draw();

        assertTrue(imgui.calls.contains("addLine 1.0 2.0 3.0 4.0 -16777216 1.0"), imgui.calls.toString());
        assertTrue(imgui.calls.contains("addTriangleFilled 1.0 2.0 3.0 4.0 5.0 6.0 -1"), imgui.calls.toString());
        assertTrue(imgui.calls.contains("addArcFilled 10.0 20.0 30.0 15.0 0.0 3.14 -16711936"),
                imgui.calls.toString());
    }

    @Test
    @DisplayName("the backend rejects a null draw list")
    void nullDrawList() {
        assertThrows(IllegalArgumentException.class, () -> new ImGuiRenderBackend(null));
    }

    @Test
    @DisplayName("the backend exposes its draw list")
    void exposesDrawList() {
        assertEquals(imgui, backend.drawList());
    }

    private static int alpha(String call) {
        String[] parts = call.split(" ");
        return Integer.parseInt(parts[parts.length - 2]) >>> 24;
    }

    private static float width(String call) {
        String[] parts = call.split(" ");
        return Float.parseFloat(parts[3]) - Float.parseFloat(parts[1]);
    }

    private static final class TestFont implements Font {

        private final int id;
        private final float height;

        private TestFont(int id, float height) {
            this.id = id;
            this.height = height;
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
            return height * 0.8F;
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
            sink.builtin(id, text, x, y, argb);
        }
    }

    private static final class RecordingImGui implements ImGuiDrawList {

        private final List<String> calls = new ArrayList<>();

        private static String colour(int argb) {
            return argb == 0xFFFFFFFF ? "-1" : Integer.toString(argb);
        }

        @Override
        public void addRectFilled(float x0, float y0, float x1, float y1, int argb, float rounding) {
            calls.add("addRectFilled " + x0 + " " + y0 + " " + x1 + " " + y1 + " "
                    + colour(argb) + " " + rounding);
        }

        @Override
        public void addRect(float x0, float y0, float x1, float y1, int argb, float rounding, float thickness) {
            calls.add("addRect " + x0 + " " + y0 + " " + x1 + " " + y1 + " "
                    + colour(argb) + " " + rounding + " " + thickness);
        }

        @Override
        public void addRectFilledMultiColor(float x0, float y0, float x1, float y1,
                                            int topLeft, int topRight, int bottomRight, int bottomLeft) {
            calls.add("addRectFilledMultiColor " + x0 + " " + y0 + " " + x1 + " " + y1 + " "
                    + colour(topLeft) + " " + colour(topRight) + " " + colour(bottomRight)
                    + " " + colour(bottomLeft));
        }

        @Override
        public void addText(int fontId, float size, float x, float y, int argb, String text) {
            calls.add("addText " + fontId + " " + size + " " + x + " " + y + " "
                    + colour(argb) + " " + text);
        }

        @Override
        public void addImage(int textureId, float x0, float y0, float x1, float y1,
                             float u0, float v0, float u1, float v1, int argb) {
            calls.add("addImage " + textureId + " " + x0 + " " + y0 + " " + x1 + " " + y1
                    + " " + u0 + " " + v0 + " " + u1 + " " + v1 + " " + colour(argb));
        }

        @Override
        public void addLine(float x0, float y0, float x1, float y1, int argb, float thickness) {
            calls.add("addLine " + x0 + " " + y0 + " " + x1 + " " + y1 + " "
                    + colour(argb) + " " + thickness);
        }

        @Override
        public void addTriangleFilled(float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
            calls.add("addTriangleFilled " + x0 + " " + y0 + " " + x1 + " " + y1 + " "
                    + x2 + " " + y2 + " " + colour(argb));
        }

        @Override
        public void addArcFilled(float centerX, float centerY, float outerRadius, float innerRadius,
                                 float startAngle, float endAngle, int argb) {
            calls.add("addArcFilled " + centerX + " " + centerY + " " + outerRadius + " "
                    + innerRadius + " " + startAngle + " " + endAngle + " " + colour(argb));
        }

        @Override
        public void pushClipRect(float x0, float y0, float x1, float y1, boolean intersect) {
            calls.add("pushClipRect " + x0 + " " + y0 + " " + x1 + " " + y1 + " " + intersect);
        }

        @Override
        public void popClipRect() {
            calls.add("popClipRect");
        }
    }
}
