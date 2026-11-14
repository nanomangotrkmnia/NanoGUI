package dev.nanomango.gui.core;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.components.Button;
import dev.nanomango.gui.components.Text;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderBackend;
import dev.nanomango.gui.render.RenderContext;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiTest {

    private Gui gui;
    private RecordingBackend backend;
    private AtomicInteger openCount;
    private AtomicInteger closeCount;

    @BeforeEach
    void setUp() {
        gui = new Gui();
        backend = new RecordingBackend();
        openCount = new AtomicInteger();
        closeCount = new AtomicInteger();
        gui.host(new GuiHost() {
            @Override
            public void openGui() {
                openCount.incrementAndGet();
            }

            @Override
            public void closeGui() {
                closeCount.incrementAndGet();
            }
        });
        gui.fonts().register(TestSupport.testFont());
        gui.fonts().setDefaultFont(TestSupport.testFont());
    }

    @Test
    @DisplayName("opening and closing notifies the host")
    void openClose() {
        assertFalse(gui.isOpen());

        gui.open();
        assertTrue(gui.isOpen());
        assertEquals(1, openCount.get());
        assertEquals(0, closeCount.get());

        gui.close();
        assertFalse(gui.isOpen());
        assertEquals(1, closeCount.get());
    }

    @Test
    @DisplayName("opening twice only notifies the host once")
    void openIsIdempotent() {
        gui.open();
        gui.open();
        assertEquals(1, openCount.get());
    }

    @Test
    @DisplayName("closing while closed does nothing")
    void closeIsIdempotent() {
        gui.close();
        assertEquals(0, closeCount.get());
    }

    @Test
    @DisplayName("toggle flips the state")
    void toggle() {
        gui.toggle();
        assertTrue(gui.isOpen());
        gui.toggle();
        assertFalse(gui.isOpen());
    }

    @Test
    @DisplayName("input is ignored while the GUI is closed")
    void inputWhileClosed() {
        assertFalse(gui.mouseClicked(10, 10, 0));
        assertFalse(gui.keyPressed(65, 0));
        assertFalse(gui.charTyped('a'));
        assertFalse(gui.mouseScrolled(0, 0, 0, 1));
    }

    @Test
    @DisplayName("a click reaches the component under the mouse")
    void clickRouting() {
        AtomicInteger clicks = new AtomicInteger();
        Button button = new Button("Press me", clicks::incrementAndGet);
        gui.root().add(button);
        gui.open();

        // The layout is computed while rendering, so a frame has to be drawn
        // before the component knows where it is.
        gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);

        assertTrue(gui.mouseClicked(20, 20, 0));
        assertTrue(gui.mouseReleased(20, 20, 0));
        assertEquals(1, clicks.get());
    }

    @Test
    @DisplayName("a click outside of the component is not delivered")
    void clickOutside() {
        AtomicInteger clicks = new AtomicInteger();
        Button button = new Button("Press me", clicks::incrementAndGet);
        gui.root().add(button);
        gui.open();
        gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);

        gui.mouseClicked(500, 500, 0);
        gui.mouseReleased(500, 500, 0);
        assertEquals(0, clicks.get());
    }

    @Test
    @DisplayName("a disabled component does not react to clicks")
    void disabledComponent() {
        AtomicInteger clicks = new AtomicInteger();
        Button button = new Button("Press me", clicks::incrementAndGet);
        button.setEnabled(false);
        gui.root().add(button);
        gui.open();
        gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);

        gui.mouseClicked(20, 20, 0);
        gui.mouseReleased(20, 20, 0);
        assertEquals(0, clicks.get());
    }

    @Test
    @DisplayName("rendering produces draw commands")
    void render() {
        gui.root().add(new Text("Hello"));
        gui.open();
        gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);

        assertTrue(backend.frames > 0);
        assertTrue(backend.lastCommandCount > 0);
    }

    @Test
    @DisplayName("nothing is rendered while the GUI is closed")
    void renderWhileClosed() {
        gui.root().add(new Text("Hello"));
        gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);
        assertEquals(0, backend.frames);
    }

    @Test
    @DisplayName("the open animation converges towards one")
    void openAnimation() {
        gui.open();
        for (int i = 0; i < 120; i++) {
            gui.render(backend, 1.0F / 60.0F, 854, 480, 0, 0);
        }
        assertEquals(1.0F, gui.openProgress(), 0.001F);
    }

    @Test
    @DisplayName("the input state is reset when the GUI closes")
    void inputResetOnClose() {
        gui.open();
        gui.mouseClicked(10, 10, 0);
        gui.close();
        assertFalse(gui.input().isButtonPressed(0));
    }

    /**
     * Backend that records the frames it received.
     */
    private static final class RecordingBackend implements RenderBackend {

        int frames;
        int lastCommandCount;

        @Override
        public void beginFrame(RenderContext context) {
            frames++;
        }

        @Override
        public void render(RenderContext context, DrawList commands, FontRegistry fonts) {
            lastCommandCount = commands.commandCount();
        }

        @Override
        public void endFrame(RenderContext context) {
        }

        @Override
        public void builtin(int fontId, String text, float x, float y, int argb) {
        }

        @Override
        public void glyph(int fontId, int textureId,
                          float u0, float v0, float u1, float v1,
                          float x, float y, float width, float height, int argb) {
        }
    }
}
