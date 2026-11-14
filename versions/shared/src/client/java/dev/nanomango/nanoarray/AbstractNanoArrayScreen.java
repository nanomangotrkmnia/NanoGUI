package dev.nanomango.nanoarray;

import dev.nanomango.nanoarray.backend.NanoArrayGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Base class of the screen that hosts the GUI.
 *
 * <p>The input handling is identical on every supported Minecraft version
 * because 1.21.11 already uses the event based input API.</p>
 */
public abstract class AbstractNanoArrayScreen extends Screen {

    protected final NanoArrayCore core;

    protected AbstractNanoArrayScreen(NanoArrayCore core, Component title) {
        super(title);
        this.core = core;
    }

    /**
     * Draws the GUI. Implemented by the version specific subclasses because
     * Minecraft 1.21.11 and 26.x use different rendering calls.
     */
    protected abstract void renderGui(NanoArrayGraphics graphics, int mouseX, int mouseY, float partialTick);

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // The GUI is closed through the keybind so that the state stays
        // consistent, but escape still closes the screen.
        return true;
    }

    @Override
    public void onClose() {
        core.gui().close();
        super.onClose();
    }

    @Override
    public void removed() {
        core.save();
        super.removed();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (core.gui().isOpen()) {
            if (core.gui().keyPressed(event.key(), event.modifiers())) {
                return true;
            }
            // Every key press is consumed so that nothing leaks into the
            // game while the GUI is open.
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (core.gui().isOpen()) {
            core.gui().keyReleased(event.key(), event.modifiers());
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (core.gui().isOpen()) {
            return core.gui().charTyped(event.codepoint()) || true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (core.gui().isOpen()) {
            core.gui().mouseClicked(event.x(), event.y(), event.button());
            // Clicks are always consumed so that they cannot reach the world.
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (core.gui().isOpen()) {
            core.gui().mouseReleased(event.x(), event.y(), event.button());
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (core.gui().isOpen()) {
            return core.gui().mouseDragged(event.x(), event.y(), event.button(), dragX, dragY) || true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (core.gui().isOpen()) {
            return core.gui().mouseScrolled(mouseX, mouseY, scrollX, scrollY) || true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (core.gui().isOpen()) {
            core.mouseMoved(mouseX, mouseY);
        }
        super.mouseMoved(mouseX, mouseY);
    }
}
