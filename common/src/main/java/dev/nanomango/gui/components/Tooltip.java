package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.MathUtil;

/**
 * A small floating label that shows additional information.
 */
public class Tooltip extends Component {

    private final String text;
    private float padding = 6.0F;

    public Tooltip(String text) {
        this.text = text == null ? "" : text;
    }

    public String text() {
        return text;
    }

    public Tooltip padding(float padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        float textWidth = context.font().width(text);
        float textHeight = context.font().height();
        float width = textWidth + padding * 2.0F;
        float height = textHeight + padding * 2.0F;

        float targetX = x();
        float targetY = y();
        if (targetX + width > context.viewportWidth()) {
            targetX = context.viewportWidth() - width - 2.0F;
        }
        if (targetY + height > context.viewportHeight()) {
            targetY = context.viewportHeight() - height - 2.0F;
        }
        targetX = Math.max(0.0F, targetX);
        targetY = Math.max(0.0F, targetY);

        context.draw().shadow(targetX, targetY, width, height, context.theme().smallRadius(),
                context.theme().shadowBlur(), context.theme().shadow());
        context.draw().fillRounded(targetX, targetY, width, height, context.theme().smallRadius(),
                context.theme().surfaceElevated());
        context.draw().strokeRounded(targetX, targetY, width, height, context.theme().smallRadius(),
                context.theme().borderWidth(), context.theme().borderStrong());
        context.draw().text(targetX + padding, targetY + padding, context.font().id(), text,
                context.theme().text());
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return context.font().width(text) + padding * 2.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.font().height() + padding * 2.0F;
    }

    /**
     * Clamps a position so the tooltip stays on screen.
     */
    public static float clampX(float value, float width, int viewportWidth) {
        return MathUtil.clamp(value, 0.0F, Math.max(0.0F, viewportWidth - width));
    }
}
