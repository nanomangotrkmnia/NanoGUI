package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * Draws a horizontal gradient between two colours.
 */
public class ColorGradient extends Component {

    private int startColor = 0xFF000000;
    private int endColor = 0xFFFFFFFF;
    private int steps = 32;
    private boolean vertical;

    public ColorGradient() {
    }

    public ColorGradient(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public ColorGradient colors(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        return this;
    }

    public ColorGradient steps(int steps) {
        this.steps = Math.max(2, steps);
        return this;
    }

    public ColorGradient vertical() {
        this.vertical = true;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        int count = steps;
        float segmentWidth = width() / count;
        float segmentHeight = height() / count;
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0.0F : (float) i / (count - 1);
            int color = dev.nanomango.gui.util.Colors.mix(startColor, endColor, t);
            if (vertical) {
                context.draw().fillRect(x(), y() + i * segmentHeight, width(), segmentHeight + 1.0F, color);
            } else {
                context.draw().fillRect(x() + i * segmentWidth, y(), segmentWidth + 1.0F, height(), color);
            }
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 160.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return 16.0F;
    }
}
