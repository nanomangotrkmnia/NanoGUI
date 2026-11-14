package dev.nanomango.nanoarray.backend;

import dev.nanomango.gui.render.RoundedGeometry;

import java.util.List;

/**
 * Approximates rounded rectangles with axis aligned rectangles.
 *
 * <p>Minecraft's GUI API can only draw axis aligned rectangles, so rounded
 * corners are rendered as a small staircase. The geometry of a corner only
 * depends on the radius, therefore it is computed once and cached by
 * {@link RoundedGeometry}.</p>
 */
public final class RoundedRectangles {

    private RoundedRectangles() {
    }

    /**
     * Draws a rounded rectangle.
     */
    public static void draw(NanoArrayGraphics graphics, float x, float y, float width, float height,
                            float radius, int argb) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        float clamped = RoundedGeometry.clampRadius(width, height, radius);
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        if (RoundedGeometry.isSquare(clamped)) {
            graphics.fill(left, top, right, bottom, argb);
            return;
        }

        int r = Math.round(clamped);
        List<int[]> bands = RoundedGeometry.bands(r, width >= height * 2.0F);

        // Horizontal middle band covers the full width.
        graphics.fill(left, top + r, right, bottom - r, argb);

        for (int[] band : bands) {
            graphics.fill(left + band[0], top + band[1], right - band[0], bottom - band[1], argb);
        }
    }

    /**
     * Draws a rounded rectangle outline.
     */
    public static void stroke(NanoArrayGraphics graphics, float x, float y, float width, float height,
                               float radius, float thickness, int argb) {
        if (width <= 0.0F || height <= 0.0F || thickness <= 0.0F) {
            return;
        }
        int t = Math.max(1, Math.round(thickness));
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        float clamped = RoundedGeometry.clampRadius(width, height, radius);
        if (RoundedGeometry.isSquare(clamped)) {
            graphics.fill(left, top, right, top + t, argb);
            graphics.fill(left, bottom - t, right, bottom, argb);
            graphics.fill(left, top + t, left + t, bottom - t, argb);
            graphics.fill(right - t, top + t, right, bottom - t, argb);
            return;
        }

        int r = Math.round(clamped);
        graphics.fill(left + r, top, right - r, top + t, argb);
        graphics.fill(left + r, bottom - t, right - r, bottom, argb);
        graphics.fill(left, top + r, left + t, bottom - r, argb);
        graphics.fill(right - t, top + r, right, bottom - r, argb);

        List<int[]> bands = RoundedGeometry.bands(r, false);
        for (int[] band : bands) {
            graphics.fill(left + band[0], top + band[1], left + band[0] + t, top + band[1] + t, argb);
            graphics.fill(right - band[0] - t, top + band[1], right - band[0], top + band[1] + t, argb);
            graphics.fill(left + band[0], bottom - band[1] - t, left + band[0] + t, bottom - band[1], argb);
            graphics.fill(right - band[0] - t, bottom - band[1] - t, right - band[0], bottom - band[1], argb);
        }
    }
}
