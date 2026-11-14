package dev.nanomango.nanoarray.backend;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Adapts the Minecraft 1.21.x graphics object to {@link NanoArrayGraphics}.
 */
public final class GuiGraphicsAdapter implements NanoArrayGraphics {

    private final GuiGraphics graphics;

    public GuiGraphicsAdapter(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void fill(int x1, int y1, int x2, int y2, int argb) {
        graphics.fill(x1, y1, x2, y2, argb);
    }

    @Override
    public void fillGradient(int x1, int y1, int x2, int y2, int topArgb, int bottomArgb) {
        graphics.fillGradient(x1, y1, x2, y2, topArgb, bottomArgb);
    }

    @Override
    public void drawString(Font font, String text, int x, int y, int argb) {
        graphics.drawString(font, text, x, y, argb);
    }

    @Override
    public void enableScissor(int x1, int y1, int x2, int y2) {
        graphics.enableScissor(x1, y1, x2, y2);
    }

    @Override
    public void disableScissor() {
        graphics.disableScissor();
    }
}
