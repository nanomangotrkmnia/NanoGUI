package dev.nanomango.gui.core;

import dev.nanomango.gui.component.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Layer that is rendered on top of the regular component tree.
 *
 * <p>Popups, dropdowns, context menus and tooltips register themselves here
 * while they are open so that they are never clipped or hidden by their
 * parent.</p>
 */
public final class OverlayLayer {

    private final List<Component> components = new ArrayList<>();

    public void add(Component component) {
        if (!components.contains(component)) {
            components.add(component);
        }
    }

    public void remove(Component component) {
        components.remove(component);
    }

    public void clear() {
        components.clear();
    }

    public List<Component> components() {
        return components;
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    public int size() {
        return components.size();
    }
}
