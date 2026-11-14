package dev.nanomango.gui.module;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.components.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleManagerTest {

    private ModuleManager manager;

    @BeforeEach
    void setUp() {
        manager = new ModuleManager();
    }

    @Test
    @DisplayName("a module can be registered and looked up")
    void register() {
        TestModule module = new TestModule("fps", "FPS");
        manager.register(module);

        assertEquals(1, manager.size());
        assertSame(module, manager.get("fps"));
        assertNull(manager.get("missing"));
    }

    @Test
    @DisplayName("registering the same id twice fails")
    void duplicateId() {
        manager.register(new TestModule("fps", "FPS"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.register(new TestModule("fps", "FPS again")));
        assertEquals(1, manager.size());
    }

    @Test
    @DisplayName("modules can be enabled and disabled")
    void enableDisable() {
        TestModule module = new TestModule("fps", "FPS");
        manager.register(module);

        assertTrue(manager.setEnabled("fps", true));
        assertTrue(module.isEnabled());

        assertTrue(manager.setEnabled("fps", false));
        assertFalse(module.isEnabled());

        assertFalse(manager.setEnabled("missing", true));
    }

    @Test
    @DisplayName("toggling flips the state and reports the new one")
    void toggle() {
        TestModule module = new TestModule("fps", "FPS");
        manager.register(module);

        assertTrue(manager.toggle("fps"));
        assertTrue(module.isEnabled());
        assertFalse(manager.toggle("fps"));
        assertFalse(module.isEnabled());

        assertFalse(manager.toggle("missing"));
    }

    @Test
    @DisplayName("only the enabled modules are listed")
    void enabledModules() {
        TestModule first = new TestModule("a", "A");
        TestModule second = new TestModule("b", "B");
        manager.register(first);
        manager.register(second);

        assertEquals(0, manager.enabled().size());
        first.setEnabled(true);
        assertEquals(1, manager.enabled().size());
        assertSame(first, manager.enabled().get(0));
    }

    @Test
    @DisplayName("the registration order is preserved")
    void order() {
        manager.register(new TestModule("c", "C"));
        manager.register(new TestModule("a", "A"));
        manager.register(new TestModule("b", "B"));

        assertEquals("c", manager.all().get(0).id());
        assertEquals("a", manager.all().get(1).id());
        assertEquals("b", manager.all().get(2).id());
    }

    @Test
    @DisplayName("a module builds its settings panel")
    void panel() {
        TestModule module = new TestModule("fps", "FPS");
        Component panel = module.createPanel();
        assertNotNull(panel);
        assertTrue(panel instanceof Text);
    }

    /**
     * Minimal module used by the tests.
     */
    private static final class TestModule implements Module {

        private final String id;
        private final String name;
        private boolean enabled;

        TestModule(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public Component createPanel() {
            return new Text(name);
        }
    }
}
