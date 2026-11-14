package dev.nanomango.gui.render.imgui;

/**
 * The subset of a Dear ImGui {@code ImDrawList} that the framework needs.
 *
 * <p>Dear ImGui is an immediate mode library: a frame is built by calling
 * drawing functions that append vertices to a draw list. This interface is the
 * boundary between the framework and the native binding, so the translation
 * from the framework's {@link dev.nanomango.gui.render.DrawList} can be unit
 * tested without the native library.</p>
 *
 * <p>All coordinates are in GUI units and colours are packed
 * {@code 0xAARRGGBB} values. Implementations convert them to the conventions
 * of Dear ImGui.</p>
 *
 * <p>Clipping follows the ImGui model: a clip rectangle is only allowed to
 * shrink while it is pushed, so a nested push intersects with its parent.</p>
 */
public interface ImGuiDrawList {

    /**
     * Adds a filled rectangle.
     *
     * @param rounding corner radius in GUI units
     */
    void addRectFilled(float x0, float y0, float x1, float y1, int argb, float rounding);

    /**
     * Adds the outline of a rectangle.
     *
     * @param rounding  corner radius in GUI units
     * @param thickness width of the outline in GUI units
     */
    void addRect(float x0, float y0, float x1, float y1, int argb, float rounding, float thickness);

    /**
     * Adds a rectangle with a colour per corner.
     *
     * <p>The corners are ordered top left, top right, bottom right and bottom
     * left, which is the order Dear ImGui uses for its multi colour
     * rectangle.</p>
     */
    void addRectFilledMultiColor(float x0, float y0, float x1, float y1,
                                 int topLeft, int topRight, int bottomRight, int bottomLeft);

    /**
     * Adds a run of text.
     *
     * @param fontId id of the font, resolved by the binding
     * @param size   font size in GUI units
     * @param x      left edge of the run
     * @param y      top of the line box
     */
    void addText(int fontId, float size, float x, float y, int argb, String text);

    /**
     * Adds a textured quad.
     *
     * <p>The texture coordinates are normalised, {@code (0, 0)} is the top
     * left and {@code (1, 1)} the bottom right corner of the image.</p>
     */
    void addImage(int textureId, float x0, float y0, float x1, float y1,
                  float u0, float v0, float u1, float v1, int argb);

    /**
     * Adds a line.
     */
    void addLine(float x0, float y0, float x1, float y1, int argb, float thickness);

    /**
     * Adds a filled triangle.
     */
    void addTriangleFilled(float x0, float y0, float x1, float y1, float x2, float y2, int argb);

    /**
     * Adds a filled ring slice.
     *
     * @param outerRadius outer radius in GUI units
     * @param innerRadius inner radius in GUI units, {@code 0} for a pie slice
     * @param startAngle  start angle in radians
     * @param endAngle    end angle in radians
     */
    void addArcFilled(float centerX, float centerY, float outerRadius, float innerRadius,
                      float startAngle, float endAngle, int argb);

    /**
     * Pushes a clipping rectangle.
     *
     * @param intersect {@code true} when the rectangle shrinks the current
     *                  clip region, {@code false} when it replaces it
     */
    void pushClipRect(float x0, float y0, float x1, float y1, boolean intersect);

    /** Pops the most recently pushed clipping rectangle. */
    void popClipRect();
}
