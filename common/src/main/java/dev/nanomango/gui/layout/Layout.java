package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;

/**
 * Arranges the children of a {@link Container}.
 */
public interface Layout {

    /**
     * Positions and sizes every child of the container.
     */
    void layout(Container container, GuiContext context);

    /**
     * Total spacing that this layout inserts between two children.
     */
    default float spacing() {
        return 0.0F;
    }
}
