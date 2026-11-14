package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;

/**
 * Stacks the children from top to bottom.
 */
public class VerticalLayout implements Layout {

    private float spacing;
    private float padding;
    private Alignment alignment = Alignment.STRETCH;

    public VerticalLayout() {
    }

    public VerticalLayout(float spacing) {
        this.spacing = spacing;
    }

    public VerticalLayout(float spacing, float padding) {
        this.spacing = spacing;
        this.padding = padding;
    }

    public VerticalLayout alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public VerticalLayout padding(float padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public float spacing() {
        return spacing;
    }

    @Override
    public void layout(Container container, GuiContext context) {
        float contentX = container.x() + padding;
        float contentY = container.y() + padding;
        float contentWidth = Math.max(0.0F, container.width() - padding * 2.0F);
        float contentHeight = Math.max(0.0F, container.height() - padding * 2.0F);

        float cursor = contentY;
        boolean first = true;

        for (Component child : container.children()) {
            if (!child.isVisible()) {
                continue;
            }
            float childHeight = child.preferredHeight(context);
            float childWidth = alignment == Alignment.STRETCH ? contentWidth : child.preferredWidth(context);
            if (childHeight <= 0.0F) {
                // An empty child (for example a notification manager without
                // toasts) must not insert a gap between its neighbours.
                child.setBounds(contentX, cursor, childWidth, 0.0F);
                continue;
            }
            if (!first) {
                cursor += spacing;
            }
            first = false;

            float childX = contentX;
            switch (alignment) {
                case CENTER -> childX = contentX + (contentWidth - childWidth) * 0.5F;
                case RIGHT -> childX = contentX + contentWidth - childWidth;
                default -> {
                }
            }

            child.setBounds(childX, cursor, childWidth, childHeight);
            cursor += childHeight;
        }
    }

    /**
     * Horizontal alignment of the children.
     */
    public enum Alignment {
        STRETCH,
        LEFT,
        CENTER,
        RIGHT
    }
}
