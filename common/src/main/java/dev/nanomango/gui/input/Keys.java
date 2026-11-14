package dev.nanomango.gui.input;

/**
 * Key codes as used by GLFW (and therefore by Minecraft).
 *
 * <p>The values are duplicated here so that the shared framework stays free of
 * any Minecraft or LWJGL dependency. The version modules translate their native
 * key codes into these values.</p>
 */
public final class Keys {

    public static final int UNKNOWN = -1;

    public static final int SPACE = 32;
    public static final int APOSTROPHE = 39;
    public static final int COMMA = 44;
    public static final int MINUS = 45;
    public static final int PERIOD = 46;
    public static final int SLASH = 47;

    public static final int D0 = 48;
    public static final int D1 = 49;
    public static final int D2 = 50;
    public static final int D3 = 51;
    public static final int D4 = 52;
    public static final int D5 = 53;
    public static final int D6 = 54;
    public static final int D7 = 55;
    public static final int D8 = 56;
    public static final int D9 = 57;

    public static final int SEMICOLON = 59;
    public static final int EQUAL = 61;

    public static final int A = 65;
    public static final int B = 66;
    public static final int C = 67;
    public static final int D = 68;
    public static final int E = 69;
    public static final int F = 70;
    public static final int G = 71;
    public static final int H = 72;
    public static final int I = 73;
    public static final int J = 74;
    public static final int K = 75;
    public static final int L = 76;
    public static final int M = 77;
    public static final int N = 78;
    public static final int O = 79;
    public static final int P = 80;
    public static final int Q = 81;
    public static final int R = 82;
    public static final int S = 83;
    public static final int T = 84;
    public static final int U = 85;
    public static final int V = 86;
    public static final int W = 87;
    public static final int X = 88;
    public static final int Y = 89;
    public static final int Z = 90;

    public static final int LEFT_BRACKET = 91;
    public static final int BACKSLASH = 92;
    public static final int RIGHT_BRACKET = 93;
    public static final int GRAVE_ACCENT = 96;

    public static final int ESCAPE = 256;
    public static final int ENTER = 257;
    public static final int TAB = 258;
    public static final int BACKSPACE = 259;
    public static final int INSERT = 260;
    public static final int DELETE = 261;
    public static final int RIGHT = 262;
    public static final int LEFT = 263;
    public static final int DOWN = 264;
    public static final int UP = 265;
    public static final int PAGE_UP = 266;
    public static final int PAGE_DOWN = 267;
    public static final int HOME = 268;
    public static final int END = 269;
    public static final int CAPS_LOCK = 280;
    public static final int SCROLL_LOCK = 281;
    public static final int NUM_LOCK = 282;
    public static final int PRINT_SCREEN = 283;
    public static final int PAUSE = 284;
    public static final int F1 = 290;
    public static final int F2 = 291;
    public static final int F3 = 292;
    public static final int F4 = 293;
    public static final int F5 = 294;
    public static final int F6 = 295;
    public static final int F7 = 296;
    public static final int F8 = 297;
    public static final int F9 = 298;
    public static final int F10 = 299;
    public static final int F11 = 300;
    public static final int F12 = 301;

    public static final int LEFT_SHIFT = 340;
    public static final int LEFT_CONTROL = 341;
    public static final int LEFT_ALT = 342;
    public static final int LEFT_SUPER = 343;
    public static final int RIGHT_SHIFT = 344;
    public static final int RIGHT_CONTROL = 345;
    public static final int RIGHT_ALT = 346;
    public static final int RIGHT_SUPER = 347;
    public static final int MENU = 348;

    /** Highest key code defined by GLFW. */
    public static final int GLFW_LAST = 348;

    private static final String[] NAMES = new String[GLFW_LAST + 1];

    static {
        NAMES[SPACE] = "Space";
        NAMES[APOSTROPHE] = "'";
        NAMES[COMMA] = ",";
        NAMES[MINUS] = "-";
        NAMES[PERIOD] = ".";
        NAMES[SLASH] = "/";
        for (int i = 0; i <= 9; i++) {
            NAMES[D0 + i] = Integer.toString(i);
        }
        NAMES[SEMICOLON] = ";";
        NAMES[EQUAL] = "=";
        for (int i = A; i <= Z; i++) {
            NAMES[i] = Character.toString((char) i);
        }
        NAMES[LEFT_BRACKET] = "[";
        NAMES[BACKSLASH] = "\\";
        NAMES[RIGHT_BRACKET] = "]";
        NAMES[GRAVE_ACCENT] = "`";
        NAMES[ESCAPE] = "Esc";
        NAMES[ENTER] = "Enter";
        NAMES[TAB] = "Tab";
        NAMES[BACKSPACE] = "Backspace";
        NAMES[INSERT] = "Insert";
        NAMES[DELETE] = "Delete";
        NAMES[RIGHT] = "Right";
        NAMES[LEFT] = "Left";
        NAMES[DOWN] = "Down";
        NAMES[UP] = "Up";
        NAMES[PAGE_UP] = "Page Up";
        NAMES[PAGE_DOWN] = "Page Down";
        NAMES[HOME] = "Home";
        NAMES[END] = "End";
        NAMES[CAPS_LOCK] = "Caps Lock";
        NAMES[SCROLL_LOCK] = "Scroll Lock";
        NAMES[NUM_LOCK] = "Num Lock";
        NAMES[PRINT_SCREEN] = "Print Screen";
        NAMES[PAUSE] = "Pause";
        for (int i = 0; i < 12; i++) {
            NAMES[F1 + i] = "F" + (i + 1);
        }
        NAMES[LEFT_SHIFT] = "Left Shift";
        NAMES[LEFT_CONTROL] = "Left Ctrl";
        NAMES[LEFT_ALT] = "Left Alt";
        NAMES[LEFT_SUPER] = "Left Super";
        NAMES[RIGHT_SHIFT] = "Right Shift";
        NAMES[RIGHT_CONTROL] = "Right Ctrl";
        NAMES[RIGHT_ALT] = "Right Alt";
        NAMES[RIGHT_SUPER] = "Right Super";
        NAMES[MENU] = "Menu";
    }

    private Keys() {
    }

    /**
     * Human readable name of a key code.
     */
    public static String name(int keyCode) {
        if (keyCode < 0 || keyCode > GLFW_LAST) {
            return "Unknown";
        }
        String name = NAMES[keyCode];
        return name == null ? "Key " + keyCode : name;
    }

    public static boolean isModifierKey(int keyCode) {
        return keyCode == LEFT_SHIFT || keyCode == RIGHT_SHIFT
                || keyCode == LEFT_CONTROL || keyCode == RIGHT_CONTROL
                || keyCode == LEFT_ALT || keyCode == RIGHT_ALT
                || keyCode == LEFT_SUPER || keyCode == RIGHT_SUPER;
    }
}
