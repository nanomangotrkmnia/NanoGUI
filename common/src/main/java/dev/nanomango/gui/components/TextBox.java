package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.input.Clipboard;
import dev.nanomango.gui.input.Keys;
import dev.nanomango.gui.util.Colors;

import java.util.function.Consumer;

/**
 * An editable single line text field.
 *
 * <p>Supports caret movement, word wise deletion, selection with the shift
 * key, and the usual clipboard shortcuts.</p>
 */
public class TextBox extends Component {

    private String text = "";
    private String placeholder = "";
    private int caret;
    private int selectionAnchor = -1;
    private Consumer<String> onChange;
    private boolean password;
    private int maxLength = 32767;
    private char passwordChar = '•';

    public TextBox() {
    }

    public TextBox(String text) {
        setText(text);
    }

    public TextBox(String text, Consumer<String> onChange) {
        setText(text);
        this.onChange = onChange;
    }

    public String text() {
        return text;
    }

    public final void setText(String text) {
        this.text = text == null ? "" : text;
        if (caret > this.text.length()) {
            caret = this.text.length();
        }
        selectionAnchor = -1;
        if (onChange != null) {
            onChange.accept(this.text);
        }
    }

    public TextBox onChange(Consumer<String> onChange) {
        this.onChange = onChange;
        return this;
    }

    public TextBox placeholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        return this;
    }

    public String placeholder() {
        return placeholder;
    }

    public TextBox maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public TextBox password(boolean password) {
        this.password = password;
        return this;
    }

    public TextBox passwordChar(char passwordChar) {
        this.passwordChar = passwordChar;
        return this;
    }

    public int caret() {
        return caret;
    }

    public void setCaret(int caret) {
        this.caret = Math.max(0, Math.min(caret, text.length()));
    }

    /**
     * True when the field currently holds the keyboard focus.
     */
    public boolean isEditing() {
        return isFocused();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    // ------------------------------------------------------------------
    // Editing
    // ------------------------------------------------------------------

    private boolean hasSelection() {
        return selectionAnchor >= 0 && selectionAnchor != caret;
    }

    private int selectionStart() {
        return Math.min(selectionAnchor, caret);
    }

    private int selectionEnd() {
        return Math.max(selectionAnchor, caret);
    }

    private String selectedText() {
        if (!hasSelection()) {
            return "";
        }
        return text.substring(selectionStart(), selectionEnd());
    }

    private void deleteSelection() {
        if (!hasSelection()) {
            return;
        }
        int start = selectionStart();
        int end = selectionEnd();
        text = text.substring(0, start) + text.substring(end);
        caret = start;
        selectionAnchor = -1;
        notifyChanged();
    }

    private void notifyChanged() {
        if (onChange != null) {
            onChange.accept(text);
        }
    }

    @Override
    public boolean charTyped(int codePoint) {
        if (!isFocused() || !isEnabled()) {
            return false;
        }
        if (text.length() >= maxLength) {
            return true;
        }
        String character = new String(Character.toChars(codePoint));
        deleteSelection();
        text = text.substring(0, caret) + character + text.substring(caret);
        caret += character.length();
        notifyChanged();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (!isFocused() || !isEnabled()) {
            return false;
        }
        boolean control = dev.nanomango.gui.input.Modifiers.hasControl(modifiers);
        boolean shift = dev.nanomango.gui.input.Modifiers.hasShift(modifiers);

        switch (keyCode) {
            case Keys.LEFT -> {
                moveCaret(caret - 1, shift);
                return true;
            }
            case Keys.RIGHT -> {
                moveCaret(caret + 1, shift);
                return true;
            }
            case Keys.HOME -> {
                moveCaret(0, shift);
                return true;
            }
            case Keys.END -> {
                moveCaret(text.length(), shift);
                return true;
            }
            case Keys.BACKSPACE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (caret > 0) {
                    int removeFrom = control ? wordStart(caret) : caret - 1;
                    text = text.substring(0, removeFrom) + text.substring(caret);
                    caret = removeFrom;
                    notifyChanged();
                }
                return true;
            }
            case Keys.DELETE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (caret < text.length()) {
                    int removeTo = control ? wordEnd(caret) : caret + 1;
                    text = text.substring(0, caret) + text.substring(removeTo);
                    notifyChanged();
                }
                return true;
            }
            case Keys.A -> {
                if (control) {
                    selectionAnchor = 0;
                    caret = text.length();
                    return true;
                }
                return false;
            }
            case Keys.C -> {
                if (control && hasSelection()) {
                    clipboard().setContents(selectedText());
                    return true;
                }
                return false;
            }
            case Keys.X -> {
                if (control && hasSelection()) {
                    clipboard().setContents(selectedText());
                    deleteSelection();
                    return true;
                }
                return false;
            }
            case Keys.V -> {
                if (control) {
                    insert(clipboard().contents());
                    return true;
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    private Clipboard clipboard() {
        Clipboard clipboard = context() == null ? null : context().clipboard();
        return clipboard != null ? clipboard : FALLBACK_CLIPBOARD;
    }

    private static final Clipboard FALLBACK_CLIPBOARD = new Clipboard.Memory();

    private void insert(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        deleteSelection();
        String sanitised = value.replace('\n', ' ').replace('\r', ' ');
        if (text.length() + sanitised.length() > maxLength) {
            sanitised = sanitised.substring(0, Math.max(0, maxLength - text.length()));
        }
        text = text.substring(0, caret) + sanitised + text.substring(caret);
        caret += sanitised.length();
        notifyChanged();
    }

    private void moveCaret(int newCaret, boolean extendSelection) {
        int previous = caret;
        newCaret = Math.max(0, Math.min(newCaret, text.length()));
        if (extendSelection) {
            if (selectionAnchor < 0) {
                selectionAnchor = previous;
            }
        } else {
            selectionAnchor = -1;
        }
        caret = newCaret;
    }

    private int wordStart(int position) {
        int index = position;
        while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private int wordEnd(int position) {
        int index = position;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    /**
     * Space that subclasses reserve on the left of the text for decorations
     * such as an icon. The text and the caret start after it.
     */
    protected float leftInset(GuiContext context) {
        return 0.0F;
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        int border = isFocused() ? accentColor(context) : borderColor(context);
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.45F));
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(), border);

        float textX = x() + padding(context) + leftInset(context);
        float textY = y() + (height() - context.font().height()) * 0.5F;

        if (text.isEmpty() && !placeholder.isEmpty() && !isFocused()) {
            context.draw().text(textX, textY, context.font().id(), placeholder, context.theme().textDisabled());
            return;
        }

        String display = password ? repeat(passwordChar, text.length()) : text;
        context.draw().text(textX, textY, context.font().id(), display, textColor(context));

        if (isFocused()) {
            float caretX = textX + context.font().width(display.substring(0, Math.min(caret, display.length())));
            context.draw().fillRect(caretX, textY, 1.0F, context.font().height(), accentColor(context));
        }
    }

    private static String repeat(char character, int count) {
        char[] characters = new char[count];
        java.util.Arrays.fill(characters, character);
        return new String(characters);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 160.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.theme().controlHeight();
    }
}
