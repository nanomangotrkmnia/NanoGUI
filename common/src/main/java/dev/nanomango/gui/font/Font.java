package dev.nanomango.gui.font;

/**
 * A font that the GUI can measure and draw with.
 *
 * <p>Implementations are free to use any underlying technology: the Minecraft
 * font atlas, a Java2D rasteriser or the bundled native renderer. The only
 * requirement is that {@link #draw(TextSink, String, float, float, int)}
 * produces a visually correct result through the supplied sink.</p>
 */
public interface Font {

    /**
     * Stable identifier of this font. Used by the render backends to look up
     * the matching host side font object.
     */
    int id();

    /** Human readable name, mostly used for diagnostics and the font picker. */
    String name();

    /** Total line height in pixels. */
    float height();

    /** Distance from the top of the line to the baseline. */
    float ascent();

    /**
     * Distance from the baseline to the bottom of the line.
     *
     * <p>The value is positive. The default implementation derives it from the
     * total line height and the ascent.</p>
     */
    default float descent() {
        return Math.max(0.0F, height() - ascent());
    }

    /** Width of the given text in pixels. */
    float width(String text);

    /** Width of a slice of the given text in pixels. */
    float width(CharSequence text, int start, int end);

    /** True when the font can render characters outside of the basic latin range. */
    default boolean supportsUnicode() {
        return false;
    }

    /**
     * Draws a run of text.
     *
     * <p>The {@code (x, y)} origin is the top left corner of the line box,
     * which is the same origin that the layout engine and the components use.
     * The baseline therefore sits at {@code y + ascent()}.</p>
     *
     * @param sink  backend specific drawing sink
     * @param text  the text to draw, never {@code null}
     * @param x     x position of the text origin
     * @param y     y position of the top of the line
     * @param argb  packed colour
     */
    void draw(TextSink sink, String text, float x, float y, int argb);

    /**
     * Optional GPU side representation of this font.
     *
     * <p>Only rasterising fonts (the native renderer and the Java2D fallback)
     * expose a texture. Host fonts such as the Minecraft font are drawn by the
     * backend itself and return {@code null}.</p>
     */
    default FontTexture texture() {
        return null;
    }
}
