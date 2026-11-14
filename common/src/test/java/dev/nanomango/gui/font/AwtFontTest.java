package dev.nanomango.gui.font;

import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwtFontTest {

    private static AwtFont font;

    @BeforeAll
    static void setUp() {
        font = (AwtFont) TestSupport.fallbackFont(16.0F);
    }

    @Test
    @DisplayName("measuring a string returns a positive width")
    void measure() {
        assertTrue(font.width("Hello NanoArray") > 0.0F);
    }

    @Test
    @DisplayName("an empty string has no width")
    void measureEmpty() {
        assertEquals(0.0F, font.width(""));
        assertEquals(0.0F, font.width((String) null));
    }

    @Test
    @DisplayName("whitespace advances the pen")
    void whitespaceAdvances() {
        // A space must take up space, otherwise the text would overlap.
        assertTrue(font.width(" ") > 0.0F);
        assertTrue(font.width("a b") > font.width("ab"));
    }

    @Test
    @DisplayName("a longer string is wider than a shorter one")
    void measureOrder() {
        assertTrue(font.width("mmmm") > font.width("i"));
    }

    @Test
    @DisplayName("the font exposes sane vertical metrics")
    void metrics() {
        assertTrue(font.ascent() > 0.0F);
        assertTrue(font.descent() > 0.0F);
        assertTrue(font.height() >= font.ascent());
    }

    @Test
    @DisplayName("shaping produces glyph quads")
    void shape() {
        RecordingSink sink = new RecordingSink();
        font.draw(sink, "Hello", 10.0F, 20.0F, 0xFFFFFFFF);
        assertEquals(5, sink.glyphs.size());
        for (float[] glyph : sink.glyphs) {
            assertTrue(glyph[6] > 0.0F, "the glyph width must be positive");
            assertTrue(glyph[7] > 0.0F, "the glyph height must be positive");
        }
    }

    @Test
    @DisplayName("the glyphs are positioned relative to the baseline")
    void baselinePlacement() {
        RecordingSink sink = new RecordingSink();
        font.draw(sink, "Hg", 0.0F, 0.0F, 0xFFFFFFFF);
        assertEquals(2, sink.glyphs.size());

        // The sink stores {u0, v0, u1, v1, x, y, width, height}.
        float baseline = font.ascent();
        float[] cap = sink.glyphs.get(0);
        assertTrue(cap[5] >= 0.0F, "the cap glyph must start inside the line box");
        assertTrue(cap[5] < baseline, "the cap glyph must start above the baseline");
        assertTrue(cap[5] + cap[7] <= baseline + 1.5F,
                "the cap glyph must not reach below the baseline");

        float[] descender = sink.glyphs.get(1);
        assertTrue(descender[5] + descender[7] > baseline,
                "the descender must reach below the baseline");
    }

    @Test
    @DisplayName("the glyph atlas contains rasterised pixels")
    void atlas() {
        RecordingSink sink = new RecordingSink();
        font.draw(sink, "Atlas test", 0.0F, 0.0F, 0xFFFFFFFF);

        FontTexture texture = font.texture();
        assertNotNull(texture);
        assertTrue(texture.width() > 0);
        assertTrue(texture.height() > 0);

        int[] pixels = new int[texture.width() * texture.height()];
        texture.writePixels(pixels);

        int opaque = 0;
        for (int pixel : pixels) {
            if ((pixel >>> 24) != 0) {
                opaque++;
            }
        }
        assertTrue(opaque > 0, "the atlas must contain rasterised glyphs");
    }

    @Test
    @DisplayName("the measurement cache returns the same result")
    void measurementCache() {
        float first = font.width("cached");
        float second = font.width("cached");
        assertEquals(first, second, 0.0F);
    }

    @Test
    @DisplayName("a raster scale keeps the logical metrics and doubles the atlas resolution")
    void rasterScale() throws Exception {
        String text = "Hello NanoArray";
        AwtFont scaled = new AwtFont(2, "Scaled",
                FontLoader.read(FontLoader.INTER_REGULAR), 16.0F, java.awt.Font.PLAIN, 2.0F);
        assertEquals(font.width(text), scaled.width(text), 0.5F);
        assertEquals(font.ascent(), scaled.ascent(), 0.5F);

        RecordingSink plainSink = new RecordingSink();
        RecordingSink scaledSink = new RecordingSink();
        font.draw(plainSink, text, 0.0F, 0.0F, 0xFFFFFFFF);
        scaled.draw(scaledSink, text, 0.0F, 0.0F, 0xFFFFFFFF);
        assertEquals(plainSink.glyphs.size(), scaledSink.glyphs.size());

        float plainWidth = plainSink.glyphs.get(0)[6];
        float scaledWidth = scaledSink.glyphs.get(0)[6];
        assertEquals(plainWidth, scaledWidth, 1.0F);

        float[] plain = plainSink.glyphs.get(0);
        float[] hiRes = scaledSink.glyphs.get(0);
        float plainPixels = (plain[2] - plain[0]) * font.texture().width();
        float hiResPixels = (hiRes[2] - hiRes[0]) * scaled.texture().width();
        assertTrue(hiResPixels > plainPixels * 1.5F,
                "the scaled atlas must store a larger glyph: " + hiResPixels + " vs " + plainPixels);
    }

    /**
     * Sink that records every glyph that is pushed into it.
     */
    private static final class RecordingSink implements TextSink {

        final List<float[]> glyphs = new ArrayList<>();

        @Override
        public void builtin(int fontId, String text, float x, float y, int argb) {
        }

        @Override
        public void glyph(int fontId, int textureId,
                          float u0, float v0, float u1, float v1,
                          float x, float y, float width, float height, int argb) {
            glyphs.add(new float[]{u0, v0, u1, v1, x, y, width, height});
        }
    }
}
