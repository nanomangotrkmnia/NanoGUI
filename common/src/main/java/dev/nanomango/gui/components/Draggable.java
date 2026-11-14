package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * Wraps a component and makes it draggable with the mouse.
 */
public class Draggable extends Component {

    private final Component target;
    private boolean dragging;
    private double offsetX;
    private double offsetY;
    private boolean draggable = true;

    public Draggable(Component target) {
        this.target = target;
    }

    public Component target() {
        return target;
    }

    public Draggable draggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void render(GuiContext context) {
        if (target != null) {
            target.render(context);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (target != null && target.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (draggable && button == 0 && isMouseOver(mouseX, mouseY)) {
            dragging = true;
            offsetX = mouseX - x();
            offsetY = mouseY - y();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return target != null && target.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            setX((float) (mouseX - offsetX));
            setY((float) (mouseY - offsetY));
            if (target != null) {
                target.setX(x());
                target.setY(y());
            }
            return true;
        }
        return target != null && target.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if (target != null) {
            target.mouseMoved(mouseX, mouseY);
        }
    }
}
