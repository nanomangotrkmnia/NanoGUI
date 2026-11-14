package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;

import java.util.function.Consumer;

/**
 * A labelled check box.
 */
public class Checkbox extends Component {

    private String label;
    private boolean value;
    private Consumer<Boolean> onChange;

    public Checkbox(String label, boolean value) {
        this.label = label == null ? "" : label;
        this.value = value;
    }

    public Checkbox(String label, boolean value, Consumer<Boolean> onChange) {
        this(label, value);
        this.onChange = onChange;
    }

    public String label() {
        return label;
    }

    public Checkbox label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public boolean value() {
        return value;
    }

    public void setValue(boolean value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        if (onChange != null) {
            onChange.accept(value);
        }
    }

    public Checkbox onChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    protected void onClick() {
        setValue(!value);
    }

    private Font resolvedFont(GuiContext context) {
        return context.theme().regularFont() != null ? context.theme().regularFont() : context.font();
    }

    private float boxSize(GuiContext context) {
        return context.theme().controlHeight() * 0.72F;
    }

    @Override
    public void render(GuiContext context) {
        float size = boxSize(context);
        float boxY = y() + (height() - size) * 0.5F;
        int borderColor = value ? accentColor(context) : context.theme().borderStrong();
        int fill = value ? accentColor(context)
                : dev.nanomango.gui.util.Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.5F);

        context.draw().fillRounded(x(), boxY, size, size, context.theme().smallRadius(), fill);
        context.draw().strokeRounded(x(), boxY, size, size, context.theme().smallRadius(),
                context.theme().borderWidth(), borderColor);

        if (value) {
            float inset = size * 0.24F;
            context.draw().line(x() + inset, boxY + size * 0.52F, x() + size * 0.42F, boxY + size - inset,
                    context.theme().onAccent());
            context.draw().line(x() + size * 0.42F, boxY + size - inset, x() + size - inset, boxY + inset,
                    context.theme().onAccent());
        }

        if (!label.isEmpty()) {
            Font font = resolvedFont(context);
            float textY = y() + (height() - font.height()) * 0.5F;
            context.draw().text(x() + size + context.theme().spacing(), textY, font.id(), label, textColor(context));
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        float size = boxSize(context);
        if (label.isEmpty()) {
            return size;
        }
        return size + context.theme().spacing() + resolvedFont(context).width(label);
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return Math.max(boxSize(context), resolvedFont(context).height());
    }
}
