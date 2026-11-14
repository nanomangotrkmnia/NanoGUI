package dev.nanomango.gui.input;

/**
 * Snapshot of the mouse and keyboard state for a single frame.
 *
 * <p>The version modules translate the host's input events into calls on this
 * class. Everything above this layer is therefore completely independent of
 * the Minecraft version.</p>
 */
public final class InputState {

    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_RIGHT = 1;
    public static final int MOUSE_BUTTON_MIDDLE = 2;
    public static final int MOUSE_BUTTON_COUNT = 8;

    private final boolean[] buttons = new boolean[MOUSE_BUTTON_COUNT];
    private final boolean[] buttonsPressed = new boolean[MOUSE_BUTTON_COUNT];
    private final boolean[] buttonsReleased = new boolean[MOUSE_BUTTON_COUNT];

    private double mouseX;
    private double mouseY;
    private double scrollX;
    private double scrollY;
    private boolean shift;
    private boolean control;
    private boolean alt;
    private boolean superKey;
    private boolean focused = true;

    private final StringBuilder textBuffer = new StringBuilder();

    // ------------------------------------------------------------------
    // Mouse
    // ------------------------------------------------------------------

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public void setMousePosition(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public boolean isButtonDown(int button) {
        return button >= 0 && button < MOUSE_BUTTON_COUNT && buttons[button];
    }

    /**
     * True on the frame the button went down.
     */
    public boolean isButtonPressed(int button) {
        return button >= 0 && button < MOUSE_BUTTON_COUNT && buttonsPressed[button];
    }

    /**
     * True on the frame the button went up.
     */
    public boolean isButtonReleased(int button) {
        return button >= 0 && button < MOUSE_BUTTON_COUNT && buttonsReleased[button];
    }

    public void setButton(int button, boolean down) {
        if (button < 0 || button >= MOUSE_BUTTON_COUNT) {
            return;
        }
        if (down && !buttons[button]) {
            buttonsPressed[button] = true;
        } else if (!down && buttons[button]) {
            buttonsReleased[button] = true;
        }
        buttons[button] = down;
    }

    public double scrollX() {
        return scrollX;
    }

    public double scrollY() {
        return scrollY;
    }

    public void addScroll(double x, double y) {
        this.scrollX += x;
        this.scrollY += y;
    }

    // ------------------------------------------------------------------
    // Keyboard
    // ------------------------------------------------------------------

    public boolean isShiftDown() {
        return shift;
    }

    public boolean isControlDown() {
        return control;
    }

    public boolean isAltDown() {
        return alt;
    }

    public boolean isSuperDown() {
        return superKey;
    }

    public void setModifiers(boolean shift, boolean control, boolean alt, boolean superKey) {
        this.shift = shift;
        this.control = control;
        this.alt = alt;
        this.superKey = superKey;
    }

    public int modifierMask() {
        return Modifiers.fromShift(shift, control, alt);
    }

    /**
     * Text typed during the current frame. Cleared by {@link #endFrame()}.
     */
    public String typedText() {
        return textBuffer.toString();
    }

    public void addTypedText(char character) {
        textBuffer.append(character);
    }

    public void addTypedText(String text) {
        textBuffer.append(text);
    }

    public boolean hasFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * Clears the per frame state. Called once the frame has been processed.
     */
    public void endFrame() {
        for (int i = 0; i < MOUSE_BUTTON_COUNT; i++) {
            buttonsPressed[i] = false;
            buttonsReleased[i] = false;
        }
        scrollX = 0.0;
        scrollY = 0.0;
        textBuffer.setLength(0);
    }

    /**
     * Clears everything, including the held button state. Used when the GUI
     * opens or closes so that no key or mouse button stays stuck.
     */
    public void reset() {
        endFrame();
        for (int i = 0; i < MOUSE_BUTTON_COUNT; i++) {
            buttons[i] = false;
        }
        shift = false;
        control = false;
        alt = false;
        superKey = false;
        mouseX = 0.0;
        mouseY = 0.0;
    }
}
