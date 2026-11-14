package dev.nanomango.gui.font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of every font known to the GUI.
 *
 * <p>Fonts are addressed by a small integer id which the render backends use
 * to resolve the matching host side resource without a map lookup on the hot
 * path.</p>
 */
public final class FontRegistry {

    private final List<Font> fonts = new ArrayList<>();
    private final Map<String, Font> byName = new HashMap<>();
    private final Map<Integer, Font> byId = new HashMap<>();
    private int defaultFontId = -1;

    /**
     * Registers a font and returns its id.
     *
     * @throws IllegalArgumentException if a font with the same name exists
     */
    public int register(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("The font must not be null");
        }
        if (byName.containsKey(font.name())) {
            throw new IllegalArgumentException("A font named '" + font.name() + "' is already registered");
        }
        fonts.add(font);
        byName.put(font.name(), font);
        byId.put(font.id(), font);
        return font.id();
    }

    public Font registerAndGet(Font font) {
        register(font);
        return font;
    }

    /**
     * Looks a font up by its id.
     *
     * @throws IllegalArgumentException if no font with that id exists
     */
    public Font byId(int id) {
        Font font = byId.get(id);
        if (font == null) {
            throw new IllegalArgumentException("Unknown font id " + id);
        }
        return font;
    }

    /**
     * Looks a font up by its id without throwing.
     */
    public Font findById(int id) {
        return byId.get(id);
    }

    public Font byName(String name) {
        return byName.get(name);
    }

    public int idOf(Font font) {
        return font.id();
    }

    public List<Font> all() {
        return fonts;
    }

    public int size() {
        return fonts.size();
    }

    /**
     * Id of the font used when a component does not request a specific one.
     *
     * @throws IllegalStateException if no font has been registered
     */
    public int defaultFontId() {
        if (defaultFontId < 0) {
            throw new IllegalStateException("No default font has been registered");
        }
        return defaultFontId;
    }

    /**
     * Sets the default font. The font is registered when it is not known yet.
     */
    public void setDefaultFont(Font font) {
        if (!byName.containsKey(font.name())) {
            register(font);
        }
        this.defaultFontId = font.id();
    }

    public Font defaultFont() {
        return byId(defaultFontId());
    }

    public boolean hasDefaultFont() {
        return defaultFontId >= 0 && byId.containsKey(defaultFontId);
    }

    /**
     * Removes every registered font.
     */
    public void clear() {
        fonts.clear();
        byName.clear();
        byId.clear();
        defaultFontId = -1;
    }
}
