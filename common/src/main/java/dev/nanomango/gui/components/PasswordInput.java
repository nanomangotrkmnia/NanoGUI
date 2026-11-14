package dev.nanomango.gui.components;

/**
 * A text field that masks its content.
 */
public class PasswordInput extends TextBox {

    public PasswordInput() {
        super();
        password(true);
    }

    public PasswordInput(String text) {
        super(text);
        password(true);
    }
}
