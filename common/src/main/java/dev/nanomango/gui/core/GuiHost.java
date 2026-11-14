package dev.nanomango.gui.core;

/**
 * Callback interface used by the {@link Gui} to ask the host to show or hide
 * the GUI screen.
 */
public interface GuiHost {

    /**
     * The host should open the GUI screen.
     */
    void openGui();

    /**
     * The host should close the GUI screen.
     */
    void closeGui();
}
