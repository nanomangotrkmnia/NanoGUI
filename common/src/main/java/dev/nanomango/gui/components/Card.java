package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

/**
 * A raised surface used to group content, for example a single entry in a
 * list.
 */
public class Card extends Container {

    private boolean elevated = true;

    public Card() {
        super(new VerticalLayout());
        clip(false);
    }

    public Card elevated(boolean elevated) {
        this.elevated = elevated;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        if (elevated && shadowEnabled(context)) {
            context.draw().shadow(x(), y() + context.theme().shadowOffsetY(), width(), height(), radius,
                    context.theme().shadowBlur(), context.theme().shadow());
        }
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                elevated ? context.theme().surfaceElevated() : context.theme().surface());
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(),
                borderColor(context));
        super.render(context);
    }
}
