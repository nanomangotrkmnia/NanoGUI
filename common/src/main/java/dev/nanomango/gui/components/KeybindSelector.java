package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.input.Keybind;
import dev.nanomango.gui.input.Keys;
import dev.nanomango.gui.input.Modifiers;

import java.util.function.Consumer;

/**
 * A control that lets the user rebind a {@link Keybind}.
 */
public class KeybindSelector extends Component {

    private final Keybind keybind;
    private boolean listening;
    private Consumer<Keybind> onChange;

    public KeybindSelector(Keybind keybind) {
        this.keybind = keybind;
    }

    public Keybind keybind() {
        return keybind;
    }

    public KeybindSelector onChange(Consumer<Keybind> onChange) {
        this.onChange = onChange;
        return this;
    }

    public boolean isListening() {
        return listening;
    }

    public void startListening() {
        listening = true;
    }

    public void cancel() {
        listening = false;
    }

    @Override
    protected void onClick() {
        listening = !listening;
    }

    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (!listening) {
            return false;
        }
        if (keyCode == Keys.ESCAPE) {
            listening = false;
            return true;
        }
        keybind.setKeyCode(keyCode);
        keybind.setModifiers(Modifiers.fromShift(
                (modifiers & Modifiers.SHIFT) != 0,
                (modifiers & Modifiers.CONTROL) != 0,
                (modifiers & Modifiers.ALT) != 0));
        listening = false;
        if (onChange != null) {
            onChange.accept(keybind);
        }
        return true;
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        int border = listening ? accentColor(context) : borderColor(context);
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                dev.nanomango.gui.util.Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.45F));
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(), border);

        String text = listening ? "Press a key..." : keybind.displayName();
        int color = listening ? accentColor(context) : textColor(context);
        float textWidth = context.font().width(text);
        float textX = x() + (width() - textWidth) * 0.5F;
        float textY = y() + (height() - context.font().height()) * 0.5F;
        context.draw().text(textX, textY, context.font().id(), text, color);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 120.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.theme().controlHeight();
    }
}
