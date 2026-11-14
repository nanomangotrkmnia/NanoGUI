package dev.nanomango.gui.render;

/**
 * Anti-aliased corner sprites for rounded rectangles.
 *
 * <p>The sprites are generated from the exact geometry of a disc. Every pixel
 * is sampled on a small grid and the fraction of the samples that lie inside
 * the disc becomes the alpha channel of the pixel. The result is a smooth
 * corner that can be blitted without scaling, so it stays sharp on every
 * display and at every GUI scale.</p>
 *
 * <p>Four orientations are stored in a single texture, side by side: top
 * left, top right, bottom left and bottom right. The other three are
 * mirrored copies of the top left corner, which keeps the generation cost
 * independent of the orientation.</p>
 *
 * <p>The class contains no Minecraft type so that the geometry can be unit
 * tested without a running game.</p>
 */
public final class CornerSprites {

    /** Number of stored orientations. */
    public static final int ORIENTATIONS = 4;

    /** Index of the top left corner. */
    public static final int TOP_LEFT = 0;

    /** Index of the top right corner. */
    public static final int TOP_RIGHT = 1;

    /** Index of the bottom left corner. */
    public static final int BOTTOM_LEFT = 2;

    /** Index of the bottom right corner. */
    public static final int BOTTOM_RIGHT = 3;

    /** Number of samples that are taken per pixel and per axis. */
    private static final int SUPERSAMPLE = 4;

    private CornerSprites() {
    }

    /**
     * Width of the texture that holds the four corners.
     */
    public static int textureWidth(int radius) {
        return Math.max(1, radius) * ORIENTATIONS;
    }

    /**
     * Height of the texture that holds the four corners.
     */
    public static int textureHeight(int radius) {
        return Math.max(1, radius);
    }

    /**
     * Generates the corners of a filled rounded rectangle.
     *
     * @param radius the corner radius in pixels
     * @return the pixels of the texture in ARGB order, row by row
     */
    public static int[] generateFill(int radius) {
        return generate(radius, radius);
    }

    /**
     * Generates the corners of the outline of a rounded rectangle.
     *
     * <p>The outline follows the outer edge of the corner and its inner edge
     * is the arc of the rounded rectangle that is inset by the thickness of
     * the outline.</p>
     *
     * @param radius    the corner radius in pixels
     * @param thickness the width of the outline in pixels
     * @return the pixels of the texture in ARGB order, row by row
     */
    public static int[] generateStroke(int radius, int thickness) {
        int r = Math.max(1, radius);
        int t = Math.max(1, Math.min(thickness, r));
        return generate(r, t);
    }

    /**
     * Generates the four corners.
     *
     * @param radius     the corner radius in pixels
     * @param innerInset how far the inner edge of the corner is away from the
     *                   outer edge. A value of {@code radius} produces a
     *                   filled corner, a value of {@code 0} an empty one.
     * @return the pixels of the texture in ARGB order, row by row
     */
    private static int[] generate(int radius, int innerInset) {
        int r = Math.max(1, radius);
        int width = textureWidth(r);
        int height = textureHeight(r);
        float innerRadius = r - innerInset;

        // The top left corner, the other orientations are mirrored copies.
        int[] base = new int[r * r];
        for (int y = 0; y < r; y++) {
            for (int x = 0; x < r; x++) {
                int alpha = Math.round(coverage(x, y, r, innerRadius) * 255.0F);
                base[y * r + x] = 0x00FFFFFF | (alpha << 24);
            }
        }

        int[] pixels = new int[width * height];
        for (int orientation = 0; orientation < ORIENTATIONS; orientation++) {
            boolean flipX = (orientation & 1) != 0;
            boolean flipY = (orientation & 2) != 0;
            int offset = orientation * r;
            for (int y = 0; y < r; y++) {
                int sourceY = flipY ? r - 1 - y : y;
                for (int x = 0; x < r; x++) {
                    int sourceX = flipX ? r - 1 - x : x;
                    pixels[y * width + offset + x] = base[sourceY * r + sourceX];
                }
            }
        }
        return pixels;
    }

    /**
     * Coverage of a single pixel.
     *
     * <p>The pixel is sampled on a {@value #SUPERSAMPLE} by
     * {@value #SUPERSAMPLE} grid. A sample counts as covered when it lies
     * between the inner and the outer radius of the corner.</p>
     *
     * @param x           the column of the pixel inside the corner cell
     * @param y           the row of the pixel inside the corner cell
     * @param radius      the outer radius of the corner
     * @param innerRadius the inner radius of the corner
     * @return the coverage in the range {@code [0, 1]}
     */
    private static float coverage(int x, int y, int radius, float innerRadius) {
        int hits = 0;
        for (int sampleY = 0; sampleY < SUPERSAMPLE; sampleY++) {
            float py = y + (sampleY + 0.5F) / SUPERSAMPLE;
            for (int sampleX = 0; sampleX < SUPERSAMPLE; sampleX++) {
                float px = x + (sampleX + 0.5F) / SUPERSAMPLE;
                float dx = px - radius;
                float dy = py - radius;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= radius && distance >= innerRadius) {
                    hits++;
                }
            }
        }
        return hits / (float) (SUPERSAMPLE * SUPERSAMPLE);
    }
}
