package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;

/**
 * Arranges the children in a grid with a fixed number of columns.
 */
public class GridLayout implements Layout {

    private final int columns;
    private float spacing;
    private float padding;

    public GridLayout(int columns) {
        if (columns <= 0) {
            throw new IllegalArgumentException("columns must be positive");
        }
        this.columns = columns;
    }

    public GridLayout(int columns, float spacing) {
        this(columns);
        this.spacing = spacing;
    }

    public GridLayout spacing(float spacing) {
        this.spacing = spacing;
        return this;
    }

    public GridLayout padding(float padding) {
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

        float cellWidth = (contentWidth - spacing * (columns - 1)) / columns;

        int index = 0;
        float rowTop = contentY;
        float rowHeight = 0.0F;

        for (Component child : container.children()) {
            if (!child.isVisible()) {
                continue;
            }
            int column = index % columns;
            if (column == 0 && index > 0) {
                rowTop += rowHeight + spacing;
                rowHeight = 0.0F;
            }

            float childHeight = child.preferredHeight(context);
            float childX = contentX + column * (cellWidth + spacing);
            child.setBounds(childX, rowTop, cellWidth, childHeight);
            rowHeight = Math.max(rowHeight, childHeight);
            index++;
        }
    }
}
