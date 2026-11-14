package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A list with a search field that filters its entries.
 */
public class SearchableList extends Container {

    private final List<Item> items = new ArrayList<>();
    private final List<Item> visibleItems = new ArrayList<>();
    private final SearchField searchField = new SearchField();
    private final Container listContainer;
    private Consumer<Item> onSelect;
    private Item selected;
    private String emptyMessage = "No matching entries";

    public SearchableList() {
        super(new VerticalLayout());
        listContainer = new Container(new VerticalLayout(2.0F));
        listContainer.scrollable(true);
        listContainer.clip(true);
        add(searchField);
        add(listContainer);
        clip(false);
    }

    public SearchableList emptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        return this;
    }

    public SearchableList onSelect(Consumer<Item> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    /**
     * Adds an entry to the list.
     */
    public Item addItem(String label, Object value) {
        Item item = new Item(label, value);
        items.add(item);
        item.setVisible(true);
        listContainer.add(item);
        return item;
    }

    public List<Item> items() {
        return items;
    }

    public List<Item> visibleItems() {
        return visibleItems;
    }

    public Item selected() {
        return selected;
    }

    /**
     * Applies a filter to the visible entries.
     */
    public void filter(Predicate<Item> predicate) {
        for (Item item : items) {
            item.setVisible(predicate.test(item));
        }
        refreshVisible();
    }

    private void refreshVisible() {
        visibleItems.clear();
        for (Item item : items) {
            if (item.isVisible()) {
                visibleItems.add(item);
            }
        }
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        String query = searchField.text().trim().toLowerCase(java.util.Locale.ROOT);
        for (Item item : items) {
            item.setVisible(query.isEmpty() || item.label().toLowerCase(java.util.Locale.ROOT).contains(query));
        }
        refreshVisible();
    }

    @Override
    public void render(GuiContext context) {
        super.render(context);
        if (visibleItems.isEmpty()) {
            float textY = listContainer.y() + context.theme().padding();
            context.draw().text(listContainer.x() + context.theme().padding(), textY, context.font().id(),
                    emptyMessage, context.theme().textDisabled());
        }
    }

    /**
     * A single list entry.
     */
    public class Item extends Component {

        private final String label;
        private final Object value;

        Item(String label, Object value) {
            this.label = label;
            this.value = value;
        }

        public String label() {
            return label;
        }

        public Object value() {
            return value;
        }

        @Override
        protected void onClick() {
            selected = this;
            if (onSelect != null) {
                onSelect.accept(this);
            }
        }

        @Override
        public void render(GuiContext context) {
            float radius = context.theme().smallRadius();
            if (this == selected || isHovered()) {
                context.draw().fillRounded(x(), y(), width(), height(), radius, context.theme().selection());
            }
            float textY = y() + (height() - context.font().height()) * 0.5F;
            context.draw().text(x() + context.theme().padding(), textY, context.font().id(), label,
                    textColor(context));
        }

        @Override
        public float preferredWidth(GuiContext context) {
            return 160.0F;
        }

        @Override
        public float preferredHeight(GuiContext context) {
            return Math.max(context.theme().controlHeight(), context.font().height() + 6.0F);
        }
    }
}
