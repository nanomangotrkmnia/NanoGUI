package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A right click menu with a list of entries.
 */
public class ContextMenu extends Container {

    private final List<Entry> entries = new ArrayList<>();
    private boolean visible;

    public ContextMenu() {
        super(new VerticalLayout(0.0F));
        clip(false);
    }

    /**
     * Adds a menu entry.
     */
    public Entry addEntry(String label, Runnable onSelect) {
        Entry entry = new Entry(label, onSelect);
        entries.add(entry);
        add(entry);
        return entry;
    }

    public Entry addSeparator() {
        Entry entry = new Entry(null, null);
        entries.add(entry);
        add(entry);
        return entry;
    }

    public void show() {
        visible = true;
        setVisible(true);
    }

    public void hide() {
        visible = false;
        setVisible(false);
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void render(GuiContext context) {
        float radius = context.theme().radius();
        context.draw().shadow(x(), y(), width(), height(), radius, context.theme().shadowBlur(),
                context.theme().shadow());
        context.draw().fillRounded(x(), y(), width(), height(), radius, context.theme().surfaceElevated());
        context.draw().strokeRounded(x(), y(), width(), height(), radius, context.theme().borderWidth(),
                context.theme().borderStrong());

        float cursor = y() + 2.0F;
        for (Entry entry : entries) {
            if (entry.isSeparator()) {
                context.draw().fillRect(x() + 6.0F, cursor + 4.0F, width() - 12.0F, 1.0F, context.theme().border());
                cursor += 9.0F;
                continue;
            }
            float entryHeight = context.theme().controlHeight();
            if (entry.hovered) {
                context.draw().fillRounded(x() + 2.0F, cursor, width() - 4.0F, entryHeight,
                        context.theme().smallRadius(), context.theme().selection());
            }
            float textY = cursor + (entryHeight - context.font().height()) * 0.5F;
            context.draw().text(x() + context.theme().padding(), textY, context.font().id(), entry.label(),
                    context.theme().text());
            cursor += entryHeight;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) {
            return false;
        }
        float cursor = y() + 2.0F;
        for (Entry entry : entries) {
            if (entry.isSeparator()) {
                cursor += 9.0F;
                continue;
            }
            float entryHeight = context.theme().controlHeight();
            if (mouseY >= cursor && mouseY < cursor + entryHeight && mouseX >= x() && mouseX < x() + width()) {
                if (entry.onSelect != null) {
                    entry.onSelect.run();
                }
                hide();
                return true;
            }
            cursor += entryHeight;
        }
        hide();
        return true;
    }

    /**
     * A single menu entry.
     */
    public static final class Entry extends Component {

        private final String label;
        private final Runnable onSelect;
        private boolean hovered;

        Entry(String label, Runnable onSelect) {
            this.label = label;
            this.onSelect = onSelect;
        }

        public String label() {
            return label;
        }

        public boolean isSeparator() {
            return label == null;
        }

        @Override
        public void render(dev.nanomango.gui.core.GuiContext context) {
            // The menu draws its entries itself.
        }
    }
}
