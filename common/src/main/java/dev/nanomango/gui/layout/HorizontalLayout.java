package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;

/**
 * Stacks the children from left to right.
 */
public class HorizontalLayout implements Layout {

    private float spacing;
    private float padding;
    private VerticalAlignment verticalAlignment = VerticalAlignment.STRETCH;

    public HorizontalLayout() {
    }

    public HorizontalLayout(float spacing) {
        this.spacing = spacing;
    }

    public HorizontalLayout(float spacing, float padding) {
        this.spacing = spacing;
        this.padding = padding;
    }

    public HorizontalLayout verticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        return this;
    }

    public HorizontalLayout padding(float padding) {
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

        float cursor = contentX;
        boolean first = true;

        for (Component child : container.children()) {
            if (!child.isVisible()) {
                continue;
            }
            if (!first) {
                cursor += spacing;
            }
            first = false;

            float childWidth = child.preferredWidth(context);
            float childHeight = verticalAlignment == VerticalAlignment.STRETCH ? contentHeight : child.preferredHeight(context);

            float childY = contentY;
            switch (verticalAlignment) {
                case CENTER -> childY = contentY + (contentHeight - childHeight) * 0.5F;
                case BOTTOM -> childY = contentY + contentHeight - childHeight;
                default -> {
                }
            }

            child.setBounds(cursor, childY, childWidth, childHeight);
            cursor += childWidth;
        }
    }

    /**
     * Vertical alignment of the children.
     */
    public enum VerticalAlignment {
        STRETCH,
        TOP,
        CENTER,
        BOTTOM
    }
}
