package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * A small floating surface anchored to a parent component.
 */
public class Popover extends Component {

    private Component anchor;
    private float offsetX;
    private float offsetY;
    private boolean open;

    public Popover(Component anchor) {
        this.anchor = anchor;
    }

    public Popover offset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        setVisible(open);
    }

    public void toggle() {
        setOpen(!open);
    }

    @Override
    public void render(GuiContext context) {
        if (!open) {
            return;
        }
        if (anchor != null) {
            dev.nanomango.gui.util.Rect bounds = anchor.bounds();
            setBounds(bounds.x() + offsetX, bounds.bottom() + offsetY, width(), height());
        }
        float radius = context.theme().radius();
        context.draw().shadow(x(), y(), width(), height(), radius, context.theme().shadowBlur(),
                context.theme().shadow());
        context.draw().fillRounded(x(), y(), width(), height(), radius, context.theme().surfaceElevated());
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(),
                context.theme().borderStrong());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
