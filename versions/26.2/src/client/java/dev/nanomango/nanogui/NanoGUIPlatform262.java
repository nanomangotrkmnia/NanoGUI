package dev.nanomango.nanogui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Version specific screen management for Minecraft 26.2.
 */
public final class NanoGUIPlatform262 implements NanoGUIPlatform {

    @Override
    public Screen createScreen() {
        return new NanoGUIScreen26x(NanoGUICore.get());
    }

    @Override
    public void openScreen(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }

    @Override
    public void closeScreen() {
        Minecraft.getInstance().gui.setScreen(null);
    }

    @Override
    public boolean supportsVectorRendering() {
        return true;
    }
}
