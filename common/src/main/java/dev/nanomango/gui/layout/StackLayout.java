package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;

/**
 * Places every child on top of each other.
 *
 * <p>Used by tabbed containers where only one child is visible at a time.</p>
 */
public class StackLayout implements Layout {

    @Override
    public void layout(Container container, GuiContext context) {
        for (Component child : container.children()) {
            if (!child.isVisible()) {
                continue;
            }
            child.setBounds(container.x(), container.y(), container.width(), container.height());
        }
    }
}
