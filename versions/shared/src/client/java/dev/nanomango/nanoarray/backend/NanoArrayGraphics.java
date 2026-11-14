package dev.nanomango.nanoarray.backend;

import net.minecraft.client.gui.Font;

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
     * Pushes a scissor rectangle. Calls have to be balanced with
     * {@link #disableScissor()}.
     */
    void enableScissor(int x1, int y1, int x2, int y2);

    /**
     * Pops the innermost scissor rectangle.
     */
    void disableScissor();
}
