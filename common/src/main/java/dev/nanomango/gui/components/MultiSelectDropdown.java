package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A drop down that allows selecting multiple options at once.
 */
public class MultiSelectDropdown extends Dropdown {

    private final Set<String> selectedOptions = new LinkedHashSet<>();
    private Consumer<Set<String>> onSelectionChange;
    private String summarySeparator = ", ";

    public MultiSelectDropdown() {
        super();
    }

    public MultiSelectDropdown(String label) {
        super(label);
    }

    public Set<String> selectedOptions() {
        return selectedOptions;
    }

    public MultiSelectDropdown onSelectionChange(Consumer<Set<String>> onSelectionChange) {
        this.onSelectionChange = onSelectionChange;
        return this;
    }

    /**
     * Toggles the selection state of an option.
     */
    public void toggle(String option) {
        if (selectedOptions.remove(option)) {
            // removed
        } else {
            selectedOptions.add(option);
        }
        if (onSelectionChange != null) {
            onSelectionChange.accept(selectedOptions);
        }
    }

    public boolean isSelected(String option) {
        return selectedOptions.contains(option);
    }

    @Override
    public void setSelectedIndex(int index) {
        // Selection is handled per option in a multi select dropdown.
    }

    @Override
    protected String displayText() {
        return summary();
    }

    @Override
    protected void renderPopup(GuiContext context) {
        super.renderPopup(context);
        // Draw the check marks on top of the entries.
        float row = popupRowHeight;
        for (int i = 0; i < popupVisibleCount && i < options().size(); i++) {
            String option = options().get(i);
            if (!selectedOptions.contains(option)) {
                continue;
            }
            float rowY = popupY + 2.0F + i * row;
            float boxSize = row * 0.4F;
            float boxX = x() + width() - padding(context) - boxSize;
            float boxY = rowY + (row - boxSize) * 0.5F;
            context.draw().fillRounded(boxX, boxY, boxSize, boxSize, context.theme().smallRadius(),
                    accentColor(context));
            context.draw().line(boxX + boxSize * 0.22F, boxY + boxSize * 0.52F,
                    boxX + boxSize * 0.42F, boxY + boxSize * 0.78F, context.theme().onAccent());
            context.draw().line(boxX + boxSize * 0.42F, boxY + boxSize * 0.78F,
                    boxX + boxSize * 0.80F, boxY + boxSize * 0.22F, context.theme().onAccent());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOpen() && button == 0) {
            float row = popupRowHeight;
            if (mouseY >= popupY && mouseY < popupY + popupVisibleCount * row + 4.0F
                    && mouseX >= x() && mouseX < x() + width()) {
                int index = (int) ((mouseY - popupY - 2.0F) / row);
                if (index >= 0 && index < popupVisibleCount && index < options().size()) {
                    toggle(options().get(index));
                    return true;
                }
            }
            setOpen(false);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Text shown in the closed state.
     */
    public String summary() {
        List<String> values = new ArrayList<>(selectedOptions);
        return String.join(summarySeparator, values);
    }

    /**
     * Colour used for the summary text.
     */
    public int summaryColor(GuiContext context) {
        return selectedOptions.isEmpty() ? context.theme().textMuted() : context.theme().text();
    }
}
