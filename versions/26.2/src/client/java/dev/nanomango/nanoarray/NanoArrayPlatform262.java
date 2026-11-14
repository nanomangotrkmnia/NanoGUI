package dev.nanomango.nanoarray;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Version specific screen management for Minecraft 26.2.
 */
public final class NanoArrayPlatform262 implements NanoArrayPlatform {

    @Override
    public Screen createScreen() {
        return new NanoArrayScreen26x(NanoArrayCore.get());
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
