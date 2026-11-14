package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.util.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A group of mutually exclusive radio buttons.
 *
 * <p>The group owns the selected value and hands out {@link Option} buttons
 * that report back when they are clicked.</p>
 */
public class RadioGroup extends Container {

    private final List<Option> options = new ArrayList<>();
    private int selectedIndex = -1;
    private IntConsumer onChange;

    public RadioGroup() {
        layout(new dev.nanomango.gui.layout.VerticalLayout(2.0F));
        clip(false);
    }

    public RadioGroup onChange(IntConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    /**
     * Adds an option and returns the created button.
     */
    public Option addOption(String label) {
        Option option = new Option(label, options.size());
        options.add(option);
        add(option);
        return option;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String selectedLabel() {
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            return null;
        }
        return options.get(selectedIndex).label();
    }

    public void setSelectedIndex(int index) {
        if (index == selectedIndex) {
            return;
        }
        if (index < 0 || index >= options.size()) {
            return;
        }
        selectedIndex = index;
        for (Option option : options) {
            option.setSelected(option.index() == index);
        }
        if (onChange != null) {
            onChange.accept(index);
        }
    }

    /**
     * A single radio button.
     */
    public class Option extends Component {

        private final String label;
        private final int index;
        private boolean selected;

        Option(String label, int index) {
            this.label = label;
            this.index = index;
        }

        public int index() {
            return index;
        }

        public String label() {
            return label;
        }

        public boolean isSelected() {
            return selected;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        protected void onClick() {
            setSelectedIndex(index);
        }

        private Font resolvedFont(GuiContext context) {
            return context.theme().regularFont() != null ? context.theme().regularFont() : context.font();
        }

        @Override
        public void render(GuiContext context) {
            float radius = context.theme().controlHeight() * 0.28F;
            float centerX = x() + radius;
            float centerY = y() + height() * 0.5F;

            if (selected) {
                context.draw().fillRounded(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F,
                        radius, accentColor(context));
                float inner = radius * 0.5F;
                context.draw().fillRounded(centerX - inner, centerY - inner, inner * 2.0F, inner * 2.0F,
                        inner, context.theme().onAccent());
            } else {
                context.draw().fillRounded(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F,
                        radius, Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.5F));
                context.draw().strokeRounded(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F,
                        radius, context.theme().borderWidth(), context.theme().borderStrong());
            }

            Font font = resolvedFont(context);
            float textY = y() + (height() - font.height()) * 0.5F;
            context.draw().text(x() + radius * 2.0F + context.theme().spacing(), textY, font.id(), label,
                    textColor(context));
        }

        @Override
        public float preferredWidth(GuiContext context) {
            float radius = context.theme().controlHeight() * 0.28F;
            return radius * 2.0F + context.theme().spacing() + resolvedFont(context).width(label);
        }

        @Override
        public float preferredHeight(GuiContext context) {
            return Math.max(context.theme().controlHeight() * 0.7F, resolvedFont(context).height());
        }
    }
}
