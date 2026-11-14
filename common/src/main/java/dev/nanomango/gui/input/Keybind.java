package dev.nanomango.gui.input;

import java.util.Objects;

/**
 * A configurable keyboard shortcut.
 *
 * <p>Keybinds are stored as a GLFW style key code plus a modifier mask so
 * that they can be persisted without any platform specific data.</p>
 */
public final class Keybind {

    private final String name;
    private final String category;
    private int keyCode;
    private int modifiers;

    public Keybind(String name, int keyCode) {
        this(name, "NanoArray", keyCode, 0);
    }

    public Keybind(String name, String category, int keyCode) {
        this(name, category, keyCode, 0);
    }

    public Keybind(String name, String category, int keyCode, int modifiers) {
        this.name = Objects.requireNonNull(name, "name");
        this.category = category == null ? "NanoArray" : category;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public int keyCode() {
        return keyCode;
    }

    public int modifiers() {
        return modifiers;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Serialised representation used by the configuration file.
     */
    public String serialize() {
        return keyCode + ":" + modifiers;
    }

    /**
     * Restores a keybind from its serialised representation.
     *
     * @throws IllegalArgumentException when the value cannot be parsed
     */
    public void deserialize(String value) {
        int separator = value.indexOf(':');
        if (separator < 0) {
            throw new IllegalArgumentException("Invalid keybind value: " + value);
        }
        try {
            this.keyCode = Integer.parseInt(value.substring(0, separator));
            this.modifiers = Integer.parseInt(value.substring(separator + 1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid keybind value: " + value, exception);
        }
    }

    /**
     * Human readable representation such as {@code "Right Shift"}.
     */
    public String displayName() {
        if (keyCode == Keys.UNKNOWN) {
            return "Unbound";
        }
        StringBuilder builder = new StringBuilder();
        // A modifier key is the key itself, not a modifier of itself. Old
        // configuration files may still contain the redundant bit.
        boolean shift = (modifiers & Modifiers.SHIFT) != 0
                && keyCode != Keys.LEFT_SHIFT && keyCode != Keys.RIGHT_SHIFT;
        boolean control = (modifiers & Modifiers.CONTROL) != 0
                && keyCode != Keys.LEFT_CONTROL && keyCode != Keys.RIGHT_CONTROL;
        boolean alt = (modifiers & Modifiers.ALT) != 0
                && keyCode != Keys.LEFT_ALT && keyCode != Keys.RIGHT_ALT;
        if (shift) {
            builder.append("Shift + ");
        }
        if (control) {
            builder.append("Ctrl + ");
        }
        if (alt) {
            builder.append("Alt + ");
        }
        builder.append(Keys.name(keyCode));
        return builder.toString();
    }

    @Override
    public String toString() {
        return "Keybind[" + name + " = " + displayName() + "]";
    }
}
