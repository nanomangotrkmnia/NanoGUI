package dev.nanomango.gui.font;

import org.junit.jupiter.api.Test;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

class SpacingCompareTest {

    @Test
    void compare() throws Exception {
        String text = "Modules Settings Keybinds";
        byte[] data = FontLoader.read(FontLoader.INTER_REGULAR);

        java.awt.Font awt = java.awt.Font
                .createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(data))
                .deriveFont(16.0F);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector gv = awt.createGlyphVector(frc, text);

        NativeFont nativeFont = new NativeFont(1, "cmp", data, 16.0F, 1.0F);
        RecordingSink sink = new RecordingSink();
        nativeFont.draw(sink, text, 0.0F, 0.0F, 0xFFFFFFFF);

        StringBuilder log = new StringBuilder();
        float prevNative = Float.NaN;
        float prevJava = Float.NaN;
        int glyphIndex = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                continue;
            }
            Rectangle2D bounds = gv.getGlyphOutline(i).getBounds2D();
            float javaLeft = (float) bounds.getX();
            float nativeLeft = sink.glyphs.get(glyphIndex)[4];
            log.append(String.format("%c java=%.2f native=%.2f diff=%+.2f",
                    text.charAt(i), javaLeft, nativeLeft, nativeLeft - javaLeft));
            if (!Float.isNaN(prevJava)) {
                log.append(String.format("  gapJava=%.2f gapNative=%.2f  dGap=%+.2f",
                        javaLeft - prevJava, nativeLeft - prevNative,
                        (nativeLeft - prevNative) - (javaLeft - prevJava)));
            }
            log.append('\n');
            prevJava = javaLeft;
            prevNative = nativeLeft;
            glyphIndex++;
        }
        System.out.println(log);
        System.out.println("AV=" + nativeFont.width("AV")
                + " A+V=" + (nativeFont.width("A") + nativeFont.width("V"))
                + " To=" + nativeFont.width("To")
                + " T+o=" + (nativeFont.width("T") + nativeFont.width("o")));
        nativeFont.close();
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
