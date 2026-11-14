package dev.nanomango.nanogui.backend;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Adapts the Minecraft 1.21.x graphics object to {@link NanoGUIGraphics}.
 */
public final class GuiGraphicsAdapter implements NanoGUIGraphics {

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
    public void blit(Identifier texture, float u, float v, int x, int y, int width, int height,
                     int textureWidth, int textureHeight, int argb) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v,
                width, height, textureWidth, textureHeight, argb);
    }

    @Override
    public void blit(Identifier texture, float u, float v, int x, int y, int width, int height,
                     int sourceWidth, int sourceHeight, int textureWidth, int textureHeight, int argb) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v,
                width, height, sourceWidth, sourceHeight, textureWidth, textureHeight, argb);
    }

    @Override
    public void pushTransform(float x, float y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
    }

    @Override
    public void popTransform() {
        graphics.pose().popMatrix();
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
