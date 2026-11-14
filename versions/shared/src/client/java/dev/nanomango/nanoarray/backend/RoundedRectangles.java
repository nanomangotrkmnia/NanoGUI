package dev.nanomango.nanoarray.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Approximates rounded rectangles with axis aligned rectangles.
 *
 * <p>Minecraft's GUI API can only draw axis aligned rectangles, so rounded
 * corners are rendered as a small staircase. The geometry of a corner only
 * depends on the radius, therefore it is computed once and cached.</p>
 */
public final class RoundedRectangles {

    private static final int DEFAULT_STEPS = 3;
    private static final Map<Long, List<int[]>> CACHE = new HashMap<>();

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
        float clamped = Math.min(radius, Math.min(width, height) * 0.5F);
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        if (clamped <= 0.75F) {
            graphics.fill(left, top, right, bottom, argb);
            return;
        }

        int r = Math.round(clamped);
        List<int[]> bands = bands(r, width >= height * 2.0F);

        // Horizontal middle band covers the full width.
        graphics.fill(left, top + r, right, bottom - r, argb);

        for (int[] band : bands) {
            graphics.fill(left + band[0], top + band[1], right - band[0], bottom - band[1], argb);
        }
    }

    /**
     * Computes the corner staircase.
     *
     * @param radius the corner radius in pixels
     * @return an array of {@code [insetX, insetTop, insetBottom]} triples
     */
    private static List<int[]> bands(int radius, boolean wide) {
        long key = ((long) radius << 1) | (wide ? 1L : 0L);
        List<int[]> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<int[]> bands = new ArrayList<>(DEFAULT_STEPS);
        for (int step = 0; step < DEFAULT_STEPS; step++) {
            double dy0 = radius * (double) step / DEFAULT_STEPS;
            double dy1 = radius * (double) (step + 1) / DEFAULT_STEPS;
            double inset = radius - Math.sqrt(Math.max(0.0, radius * (double) radius - (radius - dy0) * (radius - dy0)));
            int insetX = (int) Math.round(inset);
            int insetTop = (int) Math.round(dy0);
            int insetBottom = (int) Math.round(radius - dy1);
            bands.add(new int[]{insetX, insetTop, insetBottom});
        }
        CACHE.put(key, bands);
        return bands;
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

        float clamped = Math.min(radius, Math.min(width, height) * 0.5F);
        if (clamped <= 0.75F) {
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

        List<int[]> bands = bands(r, false);
        for (int[] band : bands) {
            graphics.fill(left + band[0], top + band[1], left + band[0] + t, top + band[1] + t, argb);
            graphics.fill(right - band[0] - t, top + band[1], right - band[0], top + band[1] + t, argb);
            graphics.fill(left + band[0], bottom - band[1] - t, left + band[0] + t, bottom - band[1], argb);
            graphics.fill(right - band[0] - t, bottom - band[1] - t, right - band[0], bottom - band[1], argb);
        }
    }
}
