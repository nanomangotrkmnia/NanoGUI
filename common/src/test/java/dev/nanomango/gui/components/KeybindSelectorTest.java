package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.input.Keybind;
import dev.nanomango.gui.input.Keys;
import dev.nanomango.gui.input.Modifiers;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeybindSelectorTest {

    @Test
    @DisplayName("binding a modifier key does not add the key as its own modifier")
    void modifierKeyIsNotItsOwnModifier() {
        Keybind bind = new Keybind("Open", "NanoGUI", Keys.UNKNOWN);
        KeybindSelector selector = new KeybindSelector(bind);
        selector.startListening();

        // The shift bit is set because shift is physically held while the key
        // is pressed. It must not become part of the binding.
        selector.keyPressed(Keys.RIGHT_SHIFT, Modifiers.SHIFT);

        assertEquals(Keys.RIGHT_SHIFT, bind.keyCode());
        assertEquals(Modifiers.NONE, bind.modifiers());
        assertEquals("Right Shift", bind.displayName());
    }

    @Test
    @DisplayName("binding a regular key keeps the held modifiers")
    void regularKeyKeepsModifiers() {
        Keybind bind = new Keybind("Open", "NanoGUI", Keys.UNKNOWN);
        KeybindSelector selector = new KeybindSelector(bind);
        selector.startListening();

        selector.keyPressed(Keys.A, Modifiers.SHIFT);

        assertEquals(Keys.A, bind.keyCode());
        assertEquals(Modifiers.SHIFT, bind.modifiers());
        assertEquals("Shift + A", bind.displayName());
    }

    @Test
    @DisplayName("an old binding with a redundant modifier still displays once")
    void legacyBindingDisplaysOnce() {
        Keybind bind = new Keybind("Open", "NanoGUI", Keys.RIGHT_SHIFT, Modifiers.SHIFT);
        assertEquals("Right Shift", bind.displayName());
    }

    @Test
    @DisplayName("escape cancels the rebind")
    void escapeCancels() {
        GuiContext context = TestSupport.context();
        Keybind bind = new Keybind("Open", "NanoGUI", Keys.A);
        KeybindSelector selector = new KeybindSelector(bind);
        selector.setContext(context);
        selector.startListening();

        selector.keyPressed(Keys.ESCAPE, Modifiers.NONE);

        assertEquals(Keys.A, bind.keyCode());
    }
}
