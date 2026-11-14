package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;

import java.util.function.Consumer;

/**
 * A numeric input field with increment and decrement buttons.
 */
public class NumberInput extends TextBox {

    private final double min;
    private final double max;
    private final double step;
    private Consumer<Double> onValueChange;

    public NumberInput(double min, double max, double value) {
        this(min, max, value, 1.0);
    }

    public NumberInput(double min, double max, double value, double step) {
        super(format(value, step));
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public NumberInput onValueChange(Consumer<Double> onValueChange) {
        this.onValueChange = onValueChange;
        return this;
    }

    public double value() {
        try {
            return Double.parseDouble(text().trim());
        } catch (NumberFormatException exception) {
            return min;
        }
    }

    public void setValue(double value) {
        setText(format(clamp(value), step));
    }

    private double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }

    private static String format(double value, double step) {
        if (step >= 1.0) {
            return String.valueOf((long) Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private void step(double amount) {
        setValue(clamp(value() + amount));
        if (onValueChange != null) {
            onValueChange.accept(value());
        }
    }

    @Override
    public void render(GuiContext context) {
        super.render(context);

        float buttonSize = height() * 0.6F;
        float right = x() + width() - padding(context);
        float upY = y() + (height() - buttonSize * 2.0F - 2.0F) * 0.5F;
        float downY = upY + buttonSize + 2.0F;
        int color = context.theme().textMuted();

        // Screen space grows downwards, so the base of the upper arrow is below
        // its tip and the base of the lower arrow is above its tip.
        context.draw().triangle(right - buttonSize * 0.5F, upY + buttonSize * 0.25F,
                right - buttonSize * 1.2F, upY + buttonSize * 0.75F,
                right + buttonSize * 0.2F, upY + buttonSize * 0.75F, color);
        context.draw().triangle(right - buttonSize * 0.5F, downY + buttonSize * 0.75F,
                right - buttonSize * 1.2F, downY + buttonSize * 0.25F,
                right + buttonSize * 0.2F, downY + buttonSize * 0.25F, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            float buttonSize = height() * 0.6F;
            float right = x() + width() - padding(context);
            float upY = y() + (height() - buttonSize * 2.0F - 2.0F) * 0.5F;
            float downY = upY + buttonSize + 2.0F;
            if (mouseY < upY + buttonSize) {
                step(stepSize());
                return true;
            }
            if (mouseY > downY && mouseY < downY + buttonSize) {
                step(-stepSize());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private double stepSize() {
        return step;
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 120.0F;
    }
}
