package dev.nanomango.nanogui;

import dev.nanomango.nanogui.backend.NanoGUIGraphics;
import net.minecraft.client.Minecraft;
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
public abstract class AbstractNanoGUIScreen extends Screen {

    protected final NanoGUICore core;

    protected AbstractNanoGUIScreen(NanoGUICore core, Component title) {
        super(title);
        this.core = core;
    }

    /**
     * Draws the GUI. Implemented by the version specific subclasses because
     * Minecraft 1.21.11 and 26.x use different rendering calls.
     */
    protected abstract void renderGui(NanoGUIGraphics graphics, int mouseX, int mouseY, float partialTick);

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
            // A widget that is listening for a new keybind gets the key first,
            // so rebinding Right Shift still works.
            if (core.gui().keyPressed(event.key(), event.modifiers())) {
                return true;
            }
            // Minecraft routes key presses to the open screen and not to the
            // key mapping, so the toggle key has to close the GUI here.
            if (event.key() == NanoGUIKeybinds.GLFW_KEY_RIGHT_SHIFT) {
                core.toggle();
                return true;
            }
            // Every other key press is consumed so that nothing leaks into the
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
            core.gui().mouseClicked(cursorX(event), cursorY(event), event.button());
            // Clicks are always consumed so that they cannot reach the world.
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (core.gui().isOpen()) {
            core.gui().mouseReleased(cursorX(event), cursorY(event), event.button());
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (core.gui().isOpen()) {
            return core.gui().mouseDragged(cursorX(event), cursorY(event), event.button(), dragX, dragY) || true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (core.gui().isOpen()) {
            return core.gui().mouseScrolled(cursorX(mouseX), cursorY(mouseY), scrollX, scrollY) || true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    /**
     * Current cursor position in GUI units.
     *
     * <p>The event coordinates are not guaranteed to be in the same space as
     * the coordinates used to lay out the frame, especially on Retina
     * displays. Reading the scaled position from the mouse handler keeps hit
     * testing aligned with what was drawn.</p>
     */
    private static double cursorX(MouseButtonEvent event) {
        return cursorX(event.x());
    }

    private static double cursorY(MouseButtonEvent event) {
        return cursorY(event.y());
    }

    private static double cursorX(double fallback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.mouseHandler != null && minecraft.getWindow() != null) {
            return minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
        }
        return fallback;
    }

    private static double cursorY(double fallback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.mouseHandler != null && minecraft.getWindow() != null) {
            return minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
        }
        return fallback;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (core.gui().isOpen()) {
            core.mouseMoved(mouseX, mouseY);
        }
        super.mouseMoved(mouseX, mouseY);
    }
}
