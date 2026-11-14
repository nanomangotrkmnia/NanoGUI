package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.Layout;
import dev.nanomango.gui.util.Colors;

import java.util.ArrayList;
import java.util.List;

/**
 * A tab bar with one content container per tab.
 *
 * <pre>{@code
 * Tabs tabs = new Tabs();
 * tabs.addTab("General", new Text("General settings"));
 * tabs.addTab("Advanced", new Text("Advanced settings"));
 * }</pre>
 */
public class Tabs extends Container {

    private final List<String> titles = new ArrayList<>();
    private final List<Container> contents = new ArrayList<>();
    private int selectedIndex;
    private float indicatorOffset;
    private float indicatorProgress = -1.0F;

    /** Tab hit boxes, refreshed every frame. */
    private final List<float[]> tabBounds = new ArrayList<>();

    /** Height of the tab bar, refreshed every frame by {@link #render}. */
    private float headerHeight;

    public Tabs() {
        super(new dev.nanomango.gui.layout.StackLayout());
        clip(false);
    }

    /**
     * Adds a tab and returns the container that holds its content.
     */
    public Container addTab(String title, Component... content) {
        Container panel = new Container(new dev.nanomango.gui.layout.VerticalLayout(context0Spacing()));
        panel.clip(false);
        for (Component component : content) {
            panel.add(component);
        }
        titles.add(title);
        contents.add(panel);
        add(panel);
        panel.setVisible(titles.size() == 1);
        return panel;
    }

    private float context0Spacing() {
        return 4.0F;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String selectedTitle() {
        return titles.isEmpty() ? null : titles.get(selectedIndex);
    }

    public void select(int index) {
        if (index < 0 || index >= titles.size() || index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        for (int i = 0; i < contents.size(); i++) {
            contents.get(i).setVisible(i == index);
        }
    }

    public List<String> titles() {
        return titles;
    }

    @Override
    public void render(GuiContext context) {
        float tabHeight = context.theme().controlHeight() + 4.0F;
        this.headerHeight = tabHeight;
        float cursor = x();
        float indicatorX = x();
        float indicatorWidth = 0.0F;
        tabBounds.clear();

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            float textWidth = context.font().width(title);
            float tabWidth = textWidth + context.theme().padding() * 2.0F;
            boolean active = i == selectedIndex;

            if (active) {
                indicatorX = cursor;
                indicatorWidth = tabWidth;
            }

            tabBounds.add(new float[]{cursor, tabWidth});

            int color = active ? context.theme().text() : context.theme().textMuted();
            float textY = y() + (tabHeight - context.font().height()) * 0.5F;
            context.draw().text(cursor + context.theme().padding(), textY, context.font().id(), title, color);
            cursor += tabWidth;
        }

        // Sliding indicator below the active tab.
        if (indicatorProgress < 0.0F) {
            indicatorProgress = indicatorX;
            indicatorOffset = indicatorX;
        }
        float speed = Math.min(1.0F, context.deltaSeconds() * 14.0F);
        indicatorOffset += (indicatorX - indicatorOffset) * speed;
        indicatorProgress = indicatorOffset;

        context.draw().fillRounded(indicatorProgress, y() + tabHeight - 2.0F, indicatorWidth, 2.0F, 1.0F,
                accentColor(context));

        // Content area.
        float contentY = y() + tabHeight + context.theme().spacing();
        for (int i = 0; i < contents.size(); i++) {
            Container content = contents.get(i);
            if (content.isVisible()) {
                content.setBounds(x(), contentY, width(), Math.max(0.0F, height() - (contentY - y())));
                content.render(context);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isEnabled()) {
            return false;
        }
        // The tab bar only reacts to the primary button.
        if (button == 0 && mouseY >= y() && mouseY < y() + headerHeight) {
            for (int i = 0; i < tabBounds.size(); i++) {
                float[] bounds = tabBounds.get(i);
                if (mouseX >= bounds[0] && mouseX < bounds[0] + bounds[1]) {
                    select(i);
                    return true;
                }
            }
            // The click was inside the tab bar but not on a tab, so it is
            // consumed but does not change the selection.
            return true;
        }
        for (int i = 0; i < contents.size(); i++) {
            Container content = contents.get(i);
            if (content.isVisible() && content.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }
}
