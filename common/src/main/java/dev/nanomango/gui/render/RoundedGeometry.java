package dev.nanomango.gui.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Geometry of rounded rectangles for backends that can only draw axis aligned
 * rectangles.
 *
 * <p>Minecraft's GUI API has no rounded rectangle primitive, so the corners
 * are approximated with a small staircase. The geometry of a corner only
 * depends on the radius, therefore it is computed once and cached.</p>
 *
 * <p>The class is part of the Minecraft independent framework so that the
 * geometry can be unit tested without a running game.</p>
 */
public final class RoundedGeometry {

    /** Number of steps that are used to approximate a single corner. */
    public static final int DEFAULT_STEPS = 3;

    private static final Map<Long, List<int[]>> CACHE = new HashMap<>();

    private RoundedGeometry() {
    }

    /**
     * Computes the corner staircase of a rounded rectangle.
     *
     * <p>Every entry of the returned list is a triple of
     * {@code [insetX, insetTop, insetBottom]}: the horizontal inset of the
     * band and its vertical offset from the top and the bottom edge.</p>
     *
     * @param radius the corner radius in pixels
     * @param wide   {@code true} when the rectangle is much wider than it is
     *               high, which allows a coarser approximation
     * @return the bands of the staircase, never {@code null}
     */
    public static List<int[]> bands(int radius, boolean wide) {
        int clamped = Math.max(1, radius);
        long key = ((long) clamped << 1) | (wide ? 1L : 0L);
        List<int[]> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        List<int[]> bands = new ArrayList<>(DEFAULT_STEPS);
        for (int step = 0; step < DEFAULT_STEPS; step++) {
            double dy0 = clamped * (double) step / DEFAULT_STEPS;
            double dy1 = clamped * (double) (step + 1) / DEFAULT_STEPS;
            double inset = clamped
                    - Math.sqrt(Math.max(0.0, clamped * (double) clamped - (clamped - dy0) * (clamped - dy0)));
            int insetX = (int) Math.round(inset);
            int insetTop = (int) Math.round(dy0);
            int insetBottom = (int) Math.round(clamped - dy1);
            bands.add(new int[]{insetX, insetTop, insetBottom});
        }
        CACHE.put(key, bands);
        return bands;
    }

    /**
     * Radius that a rounded rectangle of the given size can use at most.
     *
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param radius the requested radius
     * @return the radius that is safe to use
     */
    public static float clampRadius(float width, float height, float radius) {
        if (width <= 0.0F || height <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(radius, Math.min(width, height) * 0.5F));
    }

    /**
     * True when the radius is small enough for a plain rectangle to be an
     * acceptable approximation.
     */
    public static boolean isSquare(float radius) {
        return radius <= 0.75F;
    }
}
