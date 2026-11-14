package dev.nanomango.nanoarray;

import dev.nanomango.nanoarray.backend.GuiGraphicsAdapter;
import dev.nanomango.nanoarray.backend.NanoArrayGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Screen implementation for Minecraft 1.21.11.
 *
 * <p>1.21.11 still draws the GUI immediately inside {@link #render}.</p>
 */
public final class NanoArrayScreen1211 extends AbstractNanoArrayScreen {

    public NanoArrayScreen1211(NanoArrayCore core) {
        super(core, Component.literal("NanoArray"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderGui(new GuiGraphicsAdapter(graphics), mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderGui(NanoArrayGraphics graphics, int mouseX, int mouseY, float partialTick) {
        core.render(graphics, width, height, partialTick);
    }
}
