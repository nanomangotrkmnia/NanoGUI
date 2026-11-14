package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.function.Consumer;

/**
 * A sliding on/off switch.
 *
 * <pre>{@code
 * new Toggle("Enabled", true).onChange(value -> config.enabled = value);
 * }</pre>
 */
public class Toggle extends Component {

    private String label;
    private boolean value;
    private Consumer<Boolean> onChange;
    private boolean horizontal = true;

    public Toggle(String label, boolean value) {
        this.label = label == null ? "" : label;
        this.value = value;
    }

    public Toggle(String label, boolean value, Consumer<Boolean> onChange) {
        this(label, value);
        this.onChange = onChange;
    }

    public String label() {
        return label;
    }

    public Toggle label(String label) {
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

    public Toggle onChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        return this;
    }

    /**
     * When {@code false} the label is drawn above the switch.
     */
    public Toggle horizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    private Font resolvedFont(GuiContext context) {
        return context.theme().regularFont() != null ? context.theme().regularFont() : context.font();
    }

    @Override
    protected void onClick() {
        setValue(!value);
    }

    @Override
    public void render(GuiContext context) {
        float trackWidth = context.theme().controlHeight() * 1.6F;
        float trackHeight = context.theme().controlHeight() * 0.8F;
        float knobRadius = trackHeight * 0.5F - 2.0F;

        float trackX;
        float trackY;
        if (horizontal) {
            trackX = x() + width() - trackWidth;
            trackY = y() + (height() - trackHeight) * 0.5F;
        } else {
            trackX = x();
            trackY = y() + height() - trackHeight;
        }

        int trackColor = value
                ? Colors.mix(context.theme().surfaceVariant(), accentColor(context), 0.85F)
                : context.theme().surfaceVariant();
        int border = value ? trackColor : context.theme().borderStrong();

        context.draw().fillRounded(trackX, trackY, trackWidth, trackHeight, trackHeight * 0.5F, trackColor);
        context.draw().strokeRounded(trackX, trackY, trackWidth, trackHeight, trackHeight * 0.5F,
                context.theme().borderWidth(), border);

        float progress = value ? 1.0F : 0.0F;
        float knobX = MathUtil.lerp(trackX + 2.0F, trackX + trackWidth - knobRadius * 2.0F - 2.0F, progress);
        float knobY = trackY + (trackHeight - knobRadius * 2.0F) * 0.5F;
        int knobColor = value ? context.theme().onAccent() : context.theme().textMuted();
        context.draw().fillRounded(knobX, knobY, knobRadius * 2.0F, knobRadius * 2.0F, knobRadius, knobColor);

        if (horizontal && !label.isEmpty()) {
            Font labelFont = resolvedFont(context);
            float textY = y() + (height() - labelFont.height()) * 0.5F;
            context.draw().text(x(), textY, labelFont.id(), label, textColor(context));
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        float trackWidth = context.theme().controlHeight() * 1.6F;
        if (horizontal && !label.isEmpty()) {
            return resolvedFont(context).width(label) + context.theme().spacing() + trackWidth;
        }
        return trackWidth;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        float trackHeight = context.theme().controlHeight() * 0.8F;
        if (horizontal && !label.isEmpty()) {
            return Math.max(trackHeight, resolvedFont(context).height());
        }
        return trackHeight;
    }
}
