package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * A thin horizontal or vertical line used to group content.
 */
public class Separator extends Component {

    private boolean vertical;
    private int color = -1;

    public Separator() {
    }

    public Separator vertical() {
        this.vertical = true;
        return this;
    }

    public Separator color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void render(GuiContext context) {
        int effective = color >= 0 ? color : context.theme().border();
        if (vertical) {
            context.draw().fillRect(x() + width() * 0.5F - 0.5F, y(), 1.0F, height(), effective);
        } else {
            context.draw().fillRect(x(), y() + height() * 0.5F - 0.5F, width(), 1.0F, effective);
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return vertical ? 1.0F : 100.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return vertical ? 100.0F : 1.0F;
    }
}
