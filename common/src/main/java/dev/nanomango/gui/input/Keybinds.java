package dev.nanomango.gui.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of every keybind the framework knows about.
 */
public final class Keybinds {

    private final Map<String, Keybind> byName = new LinkedHashMap<>();

    /**
     * Registers a keybind.
     *
     * @throws IllegalArgumentException when the name is already taken
     */
    public Keybind register(Keybind keybind) {
        if (byName.containsKey(keybind.name())) {
            throw new IllegalArgumentException("A keybind named '" + keybind.name() + "' is already registered");
        }
        byName.put(keybind.name(), keybind);
        return keybind;
    }

    public Keybind register(String name, int keyCode) {
        return register(new Keybind(name, keyCode));
    }

    public Keybind get(String name) {
        return byName.get(name);
    }

    public boolean matches(String name, int keyCode, int modifiers) {
        Keybind keybind = byName.get(name);
        if (keybind == null) {
            return false;
        }
        return keybind.keyCode() == keyCode && (keybind.modifiers() & modifiers) == keybind.modifiers();
    }

    public List<Keybind> all() {
        return new ArrayList<>(byName.values());
    }

    public List<Keybind> inCategory(String category) {
        List<Keybind> result = new ArrayList<>();
        for (Keybind keybind : byName.values()) {
            if (keybind.category().equals(category)) {
                result.add(keybind);
            }
        }
        return result;
    }

    public int size() {
        return byName.size();
    }
}
