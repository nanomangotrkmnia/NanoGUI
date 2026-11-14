package dev.nanomango.nanoarray;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Registers the keybinds of the framework.
 *
 * <p>The key codes are the GLFW key codes, duplicated here so that the code
 * also compiles against Minecraft versions that replaced GLFW with SDL.</p>
 */
public final class NanoArrayKeybinds {

    /** GLFW key code of the right shift key. */
    public static final int GLFW_KEY_RIGHT_SHIFT = 344;

    /** GLFW key code of the left shift key. */
    public static final int GLFW_KEY_LEFT_SHIFT = 340;

    /** GLFW key code of the F3 key, used for the developer overlay. */
    public static final int GLFW_KEY_F3 = 292;

    private static final String CATEGORY = "nanoarray";

    private static KeyMapping openKey;
    private static KeyMapping debugKey;
    private static dev.nanomango.gui.input.Keybind openBind;

    private NanoArrayKeybinds() {
    }

    /**
     * Creates the keybinds. Calling this more than once is safe.
     */
    public static synchronized void register() {
        if (openKey != null) {
            return;
        }
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CATEGORY, "general"));
        // The three argument constructor always uses the keyboard input type,
        // which keeps the code working on the Minecraft versions that renamed
        // the constant.
        openKey = new KeyMapping("key.nanoarray.open", GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC);
        openBind = new dev.nanomango.gui.input.Keybind("Open GUI", "NanoArray", GLFW_KEY_RIGHT_SHIFT);
        debugKey = new KeyMapping("key.nanoarray.debug", GLFW_KEY_F3 + 256, KeyMapping.Category.MISC);
    }

    /**
     * The keybind that opens and closes the GUI.
     */
    public static KeyMapping openKey() {
        return openKey;
    }

    /**
     * The keybind that toggles the developer overlay.
     */
    public static KeyMapping debugKey() {
        return debugKey;
    }

    /**
     * Framework side representation of the open keybind.
     */
    public static dev.nanomango.gui.input.Keybind openBind() {
        return openBind;
    }
}
