package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * An animated spinner shown while something is loading.
 */
public class LoadingIndicator extends Component {

    private float rotation;
    private float speed = 2.2F;
    private int color = -1;

    public LoadingIndicator() {
    }

    public LoadingIndicator(float speed) {
        this.speed = speed;
    }

    public LoadingIndicator color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        rotation = (rotation + context.deltaSeconds() * speed * 60.0F) % 360.0F;
    }

    @Override
    public void render(GuiContext context) {
        float radius = Math.min(width(), height()) * 0.5F;
        float centerX = x() + width() * 0.5F;
        float centerY = y() + height() * 0.5F;
        int effective = color >= 0 ? color : accentColor(context);
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            double angle = Math.toRadians(rotation + i * (360.0 / segments));
            float alpha = 0.15F + 0.85F * (i / (float) segments);
            int segmentColor = dev.nanomango.gui.util.Colors.withAlphaFactor(effective, alpha);
            float x1 = centerX + (float) Math.cos(angle) * radius * 0.55F;
            float y1 = centerY + (float) Math.sin(angle) * radius * 0.55F;
            float x2 = centerX + (float) Math.cos(angle) * radius;
            float y2 = centerY + (float) Math.sin(angle) * radius;
            context.draw().line(x1, y1, x2, y2, segmentColor);
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return context.theme().controlHeight();
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.theme().controlHeight();
    }
}
