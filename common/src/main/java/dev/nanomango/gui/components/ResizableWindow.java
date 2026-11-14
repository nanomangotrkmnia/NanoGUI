package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

import java.util.function.Consumer;

/**
 * A window with a draggable header and a resizable edge.
 */
public class ResizableWindow extends Container {

    private String title = "";
    private boolean resizing;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private float minWidth = 160.0F;
    private float minHeight = 100.0F;
    private Consumer<ResizableWindow> onResize;

    public ResizableWindow() {
        super(new VerticalLayout());
        clip(false);
    }

    public ResizableWindow(String title) {
        this();
        this.title = title == null ? "" : title;
    }

    public ResizableWindow title(String title) {
        this.title = title == null ? "" : title;
        return this;
    }

    public ResizableWindow minimumSize(float width, float height) {
        this.minWidth = width;
        this.minHeight = height;
        return this;
    }

    public ResizableWindow onResize(Consumer<ResizableWindow> onResize) {
        this.onResize = onResize;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        float radius = context.theme().radius();
        context.draw().shadow(x(), y(), width(), height(), radius, context.theme().shadowBlur(),
                context.theme().shadow());
        context.draw().fillRounded(x(), y(), width(), height(), radius, context.theme().surfaceElevated());
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(),
                context.theme().borderStrong());

        float headerHeight = context.theme().controlHeight();
        context.draw().text(x() + context.theme().padding(), y() + (headerHeight - context.font().height()) * 0.5F,
                context.boldFont().id(), title, context.theme().text());
        context.draw().fillRect(x(), y() + headerHeight, width(), 1.0F, context.theme().border());

        super.render(context);

        // Resize grip in the bottom right corner.
        float grip = 10.0F;
        context.draw().line(x() + width() - grip, y() + height() - 3.0F, x() + width() - 3.0F, y() + height() - grip,
                context.theme().textMuted());
        context.draw().line(x() + width() - grip, y() + height() - 7.0F, x() + width() - 7.0F, y() + height() - grip,
                context.theme().textMuted());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        float grip = 12.0F;
        boolean onGrip = mouseX >= x() + width() - grip && mouseY >= y() + height() - grip;
        boolean onHeader = mouseY >= y() && mouseY < y() + context0HeaderHeight();
        if (onGrip) {
            resizing = true;
            return true;
        }
        if (onHeader) {
            dragging = true;
            dragOffsetX = mouseX - x();
            dragOffsetY = mouseY - y();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float context0HeaderHeight() {
        return 24.0F;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (resizing && button == 0) {
            resizing = false;
            return true;
        }
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (resizing) {
            setWidth(Math.max(minWidth, (float) (mouseX - x())));
            setHeight(Math.max(minHeight, (float) (mouseY - y())));
            if (onResize != null) {
                onResize.accept(this);
            }
            return true;
        }
        if (dragging) {
            setX((float) (mouseX - dragOffsetX));
            setY((float) (mouseY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
