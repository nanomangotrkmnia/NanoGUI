package dev.nanomango.gui.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrawListTest {

    private DrawList list;

    @BeforeEach
    void setUp() {
        list = new DrawList();
    }

    @Test
    @DisplayName("a fresh draw list is empty")
    void empty() {
        assertEquals(0, list.commandCount());
        assertEquals(0, list.clipDepth());
    }

    @Test
    @DisplayName("clearing the list resets it")
    void clear() {
        list.fillRect(0.0F, 0.0F, 10.0F, 10.0F, 0xFFFFFFFF);
        list.clipPush(0.0F, 0.0F, 10.0F, 10.0F);
        assertEquals(2, list.commandCount());

        list.clear();
        assertEquals(0, list.commandCount());
        assertEquals(0, list.clipDepth());
    }

    @Test
    @DisplayName("the commands are replayed in the order they were recorded")
    void order() {
        list.fillRect(1.0F, 2.0F, 3.0F, 4.0F, 0xFF112233);
        list.fillRounded(5.0F, 6.0F, 7.0F, 8.0F, 9.0F, 0xFF445566);
        list.text(10.0F, 11.0F, 3, "Label", 0xFF778899);

        assertEquals(3, list.commandCount());
        assertEquals(DrawList.OP_FILL_RECT, list.opcodes(0));
        assertEquals(DrawList.OP_FILL_ROUNDED, list.opcodes(1));
        assertEquals(DrawList.OP_TEXT, list.opcodes(2));
    }

    @Test
    @DisplayName("the arguments of a rectangle are stored in order")
    void rectangleArguments() {
        list.fillRect(1.0F, 2.0F, 3.0F, 4.0F, 0xFF112233);

        assertEquals(1.0F, list.floatAt(0));
        assertEquals(2.0F, list.floatAt(1));
        assertEquals(3.0F, list.floatAt(2));
        assertEquals(4.0F, list.floatAt(3));
        assertEquals(0xFF112233, list.intAt(0));
    }

    @Test
    @DisplayName("a text command keeps its string")
    void textArguments() {
        list.text(10.0F, 11.0F, 7, "Hello", 0xFFFFFFFF);

        assertEquals(10.0F, list.floatAt(0));
        assertEquals(11.0F, list.floatAt(1));
        assertEquals(7, list.intAt(0));
        assertEquals(0xFFFFFFFF, list.intAt(1));
        assertEquals("Hello", list.stringAt(list.intAt(2)));
    }

    @Test
    @DisplayName("equal strings share one pool entry")
    void stringPool() {
        list.text(0.0F, 0.0F, 0, "Repeated", 0xFFFFFFFF);
        list.text(0.0F, 0.0F, 0, "Repeated", 0xFFFFFFFF);
        list.text(0.0F, 0.0F, 0, "Other", 0xFFFFFFFF);

        assertEquals(0, list.intAt(2));
        assertEquals(0, list.intAt(5));
        assertEquals(1, list.intAt(8));
    }

    @Test
    @DisplayName("an empty text is not recorded")
    void emptyText() {
        list.text(0.0F, 0.0F, 0, "", 0xFFFFFFFF);
        list.text(0.0F, 0.0F, 0, null, 0xFFFFFFFF);
        assertEquals(0, list.commandCount());
    }

    @Test
    @DisplayName("the clipping stack is balanced")
    void clipStack() {
        list.clipPush(0.0F, 0.0F, 100.0F, 100.0F);
        assertEquals(1, list.clipDepth());

        list.clipPush(10.0F, 10.0F, 50.0F, 50.0F);
        assertEquals(2, list.clipDepth());

        list.clipPop();
        list.clipPop();
        assertEquals(0, list.clipDepth());
    }

    @Test
    @DisplayName("an unbalanced clip pop is ignored")
    void unbalancedClipPop() {
        list.clipPop();
        assertEquals(0, list.commandCount());
        assertEquals(0, list.clipDepth());
    }

    @Test
    @DisplayName("the list grows beyond its initial capacity")
    void growth() {
        for (int i = 0; i < 5000; i++) {
            list.fillRect(i, i, i + 1.0F, i + 1.0F, 0xFFFFFFFF);
        }
        assertEquals(5000, list.commandCount());
        assertEquals(4999.0F, list.floatAt(4999 * 4));
    }

    @Test
    @DisplayName("every opcode knows how many arguments it has")
    void argumentCounts() {
        assertEquals(4, DrawList.floatArgCount(DrawList.OP_FILL_RECT));
        assertEquals(1, DrawList.intArgCount(DrawList.OP_FILL_RECT));
        assertEquals(5, DrawList.floatArgCount(DrawList.OP_FILL_ROUNDED));
        assertEquals(1, DrawList.intArgCount(DrawList.OP_FILL_ROUNDED));
        assertEquals(2, DrawList.floatArgCount(DrawList.OP_TEXT));
        assertEquals(3, DrawList.intArgCount(DrawList.OP_TEXT));
        assertEquals(8, DrawList.floatArgCount(DrawList.OP_TEXTURE));
        assertEquals(2, DrawList.intArgCount(DrawList.OP_TEXTURE));
        assertEquals(4, DrawList.floatArgCount(DrawList.OP_CLIP_PUSH));
        assertEquals(0, DrawList.intArgCount(DrawList.OP_CLIP_PUSH));
        assertEquals(0, DrawList.floatArgCount(DrawList.OP_CLIP_POP));
        assertEquals(0, DrawList.intArgCount(DrawList.OP_CLIP_POP));
    }

    @Test
    @DisplayName("every command of the list can be walked by the backends")
    void cursorWalk() {
        list.fillRect(0.0F, 0.0F, 1.0F, 1.0F, 0xFFFFFFFF);
        list.strokeRounded(0.0F, 0.0F, 1.0F, 1.0F, 2.0F, 3.0F, 0xFF000000);
        list.line(0.0F, 0.0F, 1.0F, 1.0F, 0xFF00FF00);
        list.arc(0.0F, 0.0F, 5.0F, 0.0F, 1.0F, 2.0F, 0xFF00FFFF);
        list.clipPush(0.0F, 0.0F, 10.0F, 10.0F);
        list.clipPop();

        int opCursor = 0;
        int floatCursor = 0;
        int intCursor = 0;
        int commands = 0;
        while (opCursor < list.commandCount()) {
            int opcode = list.opcodes(opCursor++);
            floatCursor += DrawList.floatArgCount(opcode);
            intCursor += DrawList.intArgCount(opcode);
            commands++;
        }
        assertEquals(list.commandCount(), commands);
        assertTrue(floatCursor > 0);
        assertTrue(intCursor > 0);
    }
}
