package dev.nanomango.gui.render.nvg;

/**
 * The small subset of NanoVG that the framework needs.
 *
 * <p>NanoVG is a vector renderer: instead of replaying a long list of
 * rectangles it builds a path and fills or strokes it once, which produces
 * resolution independent, anti-aliased geometry. This interface keeps the
 * framework free of LWJGL types so that the translation from the draw list can
 * be unit tested without a graphics context.</p>
 *
 * <p>All coordinates are in GUI units. Colours are packed {@code 0xAARRGGBB}
 * values, the same representation that the rest of the framework uses. The
 * implementation is responsible for converting both to the conventions of the
 * underlying renderer.</p>
 *
 * <p>The contract of the individual methods mirrors the NanoVG functions of
 * the same name. Implementations do not have to be thread safe: every method
 * is called from the render thread.</p>
 */
public interface NanoVG {

    /**
     * Starts a frame.
     *
     * @param width      width of the surface in GUI units
     * @param height     height of the surface in GUI units
     * @param pixelRatio ratio between physical pixels and GUI units, used for
     *                   high density displays
     */
    void beginFrame(float width, float height, float pixelRatio);

    /** Finishes the current frame and submits the recorded geometry. */
    void endFrame();

    /** Pushes the current state onto the NanoVG state stack. */
    void save();

    /** Pops the state that was pushed by {@link #save()}. */
    void restore();

    /**
     * Translates the coordinate system.
     *
     * @param x horizontal offset
     * @param y vertical offset
     */
    void translate(float x, float y);

    /**
     * Intersects the clipping region with the supplied rectangle.
     *
     * @param x      left edge
     * @param y      top edge
     * @param width  width of the region
     * @param height height of the region
     */
    void scissor(float x, float y, float width, float height);

    /** Clears the clipping region of the current state. */
    void resetScissor();

    /**
     * Fills an axis aligned rectangle.
     */
    void fill(float x, float y, float width, float height, int argb);

    /**
     * Fills a rounded rectangle.
     *
     * @param radius corner radius in GUI units
     */
    void fillRounded(float x, float y, float width, float height, float radius, int argb);

    /**
     * Fills a rounded rectangle with a vertical gradient.
     *
     * @param topArgb    colour at the top edge
     * @param bottomArgb colour at the bottom edge
     */
    void fillRoundedGradient(float x, float y, float width, float height, float radius,
                             int topArgb, int bottomArgb);

    /**
     * Strokes the outline of a rounded rectangle.
     *
     * @param thickness width of the outline in GUI units
     */
    void strokeRounded(float x, float y, float width, float height, float radius,
                       float thickness, int argb);

    /**
     * Draws a straight line.
     */
    void line(float x1, float y1, float x2, float y2, float thickness, int argb);

    /**
     * Fills a triangle.
     */
    void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb);

    /**
     * Fills a slice of a ring.
     *
     * @param centerX     centre of the ring
     * @param centerY     centre of the ring
     * @param outerRadius outer radius
     * @param innerRadius inner radius, {@code 0} for a full pie slice
     * @param startAngle  start angle in radians
     * @param endAngle    end angle in radians
     */
    void arc(float centerX, float centerY, float outerRadius, float innerRadius,
             float startAngle, float endAngle, int argb);

    /**
     * Draws a run of text.
     *
     * @param fontHandle NanoVG font handle
     * @param text       the text to draw
     * @param x          left edge of the run
     * @param baseline   y position of the baseline
     * @param size       font size in GUI units
     */
    void text(int fontHandle, String text, float x, float baseline, float size, int argb);

    /**
     * Draws a sub rectangle of an image.
     *
     * <p>The texture coordinates are normalised, {@code (0, 0)} is the top
     * left and {@code (1, 1)} the bottom right corner of the image.</p>
     *
     * @param imageHandle NanoVG image handle
     * @param x           left edge of the destination rectangle
     * @param y           top edge of the destination rectangle
     * @param width       width of the destination rectangle
     * @param height      height of the destination rectangle
     * @param u0          left texture coordinate
     * @param v0          top texture coordinate
     * @param u1          right texture coordinate
     * @param v1          bottom texture coordinate
     * @param argb        tint and alpha
     */
    void image(int imageHandle, float x, float y, float width, float height,
               float u0, float v0, float u1, float v1, int argb);
}
