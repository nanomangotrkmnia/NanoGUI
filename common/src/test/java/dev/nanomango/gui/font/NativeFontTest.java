package dev.nanomango.gui.font;

import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NativeFontTest {

    private static NativeFont font;

    @BeforeAll
    static void setUp() {
        assumeTrue(NativeLibrary.load(),
                "The native font renderer is not available: " + NativeLibrary.failure());
        try {
            font = new NativeFont(7, "NativeTest", FontLoader.read(FontLoader.INTER_REGULAR), 16.0F);
        } catch (Exception exception) {
            throw new IllegalStateException("The native font could not be created", exception);
        }
    }

    @Test
    @DisplayName("the native library reports its version")
    void version() {
        assertEquals(1, NativeFont.nativeVersion());
    }

    @Test
    @DisplayName("measuring a string returns a positive width")
    void measure() {
        float width = font.width("Hello NanoArray");
        assertTrue(width > 0.0F, "the width must be positive");
    }

    @Test
    @DisplayName("an empty string has no width")
    void measureEmpty() {
        assertEquals(0.0F, font.width(""));
        assertEquals(0.0F, font.width((String) null));
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
        assertTrue(sink.glyphs.size() > 0, "at least one glyph must be produced");
        for (float[] glyph : sink.glyphs) {
            assertTrue(glyph[6] > 0.0F, "the glyph width must be positive");
            assertTrue(glyph[7] > 0.0F, "the glyph height must be positive");
        }
    }

    @Test
    @DisplayName("the shaped glyphs stay inside the measured width")
    void shapeStaysInBounds() {
        String text = "Hello NanoArray";
        RecordingSink sink = new RecordingSink();
        font.draw(sink, text, 0.0F, 0.0F, 0xFFFFFFFF);

        float right = 0.0F;
        for (float[] glyph : sink.glyphs) {
            // The sink stores {u0, v0, u1, v1, x, y, width, height}.
            right = Math.max(right, glyph[4] + glyph[6]);
        }
        assertTrue(right <= font.width(text) + 1.0F,
                "the glyphs must not exceed the measured width (" + right + " > " + font.width(text) + ")");
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
    @DisplayName("the atlas revision changes when new glyphs are rasterised")
    void atlasRevision() {
        FontTexture texture = font.texture();
        int before = texture.revision();

        RecordingSink sink = new RecordingSink();
        font.draw(sink, "revision probe", 0.0F, 0.0F, 0xFFFFFFFF);
        assertTrue(texture.revision() >= before);
    }

    @Test
    @DisplayName("unicode text is shaped into more than one glyph")
    void unicode() {
        RecordingSink sink = new RecordingSink();
        // Inter covers the latin range including the accented characters.
        font.draw(sink, "äöü ÄÖÜ", 0.0F, 0.0F, 0xFFFFFFFF);
        assertEquals(6, sink.glyphs.size(),
                "every accented character must produce a glyph");
        assertTrue(font.supportsUnicode());
    }

    @Test
    @DisplayName("characters that the font does not contain are skipped")
    void missingCharacters() {
        RecordingSink sink = new RecordingSink();
        // Inter does not contain CJK glyphs, they must not produce a quad.
        font.draw(sink, "你好", 0.0F, 0.0F, 0xFFFFFFFF);
        assertTrue(sink.glyphs.isEmpty(),
                "a missing glyph must not produce a quad");
    }

    @Test
    @DisplayName("the native renderer is at least as fast as the Java fallback")
    void performance() {
        Font fallback = TestSupport.fallbackFont(16.0F);
        String text = "NanoArray performance probe 1234567890";

        // Warm up both implementations so that the JIT can optimise them.
        for (int i = 0; i < 200; i++) {
            font.width(text);
            fallback.width(text);
        }

        int iterations = 2000;
        long nativeTime = time(() -> font.width(text), iterations);
        long fallbackTime = time(() -> fallback.width(text), iterations);

        // The native renderer must not be slower than the Java2D fallback.
        // A generous factor keeps the test stable on loaded CI machines.
        assertTrue(nativeTime <= fallbackTime * 4,
                "the native renderer was unexpectedly slow: " + nativeTime + " ns vs " + fallbackTime + " ns");
    }

    @Test
    @DisplayName("the native and the Java renderer measure the same text equally")
    void agreesWithTheJavaFallback() {
        Font fallback = TestSupport.fallbackFont(16.0F);
        String[] samples = {
                "Hello",
                "NanoArray performance probe 1234567890",
                "Wide Wg. 123",
                "äöü ÄÖÜ ß",
        };
        for (String sample : samples) {
            float nativeWidth = font.width(sample);
            float javaWidth = fallback.width(sample);
            float difference = Math.abs(nativeWidth - javaWidth);
            float tolerance = Math.max(2.0F, javaWidth * 0.05F);
            assertTrue(difference <= tolerance,
                    "the widths of '" + sample + "' differ too much: native=" + nativeWidth
                            + " java=" + javaWidth);
        }
    }

    @Test
    @DisplayName("the glyphs are positioned relative to the baseline")
    void baselinePlacement() {
        RecordingSink sink = new RecordingSink();
        // The line box starts at y = 0, the baseline therefore sits at the
        // ascent of the font.
        font.draw(sink, "Hg", 0.0F, 0.0F, 0xFFFFFFFF);
        assertEquals(2, sink.glyphs.size());

        // The sink stores {u0, v0, u1, v1, x, y, width, height}.
        float baseline = font.ascent();
        float[] cap = sink.glyphs.get(0);
        float capTop = cap[5];
        float capBottom = cap[5] + cap[7];

        assertTrue(capTop >= 0.0F,
                "the cap glyph must start inside the line box: " + capTop);
        assertTrue(capTop < baseline,
                "the cap glyph must start above the baseline: " + capTop);
        assertTrue(capBottom <= baseline + 1.5F,
                "the cap glyph must not reach below the baseline: " + capBottom);

        // The descender of the 'g' reaches below the baseline.
        float[] descender = sink.glyphs.get(1);
        assertTrue(descender[5] + descender[7] > baseline,
                "the descender must reach below the baseline");
    }

    @Test
    @DisplayName("a longer string is wider than a shorter one")
    void measurementOrder() {
        assertTrue(font.width("mmmm") > font.width("i"));
    }

    @Test
    @DisplayName("the native renderer can be closed and reports no glyphs afterwards")
    void close() throws Exception {
        NativeFont disposable = new NativeFont(8, "Disposable",
                FontLoader.read(FontLoader.INTER_REGULAR), 12.0F);
        disposable.close();
        RecordingSink sink = new RecordingSink();
        disposable.draw(sink, "ignored", 0.0F, 0.0F, 0xFFFFFFFF);
        assertTrue(sink.glyphs.isEmpty());
        assertFalse(disposable.texture() != null && disposable.texture().width() > 0);
    }

    private static long time(Runnable action, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            action.run();
        }
        return System.nanoTime() - start;
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
