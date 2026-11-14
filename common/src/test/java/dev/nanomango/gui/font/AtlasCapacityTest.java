package dev.nanomango.gui.font;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Checks that a large amount of interface text fits in the glyph atlas. A glyph
 * that did not fit used to be dropped silently, which left holes in the text
 * that look like broken letter spacing.
 */
class AtlasCapacityTest {

    /** Letters, digits and punctuation that are definitely present in Inter. */
    private static final String CORPUS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz "
                    + "0123456789 .,:;!?()[]-+/%#@&*";

    @Test
    @DisplayName("the atlas grows instead of dropping glyphs at a high raster scale")
    void atlasGrowsInsteadOfDroppingGlyphs() throws Exception {
        assumeTrue(NativeLibrary.load(), "the native renderer is not available");
        // A 13 px font at raster scale 6 produces 78 px glyphs, which cannot
        // all fit in the initial 512 x 512 atlas at four subpixel phases.
        NativeFont font = new NativeFont(1, "capacity",
                FontLoader.read(FontLoader.INTER_REGULAR), 13.0F, 6.0F);
        try {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                text.append(CORPUS);
            }

            RecordingSink sink = new RecordingSink();
            font.draw(sink, text.toString(), 0.0F, 0.0F, 0xFFFFFFFF);

            int expected = 0;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != ' ') {
                    expected++;
                }
            }
            assertEquals(expected, sink.glyphs.size(),
                    "every character must produce a glyph instead of being dropped");
            assertTrue(font.texture().width() > 512,
                    "the atlas must grow rather than overflow: " + font.texture().width());
        } finally {
            font.close();
        }
    }

    private static final class RecordingSink implements TextSink {
        final List<float[]> glyphs = new ArrayList<>();

        @Override
        public void builtin(int fontId, String text, float x, float y, int argb) {
        }

        @Override
        public void glyph(int fontId, int textureId, float u0, float v0, float u1, float v1,
                          float x, float y, float width, float height, int argb) {
            glyphs.add(new float[]{u0, v0, u1, v1, x, y, width, height});
        }
    }
}
