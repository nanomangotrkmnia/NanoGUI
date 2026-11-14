package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A line chart that plots a series of values over time.
 */
public class Graph extends Component {

    private final List<Float> values = new ArrayList<>();
    private int maximumSamples = 120;
    private int lineColor = -1;
    private int fillColor = -1;
    private float minimum = 0.0F;
    private float maximum = 1.0F;
    private boolean autoScale = true;

    public Graph() {
    }

    public Graph addValue(float value) {
        values.add(value);
        while (values.size() > maximumSamples) {
            values.remove(0);
        }
        return this;
    }

    public Graph clear() {
        values.clear();
        return this;
    }

    public List<Float> values() {
        return values;
    }

    public Graph maximumSamples(int maximumSamples) {
        this.maximumSamples = maximumSamples;
        return this;
    }

    public Graph colors(int lineColor, int fillColor) {
        this.lineColor = lineColor;
        this.fillColor = fillColor;
        return this;
    }

    public Graph range(float minimum, float maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.autoScale = false;
        return this;
    }

    public Graph autoScale(boolean autoScale) {
        this.autoScale = autoScale;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        float radius = context.theme().smallRadius();
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.4F));

        if (values.size() < 2) {
            return;
        }

        float low = minimum;
        float high = maximum;
        if (autoScale) {
            low = Float.MAX_VALUE;
            high = -Float.MAX_VALUE;
            for (float value : values) {
                low = Math.min(low, value);
                high = Math.max(high, value);
            }
            if (high - low < 0.0001F) {
                high = low + 1.0F;
            }
            low -= (high - low) * 0.1F;
            high += (high - low) * 0.1F;
        }

        int line = lineColor >= 0 ? lineColor : accentColor(context);
        int fill = fillColor >= 0 ? fillColor : Colors.withAlphaFactor(line, 0.18F);

        float step = width() / Math.max(1, values.size() - 1);
        float previousX = 0.0F;
        float previousY = 0.0F;

        for (int i = 0; i < values.size(); i++) {
            float normalized = MathUtil.saturate((values.get(i) - low) / (high - low));
            float pointX = x() + i * step;
            float pointY = y() + height() - normalized * height();
            if (i > 0) {
                context.draw().line(previousX, previousY, pointX, pointY, line);
                context.draw().triangle(previousX, previousY, pointX, pointY,
                        (previousX + pointX) * 0.5F, (previousY + pointY) * 0.5F, fill);
            }
            previousX = pointX;
            previousY = pointY;
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 200.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return 80.0F;
    }
}
