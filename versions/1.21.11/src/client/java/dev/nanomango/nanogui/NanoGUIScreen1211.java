package dev.nanomango.nanogui;

import dev.nanomango.nanogui.backend.GuiGraphicsAdapter;
import dev.nanomango.nanogui.backend.NanoGUIGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Screen implementation for Minecraft 1.21.11.
 *
 * <p>1.21.11 still draws the GUI immediately inside {@link #render}.</p>
 */
public final class NanoGUIScreen1211 extends AbstractNanoGUIScreen {

    public NanoGUIScreen1211(NanoGUICore core) {
        super(core, Component.literal("NanoGUI"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderGui(new GuiGraphicsAdapter(graphics), mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderGui(NanoGUIGraphics graphics, int mouseX, int mouseY, float partialTick) {
        core.render(graphics, width, height, partialTick);
    }
}
