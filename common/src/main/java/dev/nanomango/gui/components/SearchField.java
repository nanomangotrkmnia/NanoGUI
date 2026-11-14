package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;

import java.util.function.Consumer;

/**
 * A text field with a search icon and a clear button.
 */
public class SearchField extends TextBox {

    private String emptyMessage = "No results";
    private Consumer<String> onSearch;

    /** Hit box of the clear button, refreshed every frame. */
    private float clearX;
    private float clearY;
    private float clearSize;

    public SearchField() {
        super();
        placeholder("Search...");
    }

    public SearchField(String text) {
        super(text);
        placeholder("Search...");
    }

    public SearchField onSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
        return this;
    }

    public SearchField emptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public String emptyMessage() {
        return emptyMessage;
    }

    @Override
    protected float leftInset(GuiContext context) {
        // Room for the magnifier so that the placeholder and the text never
        // start underneath it.
        float size = context.theme().iconSize() * 0.8F;
        return size * 0.9F + context.theme().spacing() * 0.5F;
    }

    @Override
    public void render(GuiContext context) {
        super.render(context);

        // Magnifier icon drawn with two primitives: a ring plus a handle.
        float size = context.theme().iconSize() * 0.8F;
        float cx = x() + padding(context) + size * 0.4F;
        float cy = y() + height() * 0.5F - size * 0.15F;
        int iconColor = Colors.withAlphaFactor(context.theme().textMuted(), 0.9F);
        context.draw().strokeRounded(cx - size * 0.35F, cy - size * 0.35F, size * 0.7F, size * 0.7F,
                size * 0.35F, 1.0F, iconColor);
        context.draw().line(cx + size * 0.22F, cy + size * 0.22F, cx + size * 0.5F, cy + size * 0.5F, iconColor);

        clearSize = context.theme().iconSize() * 0.55F;
        clearX = x() + width() - padding(context) - clearSize;
        clearY = y() + (height() - clearSize) * 0.5F;
        if (!text().isEmpty()) {
            int clearColor = context.theme().textMuted();
            context.draw().line(clearX, clearY, clearX + clearSize, clearY + clearSize, clearColor);
            context.draw().line(clearX + clearSize, clearY, clearX, clearY + clearSize, clearColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (!text().isEmpty() && isMouseOver(mouseX, mouseY) && clearSize > 0.0F) {
            if (mouseX >= clearX && mouseX <= clearX + clearSize
                    && mouseY >= clearY && mouseY <= clearY + clearSize) {
                setText("");
                if (onSearch != null) {
                    onSearch.accept("");
                }
                return true;
            }
        }
        return false;
    }
}
