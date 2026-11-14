package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.input.Keys;
import dev.nanomango.gui.input.Modifiers;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextBoxTest {

    private GuiContext context;

    @BeforeEach
    void setUp() {
        context = TestSupport.context();
    }

    @Test
    @DisplayName("typing inserts characters at the caret")
    void typing() {
        TextBox box = new TextBox();
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();

        assertTrue(box.charTyped('a'));
        assertTrue(box.charTyped('b'));
        assertEquals("ab", box.text());
        assertEquals(2, box.caret());
    }

    @Test
    @DisplayName("a text box that is not focused ignores the keyboard")
    void notFocused() {
        TextBox box = new TextBox();
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);

        assertFalse(box.charTyped('a'));
        assertEquals("", box.text());
    }

    @Test
    @DisplayName("the caret moves with the arrow keys")
    void caretMovement() {
        TextBox box = new TextBox("abc");
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();
        box.setCaret(3);

        assertTrue(box.keyPressed(Keys.LEFT, 0));
        assertEquals(2, box.caret());
        assertTrue(box.keyPressed(Keys.HOME, 0));
        assertEquals(0, box.caret());
        assertTrue(box.keyPressed(Keys.END, 0));
        assertEquals(3, box.caret());
    }

    @Test
    @DisplayName("backspace deletes the character before the caret")
    void backspace() {
        TextBox box = new TextBox("abc");
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();
        box.setCaret(3);

        assertTrue(box.keyPressed(Keys.BACKSPACE, 0));
        assertEquals("ab", box.text());
        assertEquals(2, box.caret());
    }

    @Test
    @DisplayName("control and a selects the whole text")
    void selectAll() {
        TextBox box = new TextBox("abc");
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();
        box.setCaret(0);

        assertTrue(box.keyPressed(Keys.A, Modifiers.CONTROL));
        assertTrue(box.keyPressed(Keys.C, Modifiers.CONTROL));
        assertEquals("abc", context.clipboard().contents());
    }

    @Test
    @DisplayName("control and v inserts the clipboard contents")
    void paste() {
        TextBox box = new TextBox();
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();

        context.clipboard().setContents("hello");
        assertTrue(box.keyPressed(Keys.V, Modifiers.CONTROL));
        assertEquals("hello", box.text());
    }

    @Test
    @DisplayName("control and x cuts the selected text")
    void cut() {
        TextBox box = new TextBox("hello");
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();
        box.setCaret(0);

        box.keyPressed(Keys.A, Modifiers.CONTROL);
        assertTrue(box.keyPressed(Keys.X, Modifiers.CONTROL));
        assertEquals("", box.text());
        assertEquals("hello", context.clipboard().contents());
    }

    @Test
    @DisplayName("the change listener receives every modification")
    void changeListener() {
        List<String> changes = new ArrayList<>();
        TextBox box = new TextBox();
        box.onChange(changes::add);
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();

        box.charTyped('x');
        assertEquals(List.of("x"), changes);
    }

    @Test
    @DisplayName("the maximum length is respected")
    void maxLength() {
        TextBox box = new TextBox();
        box.maxLength(3);
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();

        box.charTyped('a');
        box.charTyped('b');
        box.charTyped('c');
        box.charTyped('d');
        assertEquals("abc", box.text());
    }

    @Test
    @DisplayName("a password field masks its content")
    void password() {
        PasswordInput box = new PasswordInput("secret");
        assertTrue(box.text().equals("secret"));
        box.setBounds(0, 0, 100, 20);
        box.setContext(context);
        box.focus();
        assertTrue(box.isFocusable());
    }
}
