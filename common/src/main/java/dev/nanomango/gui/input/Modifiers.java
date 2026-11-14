package dev.nanomango.gui.input;

/**
 * Bit masks for the modifier keys used by keybinds.
 */
public final class Modifiers {

    public static final int NONE = 0;
    public static final int SHIFT = 1;
    public static final int CONTROL = 1 << 1;
    public static final int ALT = 1 << 2;
    public static final int SUPER = 1 << 3;

    private Modifiers() {
    }

    /**
     * Converts a modifier mask into the GLFW style bit field.
     *
     * @param shift  whether shift is held
     * @param control whether control is held
     * @param alt    whether alt is held
     * @param superKey whether the super key is held
     * @return GLFW style modifier mask
     */
    public static int glfw(boolean shift, boolean control, boolean alt, boolean superKey) {
        int mask = NONE;
        if (shift) {
            mask |= 0x0001;
        }
        if (control) {
            mask |= 0x0002;
        }
        if (alt) {
            mask |= 0x0004;
        }
        if (superKey) {
            mask |= 0x0008;
        }
        return mask;
    }

    public static int fromShift(boolean shift, boolean control, boolean alt) {
        int mask = NONE;
        if (shift) {
            mask |= SHIFT;
        }
        if (control) {
            mask |= CONTROL;
        }
        if (alt) {
            mask |= ALT;
        }
        return mask;
    }

    public static boolean hasShift(int mask) {
        return (mask & SHIFT) != 0;
    }

    public static boolean hasControl(int mask) {
        return (mask & CONTROL) != 0;
    }

    public static boolean hasAlt(int mask) {
        return (mask & ALT) != 0;
    }
}
