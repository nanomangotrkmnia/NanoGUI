package dev.nanomango.nanogui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Version specific screen management for Minecraft 1.21.11.
 */
public final class NanoGUIPlatform12111 implements NanoGUIPlatform {

    @Override
    public Screen createScreen() {
        return new NanoGUIScreen1211(NanoGUICore.get());
    }

    @Override
    public void openScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    public void closeScreen() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean supportsVectorRendering() {
        return true;
    }
}
