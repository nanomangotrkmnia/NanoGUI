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

    /**
     * Geometry of the magnifier: {@code x, y, extent, radius}. The box covers
     * the lens and the handle, so centring it centres what is actually drawn.
     */
    float[] iconBounds(GuiContext context) {
        float size = context.theme().iconSize() * 0.8F;
        float radius = size * 0.35F;
        float handle = size * 0.28F;
        float diagonal = (radius + handle) * 0.7071F;
        float extent = radius + diagonal;
        float boxX = x() + padding(context);
        float boxY = y() + (height() - extent) * 0.5F;
        return new float[]{boxX, boxY, extent, radius};
    }

    @Override
    protected float leftInset(GuiContext context) {
        // Room for the magnifier so that the placeholder and the text never
        // start underneath it. The gap matches the spacing used between the
        // other elements of a row.
        float[] icon = iconBounds(context);
        return icon[2] + context.theme().spacing() * 0.6F;
    }

    @Override
    public void render(GuiContext context) {
        super.render(context);

        // Magnifier icon drawn with two primitives: a ring plus a handle. The
        // ring sits at the top left of the ink box and the handle runs to the
        // bottom right, so the box is what gets centred in the field.
        float[] icon = iconBounds(context);
        float radius = icon[3];
        float cx = icon[0] + radius;
        float cy = icon[1] + radius;
        float diagonal = icon[2] - radius;
        int iconColor = Colors.withAlphaFactor(context.theme().textMuted(), 0.9F);
        context.draw().strokeRounded(cx - radius, cy - radius, radius * 2.0F, radius * 2.0F,
                radius, 1.0F, iconColor);
        context.draw().line(cx + radius * 0.7071F, cy + radius * 0.7071F,
                cx + diagonal, cy + diagonal, iconColor);

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
        // The clear button sits inside the field, so it has to be tested
        // before the field takes the click for itself.
        if (button == 0 && !text().isEmpty() && clearSize > 0.0F && isMouseOver(mouseX, mouseY)
                && mouseX >= clearX && mouseX <= clearX + clearSize
                && mouseY >= clearY && mouseY <= clearY + clearSize) {
            setText("");
            if (onSearch != null) {
                onSearch.accept("");
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
