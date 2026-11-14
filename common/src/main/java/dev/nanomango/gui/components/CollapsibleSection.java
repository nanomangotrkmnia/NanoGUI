package dev.nanomango.gui.components;

import dev.nanomango.gui.animation.Easing;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

/**
 * A section with a header that can be collapsed and expanded.
 */
public class CollapsibleSection extends Container {

    private final String title;
    private boolean expanded = true;
    private float expandProgress = 1.0F;
    private float contentHeight;

    public CollapsibleSection(String title) {
        super(new VerticalLayout());
        this.title = title == null ? "" : title;
        clip(false);
    }

    public String title() {
        return title;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public CollapsibleSection expanded(boolean expanded) {
        this.expanded = expanded;
        expandProgress = expanded ? 1.0F : 0.0F;
        return this;
    }

    @Override
    protected void onClick() {
        expanded = !expanded;
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        float target = expanded ? 1.0F : 0.0F;
        float speed = Math.min(1.0F, context.deltaSeconds() * 12.0F);
        expandProgress = expandProgress + (target - expandProgress) * speed;
        if (Math.abs(target - expandProgress) < 0.001F) {
            expandProgress = target;
        }
    }

    @Override
    protected float contentOffset(GuiContext context) {
        return context.theme().controlHeight();
    }

    @Override
    public void render(GuiContext context) {
        float headerHeight = context.theme().controlHeight();
        float arrowSize = context.theme().iconSize() * 0.45F;
        float arrowX = x() + padding(context);
        float arrowY = y() + (headerHeight - arrowSize) * 0.5F;

        int arrowColor = context.theme().textMuted();
        float offset = (1.0F - expandProgress) * arrowSize * 0.6F;
        context.draw().triangle(arrowX + offset, arrowY, arrowX + arrowSize + offset, arrowY,
                arrowX + arrowSize * 0.5F + offset, arrowY + arrowSize, arrowColor);

        context.draw().text(x() + padding(context) + arrowSize + context.theme().spacing(),
                y() + (headerHeight - context.font().height()) * 0.5F, context.boldFont().id(), title,
                textColor(context));

        if (expandProgress > 0.01F) {
            float visibleHeight = super.preferredHeight(context) * expandProgress;
            context.draw().clipPush(x(), y() + headerHeight, width(), visibleHeight);
            super.render(context);
            context.draw().clipPop();
        }
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return contentOffset(context) + super.preferredHeight(context);
    }
}
