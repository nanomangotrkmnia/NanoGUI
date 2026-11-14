package dev.nanomango.nanoarray.backend;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

/**
 * Thin abstraction over Minecraft's GUI drawing primitives.
 *
 * <p>Minecraft 1.21.11 draws with {@code GuiGraphics} while 26.x records the
 * drawing commands with {@code GuiGraphicsExtractor}. Both expose the same
 * operations, so the version modules only have to provide a small adapter
 * that implements this interface.</p>
 */
public interface NanoArrayGraphics {

    /**
     * Fills an axis aligned rectangle.
     */
    void fill(int x1, int y1, int x2, int y2, int argb);

    /**
     * Fills an axis aligned rectangle with a vertical gradient.
     */
    void fillGradient(int x1, int y1, int x2, int y2, int topArgb, int bottomArgb);

    /**
     * Draws a text run with the Minecraft font.
     */
    void drawString(Font font, String text, int x, int y, int argb);

    /**
     * Draws a textured rectangle with a colour tint.
     *
     * <p>The source rectangle is addressed in pixels of the source texture,
     * which is the convention that Minecraft's own {@code blit} calls use.</p>
     *
     * @param texture       the source texture
     * @param u             x offset of the source rectangle in the texture
     * @param v             y offset of the source rectangle in the texture
     * @param x             destination x position
     * @param y             destination y position
     * @param width         destination width
     * @param height        destination height
     * @param textureWidth  width of the source texture in pixels
     * @param textureHeight height of the source texture in pixels
     * @param argb          colour tint
     */
    void blit(Identifier texture, float u, float v, int x, int y, int width, int height,
              int textureWidth, int textureHeight, int argb);

    /**
     * Pushes a scissor rectangle. Calls have to be balanced with
     * {@link #disableScissor()}.
     */
    void enableScissor(int x1, int y1, int x2, int y2);

    /**
     * Pops the innermost scissor rectangle.
     */
    void disableScissor();
}
