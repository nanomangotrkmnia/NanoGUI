package dev.nanomango.gui.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of every {@link Module} known to the client.
 */
public final class ModuleManager {

    private final Map<String, Module> modules = new LinkedHashMap<>();

    public Module register(Module module) {
        if (modules.containsKey(module.id())) {
            throw new IllegalArgumentException("A module with the id '" + module.id() + "' is already registered");
        }
        modules.put(module.id(), module);
        return module;
    }

    public Module get(String id) {
        return modules.get(id);
    }

    public List<Module> all() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> enabled() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules.values()) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    /**
     * Enables or disables a module.
     *
     * @return {@code true} when the module exists
     */
    public boolean setEnabled(String id, boolean enabled) {
        Module module = modules.get(id);
        if (module == null) {
            return false;
        }
        module.setEnabled(enabled);
        return true;
    }

    public boolean toggle(String id) {
        Module module = modules.get(id);
        if (module == null) {
            return false;
        }
        module.setEnabled(!module.isEnabled());
        return module.isEnabled();
    }

    public int size() {
        return modules.size();
    }
}
