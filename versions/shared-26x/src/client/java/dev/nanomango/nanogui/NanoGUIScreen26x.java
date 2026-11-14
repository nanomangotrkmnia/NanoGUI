package dev.nanomango.nanogui;

import dev.nanomango.nanogui.backend.GuiGraphicsExtractorAdapter;
import dev.nanomango.nanogui.backend.NanoGUIGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Screen implementation for the Minecraft 26.x line.
 *
 * <p>26.x records the drawing commands instead of drawing them immediately,
 * which happens in {@link #extractRenderState}.</p>
 */
public final class NanoGUIScreen26x extends AbstractNanoGUIScreen {

    public NanoGUIScreen26x(NanoGUICore core) {
        super(core, Component.literal("NanoGUI"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderGui(new GuiGraphicsExtractorAdapter(graphics), mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderGui(NanoGUIGraphics graphics, int mouseX, int mouseY, float partialTick) {
        core.render(graphics, width, height, partialTick);
    }
}
