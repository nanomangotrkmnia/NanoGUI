package dev.nanomango.gui.render;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundedGeometryTest {

    @Test
    @DisplayName("the radius never exceeds half of the smaller side")
    void clampRadius() {
        assertEquals(5.0F, RoundedGeometry.clampRadius(20.0F, 10.0F, 8.0F));
        assertEquals(5.0F, RoundedGeometry.clampRadius(20.0F, 10.0F, 50.0F));
        assertEquals(0.0F, RoundedGeometry.clampRadius(0.0F, 10.0F, 8.0F));
        assertEquals(0.0F, RoundedGeometry.clampRadius(20.0F, 10.0F, -3.0F));
    }

    @Test
    @DisplayName("a tiny radius is drawn as a plain rectangle")
    void square() {
        assertTrue(RoundedGeometry.isSquare(0.0F));
        assertTrue(RoundedGeometry.isSquare(0.75F));
        assertFalse(RoundedGeometry.isSquare(0.76F));
        assertFalse(RoundedGeometry.isSquare(8.0F));
    }

    @Test
    @DisplayName("the staircase has one band per step")
    void bandCount() {
        assertEquals(RoundedGeometry.DEFAULT_STEPS, RoundedGeometry.bands(8, false).size());
        assertEquals(RoundedGeometry.DEFAULT_STEPS, RoundedGeometry.bands(8, true).size());
    }

    @Test
    @DisplayName("the bands stay inside the radius")
    void bandsInsideRadius() {
        int radius = 8;
        for (int[] band : RoundedGeometry.bands(radius, false)) {
            assertTrue(band[0] >= 0 && band[0] <= radius,
                    "the horizontal inset must stay inside the radius: " + band[0]);
            assertTrue(band[1] >= 0 && band[1] <= radius,
                    "the top offset must stay inside the radius: " + band[1]);
            assertTrue(band[2] >= 0 && band[2] <= radius,
                    "the bottom offset must stay inside the radius: " + band[2]);
        }
    }

    @Test
    @DisplayName("the staircase covers the full height of the corner")
    void bandsCoverTheCorner() {
        int radius = 9;
        List<int[]> bands = RoundedGeometry.bands(radius, false);
        // The first band starts at the top edge of the rectangle, the last
        // one reaches the middle of the rectangle.
        assertEquals(0, bands.get(0)[1]);
        assertEquals(0, bands.get(bands.size() - 1)[2]);
    }

    @Test
    @DisplayName("the inset shrinks towards the middle of the rectangle")
    void insetShrinks() {
        List<int[]> bands = RoundedGeometry.bands(12, false);
        int previous = Integer.MAX_VALUE;
        for (int[] band : bands) {
            assertTrue(band[0] <= previous,
                    "the inset must shrink while walking towards the middle");
            previous = band[0];
        }
        assertTrue(previous < 12, "the last band must be rounded off");
    }

    @Test
    @DisplayName("the geometry is cached and therefore stable")
    void caching() {
        List<int[]> first = RoundedGeometry.bands(11, false);
        List<int[]> second = RoundedGeometry.bands(11, false);
        assertTrue(first == second, "the same radius must reuse the cached geometry");

        assertFalse(first == RoundedGeometry.bands(11, true),
                "the wide variant must use its own cache entry");
    }
}
