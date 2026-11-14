package dev.nanomango.nanoarray;

import net.minecraft.client.gui.screens.Screen;

/**
 * Version specific operations that the shared code needs.
 *
 * <p>Minecraft 26.2 moved the screen handling from {@code Minecraft} into
 * {@code Minecraft.gui}, which is why the screen management is abstracted
 * here.</p>
 */
public interface NanoArrayPlatform {

    /**
     * Creates the screen that hosts the GUI.
     */
    Screen createScreen();

    /**
     * Shows the given screen.
     */
    void openScreen(Screen screen);

    /**
     * Closes the currently open screen.
     */
    void closeScreen();

    /**
     * True when the game runs on an OpenGL context. The optional vector
     * renderer requires OpenGL.
     */
    boolean supportsVectorRendering();
}
