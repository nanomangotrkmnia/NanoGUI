package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A drop down that shows a list of options when clicked.
 *
 * <pre>{@code
 * new Dropdown("Mode").options("Default", "Advanced", "Custom");
 * }</pre>
 */
public class Dropdown extends Component {

    private final List<String> options = new ArrayList<>();
    private String label = "";
    private int selectedIndex = -1;
    private boolean open;
    private Consumer<String> onChange;
    private int maxVisibleItems = 8;
    private float rowHeight = -1.0F;

    /** Popup geometry, refreshed every frame by {@link #renderPopup}. */
    protected float popupY;
    protected float popupRowHeight = 24.0F;
    protected int popupVisibleCount;

    public Dropdown() {
    }

    public Dropdown(String label) {
        this.label = label == null ? "" : label;
    }

    public String label() {
        return label;
    }

    public Dropdown label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public Dropdown options(String... values) {
        options.clear();
        for (String value : values) {
            options.add(value);
        }
        return this;
    }

    public Dropdown options(List<String> values) {
        options.clear();
        options.addAll(values);
        return this;
    }

    public List<String> options() {
        return options;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String selected() {
        return selectedIndex >= 0 && selectedIndex < options.size() ? options.get(selectedIndex) : null;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= options.size() || index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        if (onChange != null) {
            onChange.accept(options.get(index));
        }
    }

    public Dropdown onChange(Consumer<String> onChange) {
        this.onChange = onChange;
        return this;
    }

    public Dropdown maxVisibleItems(int maxVisibleItems) {
        this.maxVisibleItems = maxVisibleItems;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        if (this.open == open) {
            return;
        }
        this.open = open;
        if (open) {
            contextOverlayAdd();
        } else {
            contextOverlayRemove();
        }
    }

    private dev.nanomango.gui.core.OverlayLayer pendingOverlay;

    private void contextOverlayAdd() {
        if (context != null) {
            context.overlay().add(this);
        } else if (pendingOverlay != null) {
            pendingOverlay.add(this);
        }
    }

    private void contextOverlayRemove() {
        if (context != null) {
            context.overlay().remove(this);
        }
        if (pendingOverlay != null) {
            pendingOverlay.remove(this);
        }
    }

    /**
     * Called by the GUI when the component is attached to an overlay layer.
     */
    public void attachOverlay(dev.nanomango.gui.core.OverlayLayer overlay) {
        this.pendingOverlay = overlay;
        if (open) {
            overlay.add(this);
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        context.draw().fillRounded(x(), y(), width(), height(), radius,
                Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.5F));
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(),
                open ? accentColor(context) : borderColor(context));

        float textX = x() + padding(context);
        float textY = y() + (height() - context.font().height()) * 0.5F;

        String display = displayText();
        if (display == null || display.isEmpty()) {
            context.draw().text(textX, textY, context.font().id(), label, context.theme().textMuted());
        } else {
            context.draw().text(textX, textY, context.font().id(), display, textColor(context));
        }

        // Chevron.
        float arrowSize = context.theme().iconSize() * 0.45F;
        float arrowX = x() + width() - padding(context) - arrowSize;
        float arrowY = y() + (height() - arrowSize) * 0.5F;
        int arrowColor = context.theme().textMuted();
        context.draw().triangle(arrowX, arrowY, arrowX + arrowSize, arrowY, arrowX + arrowSize * 0.5F,
                arrowY + arrowSize, arrowColor);

        if (open) {
            // The popup has to be drawn by the overlay, otherwise the next
            // sibling in the panel paints over it. Re-add it here in case the
            // overlay was cleared while the GUI was closed.
            if (context.overlay() != null && !context.overlay().components().contains(this)) {
                context.overlay().add(this);
            }
            renderPopup(context);
        }
    }

    /**
     * Text shown in the closed state of the dropdown.
     */
    protected String displayText() {
        return selected() != null ? selected() : "";
    }

    protected void renderPopup(GuiContext context) {
        float row = rowHeight > 0.0F ? rowHeight : Math.max(context.theme().controlHeight(), context.font().height() + 8.0F);
        int visible = Math.min(options.size(), Math.max(1, maxVisibleItems));
        float popupWidth = width();
        float popupHeight = visible * row + 4.0F;
        float popupX = x();
        float targetY = y() + height() + 2.0F;

        // Keep the popup inside the viewport.
        float viewportHeight = context.viewportHeight();
        if (targetY + popupHeight > viewportHeight) {
            targetY = Math.max(0.0F, y() - popupHeight - 2.0F);
        }
        this.popupY = targetY;
        this.popupRowHeight = row;
        this.popupVisibleCount = visible;

        context.draw().shadow(popupX, popupY, popupWidth, popupHeight, radius(context),
                context.theme().shadowBlur(), context.theme().shadow());
        context.draw().fillRounded(popupX, popupY, popupWidth, popupHeight, radius(context),
                context.theme().surfaceElevated());
        context.draw().strokeRounded(popupX, popupY, popupWidth, popupHeight, radius(context),
                context.theme().borderWidth(), borderColor(context));

        for (int i = 0; i < visible; i++) {
            int index = i;
            float rowY = popupY + 2.0F + i * row;
            boolean hovered = context.mouseX() >= popupX && context.mouseX() < popupX + popupWidth
                    && context.mouseY() >= rowY && context.mouseY() < rowY + row;
            if (hovered) {
                context.draw().fillRounded(popupX + 2.0F, rowY, popupWidth - 4.0F, row, radius(context) * 0.6F,
                        context.theme().selection());
            }
            int color = index == selectedIndex ? accentColor(context) : textColor(context);
            float textY = rowY + (row - context.font().height()) * 0.5F;
            context.draw().text(popupX + padding(context), textY, context.font().id(), options.get(index), color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isEnabled()) {
            return false;
        }
        if (open) {
            float row = popupRowHeight;
            int visible = popupVisibleCount;
            float popupHeight = visible * row + 4.0F;
            if (mouseY >= popupY && mouseY < popupY + popupHeight
                    && mouseX >= x() && mouseX < x() + width()) {
                int index = (int) ((mouseY - popupY - 2.0F) / row);
                if (index >= 0 && index < visible) {
                    setSelectedIndex(index);
                    setOpen(false);
                    return true;
                }
            }
            setOpen(false);
            return true;
        }
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            setOpen(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        float widest = context.font().width(label);
        for (String option : options) {
            widest = Math.max(widest, context.font().width(option));
        }
        return Math.max(120.0F, widest + context.theme().padding() * 2.0F + context.theme().iconSize() * 2.0F);
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return context.theme().controlHeight();
    }

    /**
     * Convenience helper that clamps a raw scroll delta.
     */
    protected float clampScroll(float amount) {
        return MathUtil.clamp(amount, -100.0F, 100.0F);
    }
}
