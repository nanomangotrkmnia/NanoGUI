package dev.nanomango.nanoarray;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Version specific screen management for Minecraft 1.21.11.
 */
public final class NanoArrayPlatform12111 implements NanoArrayPlatform {

    @Override
    public Screen createScreen() {
        return new NanoArrayScreen1211(NanoArrayCore.get());
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
