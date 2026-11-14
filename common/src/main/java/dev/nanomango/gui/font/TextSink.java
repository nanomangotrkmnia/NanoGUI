package dev.nanomango.gui.font;

/**
 * Backend specific drawing sink used by {@link Font} implementations.
 *
 * <p>Two strategies are supported:</p>
 * <ul>
 *   <li>{@link #builtin(String, float, float, int)} hands a text run to the
 *       host renderer, for example the Minecraft font.</li>
 *   <li>{@link #glyph(int, float, float, float, float, float, float, float, float, int)}
 *       blits a single rasterised glyph from a font atlas.</li>
 * </ul>
 */
public interface TextSink {

    /**
     * Draws a text run using the host's built-in font.
     *
     * @param fontId id of the font as reported by {@link Font#id()}
     */
    void builtin(int fontId, String text, float x, float y, int argb);

    /**
     * Blits a single glyph from a font atlas.
     *
     * @param fontId  id of the font the glyph belongs to
     * @param textureId id of the atlas texture inside the backend
     * @param u0       texture coordinate of the left glyph edge
     * @param v0       texture coordinate of the top glyph edge
     * @param u1       texture coordinate of the right glyph edge
     * @param v1       texture coordinate of the bottom glyph edge
     * @param x        destination x of the glyph
     * @param y        destination y of the glyph
     * @param width    destination width of the glyph
     * @param height   destination height of the glyph
     * @param argb     packed colour
     */
    void glyph(int fontId, int textureId,
               float u0, float v0, float u1, float v1,
               float x, float y, float width, float height, int argb);
}
