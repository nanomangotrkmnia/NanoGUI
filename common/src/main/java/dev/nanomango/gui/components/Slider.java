package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.function.Consumer;

/**
 * A draggable slider with a numeric range.
 *
 * <pre>{@code
 * new Slider("Volume", 0, 100, 50).onChange(value -> volume = value);
 * }</pre>
 */
public class Slider extends Component {

    private String label;
    private float min;
    private float max;
    private float value;
    private float step;
    private Consumer<Float> onChange;
    private boolean dragging;
    private boolean showValue = true;
    private int gradientStart = -1;
    private int gradientEnd = -1;

    /** Geometry of the track, refreshed every frame by {@link #render}. */
    private float trackX;
    private float trackUsableWidth;
    private float handleRadius = 7.5F;

    public Slider(String label, float min, float max, float value) {
        this.label = label == null ? "" : label;
        this.min = min;
        this.max = max;
        this.value = MathUtil.clamp(value, min, max);
    }

    public Slider(String label, float min, float max, float value, Consumer<Float> onChange) {
        this(label, min, max, value);
        this.onChange = onChange;
    }

    public String label() {
        return label;
    }

    public Slider label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public float value() {
        return value;
    }

    public void setValue(float value) {
        float clamped = MathUtil.clamp(value, min, max);
        if (step > 0.0F) {
            clamped = MathUtil.snap(clamped, step);
            clamped = MathUtil.clamp(clamped, min, max);
        }
        if (Math.abs(clamped - this.value) < 1.0e-6F) {
            return;
        }
        this.value = clamped;
        if (onChange != null) {
            onChange.accept(clamped);
        }
    }

    public Slider onChange(Consumer<Float> onChange) {
        this.onChange = onChange;
        return this;
    }

    public Slider step(float step) {
        this.step = step;
        return this;
    }

    public Slider showValue(boolean showValue) {
        this.showValue = showValue;
        return this;
    }

    /**
     * Colours used for the filled part of the track. When unset the accent
     * colour is used.
     */
    public Slider gradient(int start, int end) {
        this.gradientStart = start;
        this.gradientEnd = end;
        return this;
    }

    public Slider range(float min, float max) {
        this.min = min;
        this.max = max;
        setValue(value);
        return this;
    }

    private float progress() {
        if (max <= min) {
            return 0.0F;
        }
        return MathUtil.clamp((value - min) / (max - min), 0.0F, 1.0F);
    }

    private float trackHeight(GuiContext context) {
        return Math.max(3.0F, context.theme().controlHeight() * 0.16F);
    }

    private float handleRadius(GuiContext context) {
        return context.theme().controlHeight() * 0.32F;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isEnabled()) {
            return false;
        }
        if (button != 0 || !isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        dragging = true;
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void updateFromMouse(double mouseX) {
        float usable = Math.max(1.0F, trackUsableWidth);
        float t = MathUtil.clamp((float) ((mouseX - trackX) / usable), 0.0F, 1.0F);
        setValue(min + (max - min) * t);
    }

    @Override
    public void render(GuiContext context) {
        float track = trackHeight(context);
        float radius = handleRadius(context);
        float trackY = y() + (height() - track) * 0.5F;

        if (!label.isEmpty()) {
            float textY = y();
            context.draw().text(x(), textY, context.font().id(), label, textColor(context));
        }

        float valueTextWidth = 0.0F;
        if (showValue) {
            String text = formatValue();
            valueTextWidth = context.font().width(text);
            float textY = y();
            context.draw().text(x() + width() - valueTextWidth, textY, context.font().id(), text,
                    context.theme().textMuted());
        }

        float trackWidth = width() - valueTextWidth - (valueTextWidth > 0.0F ? context.theme().spacing() : 0.0F);
        float usable = Math.max(1.0F, trackWidth - radius * 2.0F);
        this.trackX = x();
        this.trackUsableWidth = usable;
        this.handleRadius = radius;

        context.draw().fillRounded(trackX, trackY, trackWidth, track, track * 0.5F,
                Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.9F));

        float filled = radius + usable * progress();
        int fillColor = accentColor(context);
        if (gradientStart >= 0 && gradientEnd >= 0) {
            context.draw().fillGradient(trackX, trackY, filled, track, track * 0.5F, gradientStart, gradientEnd);
        } else {
            context.draw().fillRounded(trackX, trackY, filled, track, track * 0.5F, fillColor);
        }

        float handleX = trackX + usable * progress();
        float handleY = y() + (height() - radius * 2.0F) * 0.5F;
        int handleColor = isEnabled() ? context.theme().text() : context.theme().textDisabled();
        context.draw().shadow(handleX, handleY, radius * 2.0F, radius * 2.0F, radius,
                context.theme().shadowBlur() * 0.5F, context.theme().shadow());
        context.draw().fillRounded(handleX, handleY, radius * 2.0F, radius * 2.0F, radius, handleColor);
    }

    private String formatValue() {
        if (step >= 1.0F) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return Math.max(120.0F, context.font().width(label) + 60.0F);
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return Math.max(context.theme().controlHeight(), context.font().height() * 2.0F + 6.0F);
    }
}
