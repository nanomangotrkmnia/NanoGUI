package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;

/**
 * A square button that renders an icon instead of a text label.
 *
 * <p>Icons are described by an {@link IconPainter}, which lets a project draw
 * its own vector icons without touching the rendering engine.</p>
 */
public class IconButton extends Component {

    private final IconPainter painter;
    private Runnable onClick;
    private int iconColor = -1;
    private boolean danger;

    public IconButton(IconPainter painter) {
        this.painter = painter;
    }

    public IconButton(IconPainter painter, Runnable onClick) {
        this.painter = painter;
        this.onClick = onClick;
    }

    public IconButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public IconButton iconColor(int iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public IconButton danger(boolean danger) {
        this.danger = danger;
        return this;
    }

    @Override
    protected void onClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        int background = danger
                ? Colors.withAlphaFactor(context.theme().danger(), 0.18F + 0.22F * hoverProgress())
                : Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.35F + 0.65F * hoverProgress());
        context.draw().fillRounded(x(), y(), width(), height(), radius, background);

        int color = iconColor >= 0 ? iconColor : (danger ? context.theme().danger() : textColor(context));
        if (painter != null) {
            painter.paint(context, this, color);
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

    /**
     * Draws an icon inside a component.
     */
    @FunctionalInterface
    public interface IconPainter {
        void paint(GuiContext context, IconButton button, int color);
    }
}
