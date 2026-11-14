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
        if ((modifiers & Modifiers.SHIFT) != 0) {
            builder.append("Shift + ");
        }
        if ((modifiers & Modifiers.CONTROL) != 0) {
            builder.append("Ctrl + ");
        }
        if ((modifiers & Modifiers.ALT) != 0) {
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
