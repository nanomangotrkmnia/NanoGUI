package dev.nanomango.gui.test;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontLoader;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.input.Clipboard;
import dev.nanomango.gui.input.InputState;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.theme.Theme;
import dev.nanomango.gui.theme.ThemeManager;

import java.awt.FontFormatException;
import java.io.IOException;

/**
 * Shared helpers for the unit tests.
 */
public final class TestSupport {

    private static Font cachedFont;

    private TestSupport() {
    }

    /**
     * Creates a context that is wired to a recording draw list.
     */
    public static GuiContext context(Font font) {
        Theme theme = new ThemeManager().current();
        InputState input = new InputState();
        DrawList draw = new DrawList();
        FontRegistry fonts = new FontRegistry();
        fonts.register(font);
        fonts.setDefaultFont(font);

        GuiContext context = new GuiContext(theme, input, draw, fonts);
        context.setClipboard(new Clipboard.Memory());
        context.setViewport(854, 480);
        context.setDeltaSeconds(1.0F / 60.0F);
        return context;
    }

    /**
     * Creates a context with the font used by the tests.
     */
    public static GuiContext context() {
        return context(testFont());
    }

    /**
     * Loads the bundled Inter font.
     *
     * <p>The loader transparently uses the native renderer when it is
     * available and the Java2D fallback otherwise, which keeps the tests
     * meaningful on every machine.</p>
     */
    public static synchronized Font testFont() {
        if (cachedFont == null) {
            try {
                cachedFont = FontLoader.create(0, "Test", FontLoader.INTER_REGULAR, 12.0F,
                        java.awt.Font.PLAIN);
            } catch (IOException exception) {
                throw new IllegalStateException("The bundled test font is missing", exception);
            }
        }
        return cachedFont;
    }

    /**
     * Loads the bundled Inter font without the native renderer.
     */
    public static Font fallbackFont() {
        return fallbackFont(12.0F);
    }

    /**
     * Loads the bundled Inter font without the native renderer.
     *
     * @param size the pixel size of the font
     */
    public static Font fallbackFont(float size) {
        try {
            return new dev.nanomango.gui.font.AwtFont(0, "TestFallback",
                    FontLoader.read(FontLoader.INTER_REGULAR), size, java.awt.Font.PLAIN);
        } catch (IOException | FontFormatException exception) {
            throw new IllegalStateException("The bundled test font is missing", exception);
        }
    }
}
