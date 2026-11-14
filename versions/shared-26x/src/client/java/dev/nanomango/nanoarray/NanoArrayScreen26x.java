package dev.nanomango.nanoarray;

import dev.nanomango.nanoarray.backend.GuiGraphicsExtractorAdapter;
import dev.nanomango.nanoarray.backend.NanoArrayGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Screen implementation for the Minecraft 26.x line.
 *
 * <p>26.x records the drawing commands instead of drawing them immediately,
 * which happens in {@link #extractRenderState}.</p>
 */
public final class NanoArrayScreen26x extends AbstractNanoArrayScreen {

    public NanoArrayScreen26x(NanoArrayCore core) {
        super(core, Component.literal("NanoArray"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderGui(new GuiGraphicsExtractorAdapter(graphics), mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderGui(NanoArrayGraphics graphics, int mouseX, int mouseY, float partialTick) {
        core.render(graphics, width, height, partialTick);
    }
}
