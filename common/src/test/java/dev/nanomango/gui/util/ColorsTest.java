package dev.nanomango.gui.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorsTest {

    @Test
    @DisplayName("rgb packs the channels into an opaque colour")
    void rgb() {
        assertEquals(0xFF102030, Colors.rgb(0x10, 0x20, 0x30));
    }

    @Test
    @DisplayName("argb packs the alpha channel")
    void argb() {
        assertEquals(0x80102030, Colors.argb(0x80, 0x10, 0x20, 0x30));
    }

    @Test
    @DisplayName("channel accessors return the expected values")
    void accessors() {
        int color = 0x80102030;
        assertEquals(0x80, Colors.alpha(color));
        assertEquals(0x10, Colors.red(color));
        assertEquals(0x20, Colors.green(color));
        assertEquals(0x30, Colors.blue(color));
    }

    @Test
    @DisplayName("withAlpha replaces the alpha channel and keeps the colour")
    void withAlpha() {
        assertEquals(0x00FFFFFF, Colors.withAlpha(0xFFFFFFFF, 0x00));
        assertEquals(0x80123456, Colors.withAlpha(0xFF123456, 0x80));
    }

    @Test
    @DisplayName("withAlphaFactor scales the alpha channel")
    void withAlphaFactor() {
        assertEquals(0x80FFFFFF, Colors.withAlphaFactor(0xFFFFFFFF, 0.5F));
        assertEquals(0x00FFFFFF, Colors.withAlphaFactor(0xFFFFFFFF, 0.0F));
        assertEquals(0xFFFFFFFF, Colors.withAlphaFactor(0xFFFFFFFF, 2.0F));
    }

    @Test
    @DisplayName("mix blends two colours and clamps the factor")
    void mix() {
        assertEquals(0xFF000000, Colors.mix(0xFF000000, 0xFFFFFFFF, -1.0F));
        assertEquals(0xFFFFFFFF, Colors.mix(0xFF000000, 0xFFFFFFFF, 2.0F));
        assertEquals(0xFF808080, Colors.mix(0xFF000000, 0xFFFFFFFF, 0.5F));
    }

    @Test
    @DisplayName("hsv converts pure colours correctly")
    void hsv() {
        assertEquals(0xFFFF0000, Colors.hsv(0.0F, 1.0F, 1.0F, 1.0F));
        assertEquals(0xFF00FF00, Colors.hsv(120.0F, 1.0F, 1.0F, 1.0F));
        assertEquals(0xFF0000FF, Colors.hsv(240.0F, 1.0F, 1.0F, 1.0F));
        assertEquals(0xFFFFFFFF, Colors.hsv(0.0F, 0.0F, 1.0F, 1.0F));
        assertEquals(0xFF000000, Colors.hsv(0.0F, 0.0F, 0.0F, 1.0F));
    }

    @Test
    @DisplayName("rgbToHsv is the inverse of hsv")
    void rgbToHsvRoundTrip() {
        int[] samples = {
                Colors.hsv(0.0F, 1.0F, 1.0F, 1.0F),
                Colors.hsv(45.0F, 0.5F, 0.75F, 1.0F),
                Colors.hsv(210.0F, 0.8F, 0.4F, 1.0F),
                Colors.hsv(300.0F, 0.2F, 1.0F, 1.0F),
        };
        for (int sample : samples) {
            float[] hsv = new float[3];
            Colors.rgbToHsv(sample, hsv);
            int restored = Colors.hsv(hsv[0] * 360.0F, hsv[1], hsv[2], 1.0F);
            assertEquals(Colors.red(sample), Colors.red(restored));
            assertEquals(Colors.green(sample), Colors.green(restored));
            assertEquals(Colors.blue(sample), Colors.blue(restored));
        }
    }

    @Test
    @DisplayName("grey colours have no saturation")
    void grey() {
        float[] hsv = new float[3];
        Colors.rgbToHsv(0xFF808080, hsv);
        assertEquals(0.0F, hsv[1], 0.0001F);
        assertEquals(128.0F / 255.0F, hsv[2], 0.001F);
    }

    @Test
    @DisplayName("fraction accessors match the byte accessors")
    void fractions() {
        int color = 0x80102030;
        assertEquals(Colors.alpha(color) / 255.0F, Colors.alphaFraction(color), 0.0001F);
        assertEquals(Colors.red(color) / 255.0F, Colors.redFraction(color), 0.0001F);
        assertEquals(Colors.green(color) / 255.0F, Colors.greenFraction(color), 0.0001F);
        assertEquals(Colors.blue(color) / 255.0F, Colors.blueFraction(color), 0.0001F);
        assertTrue(Colors.alphaFraction(color) < 1.0F);
    }
}
