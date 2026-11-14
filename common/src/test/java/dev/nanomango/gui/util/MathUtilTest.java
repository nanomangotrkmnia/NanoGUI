package dev.nanomango.gui.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilTest {

    @Test
    @DisplayName("clamp keeps the value inside the range")
    void clamp() {
        assertEquals(5.0F, MathUtil.clamp(5.0F, 0.0F, 10.0F));
        assertEquals(0.0F, MathUtil.clamp(-3.0F, 0.0F, 10.0F));
        assertEquals(10.0F, MathUtil.clamp(42.0F, 0.0F, 10.0F));
    }

    @Test
    @DisplayName("clamp handles an inverted range")
    void clampInverted() {
        // With min greater than max the upper bound wins.
        assertEquals(0.0F, MathUtil.clamp(5.0F, 1.0F, 0.0F));
    }

    @Test
    @DisplayName("lerp interpolates linearly")
    void lerp() {
        assertEquals(0.0F, MathUtil.lerp(0.0F, 10.0F, 0.0F));
        assertEquals(10.0F, MathUtil.lerp(0.0F, 10.0F, 1.0F));
        assertEquals(5.0F, MathUtil.lerp(0.0F, 10.0F, 0.5F));
    }

    @Test
    @DisplayName("saturate limits the value to the unit interval")
    void saturate() {
        assertEquals(0.0F, MathUtil.saturate(-1.0F));
        assertEquals(1.0F, MathUtil.saturate(2.0F));
        assertEquals(0.25F, MathUtil.saturate(0.25F));
    }

    @Test
    @DisplayName("snap rounds to the nearest multiple of the step")
    void snap() {
        assertEquals(0.0F, MathUtil.snap(0.4F, 1.0F));
        assertEquals(1.0F, MathUtil.snap(0.6F, 1.0F));
        assertEquals(2.5F, MathUtil.snap(2.4F, 0.5F));
    }

    @Test
    @DisplayName("roundToPixel snaps to the pixel grid")
    void roundToPixel() {
        assertEquals(3.0F, MathUtil.roundToPixel(3.4F, 1.0F));
        assertEquals(4.0F, MathUtil.roundToPixel(3.6F, 1.0F));
        // On a high density display the grid is twice as fine.
        assertEquals(3.5F, MathUtil.roundToPixel(3.46F, 2.0F));
        assertEquals(3.0F, MathUtil.roundToPixel(3.24F, 2.0F));
    }

    @Test
    @DisplayName("lerpColor blends two packed colours")
    void lerpColor() {
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;
        int middle = MathUtil.lerpColor(black, white, 0.5F);
        // The alpha channel is identical in both colours and therefore kept.
        assertEquals(255, (middle >> 24) & 0xFF);
        assertEquals(128, (middle >> 16) & 0xFF);
        assertEquals(128, (middle >> 8) & 0xFF);
        assertEquals(128, middle & 0xFF);
    }

    @Test
    @DisplayName("approach moves a value towards a target")
    void approach() {
        float value = 0.0F;
        value = MathUtil.approach(value, 10.0F, 0.5F);
        assertEquals(5.0F, value);
        value = MathUtil.approach(value, 10.0F, 0.5F);
        assertEquals(7.5F, value);
    }

    @Test
    @DisplayName("approximately compares floats with a tolerance")
    void approximately() {
        assertTrue(MathUtil.approximately(1.0F, 1.0001F, 0.001F));
        assertTrue(!MathUtil.approximately(1.0F, 1.1F, 0.001F));
    }
}
