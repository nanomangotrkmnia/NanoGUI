package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

/**
 * A horizontal progress bar.
 *
 * <p>The value animates towards its target so that repeated updates look
 * smooth instead of jumping.</p>
 */
public class ProgressBar extends Component {

    private float value;
    private float displayedValue;
    private float max = 1.0F;
    private int color = -1;
    private boolean animated = true;

    public ProgressBar(float value) {
        this.value = value;
        this.displayedValue = value;
    }

    public ProgressBar(float value, float max) {
        this(value);
        this.max = max;
    }

    public float value() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        if (!animated) {
            this.displayedValue = value;
        }
    }

    public ProgressBar max(float max) {
        this.max = max;
        return this;
    }

    public ProgressBar color(int color) {
        this.color = color;
        return this;
    }

    public ProgressBar animated(boolean animated) {
        this.animated = animated;
        return this;
    }

    private float progress() {
        if (max <= 0.0F) {
            return 0.0F;
        }
        return MathUtil.saturate(value / max);
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        if (animated) {
            float speed = Math.max(0.0001F, context.deltaSeconds() * 8.0F);
            displayedValue = MathUtil.lerp(displayedValue, value, Math.min(1.0F, speed));
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = height() * 0.5F;
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.8F));

        float shown = max <= 0.0F ? 0.0F : MathUtil.saturate(displayedValue / max);
        float fillWidth = width() * shown;
        if (fillWidth > 0.5F) {
            int fill = color >= 0 ? color : accentColor(context);
            context.draw().fillRounded(x(), y(), fillWidth, height(), radius, fill);
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 160.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return Math.max(6.0F, context.theme().controlHeight() * 0.28F);
    }
}
