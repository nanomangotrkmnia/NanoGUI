package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.Layout;
import dev.nanomango.gui.layout.VerticalLayout;

/**
 * A titled container that groups related widgets.
 *
 * <pre>{@code
 * Panel panel = new Panel("General");
 * panel.add(new Text("General Settings"));
 * }</pre>
 */
public class Panel extends Container {

    private String title = "";
    private boolean showTitle = true;

    public Panel() {
        this("");
    }

    public Panel(String title) {
        super(new VerticalLayout());
        this.title = title == null ? "" : title;
        clip(false);
    }

    public Panel(String title, Layout layout) {
        super(layout);
        this.title = title == null ? "" : title;
        clip(false);
    }

    public String title() {
        return title;
    }

    public Panel title(String title) {
        this.title = title == null ? "" : title;
        return this;
    }

    public Panel showTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    @Override
    protected float contentOffset(GuiContext context) {
        if (!showTitle || title.isEmpty()) {
            return 0.0F;
        }
        return context.boldFont().height() + context.theme().spacing();
    }

    @Override
    public void render(GuiContext context) {
        renderPanel(context, x(), y(), width(), height(), backgroundColor(context));
        if (showTitle && !title.isEmpty()) {
            float titleY = y() + padding(context) * 0.6F;
            context.draw().text(x() + padding(context), titleY, context.boldFont().id(), title,
                    context.theme().text());
        }
        super.render(context);
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return contentOffset(context) + super.preferredHeight(context) + padding(context) * 2.0F;
    }
}
