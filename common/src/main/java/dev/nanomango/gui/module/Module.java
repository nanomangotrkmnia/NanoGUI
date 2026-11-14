package dev.nanomango.gui.module;

import dev.nanomango.gui.component.Component;

/**
 * A feature that can be enabled, disabled and configured from the GUI.
 */
public interface Module {

    /**
     * Stable identifier, used for persistence.
     */
    String id();

    /**
     * Display name.
     */
    String name();

    /**
     * Short description shown in the module list.
     */
    default String description() {
        return "";
    }

    /**
     * Whether the module is currently active.
     */
    boolean isEnabled();

    /**
     * Enables or disables the module.
     */
    void setEnabled(boolean enabled);

    /**
     * Builds the settings panel of the module.
     */
    Component createPanel();
}
