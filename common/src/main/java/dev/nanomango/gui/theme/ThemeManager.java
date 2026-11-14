package dev.nanomango.gui.theme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds every known theme and the currently active one.
 */
public final class ThemeManager {

    public static final String DARK_ID = "nanoarray.dark";
    public static final String LIGHT_ID = "nanoarray.light";

    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private Theme current;

    /**
     * Creates a theme manager with the built in dark and light themes.
     */
    public ThemeManager() {
        Theme dark = Theme.builder(DARK_ID, "NanoArray Dark")
                .dark(true)
                .build();
        Theme light = Theme.builder(LIGHT_ID, "NanoArray Light")
                .dark(false)
                .background(0xF2F4F7)
                .surface(0xFFFFFFFF)
                .surfaceVariant(0xFFEDEFF3)
                .surfaceElevated(0xFFFFFFFF)
                .overlay(0x66000000)
                .text(0xFF161A20)
                .textMuted(0xFF5A6472)
                .textDisabled(0xFF9AA3B2)
                .border(0xFFDDE1E8)
                .borderStrong(0xFFC3CAD6)
                .scrollbar(0xFFC3CAD6)
                .scrollbarHover(0xFFA9B3C2)
                .shadow(0x22000000)
                .selection(0x334C8DFF)
                .build();
        themes.put(dark.id(), dark);
        themes.put(light.id(), light);
        current = dark;
    }

    public void register(Theme theme) {
        themes.put(theme.id(), theme);
    }

    public Theme theme(String id) {
        return themes.get(id);
    }

    public List<Theme> all() {
        return new ArrayList<>(themes.values());
    }

    public Theme current() {
        return current;
    }

    /**
     * Switches to another theme.
     *
     * @return {@code true} when the theme exists and was activated
     */
    public boolean setTheme(String id) {
        Theme theme = themes.get(id);
        if (theme == null) {
            return false;
        }
        current = theme;
        return true;
    }

    /**
     * Creates a copy of the active theme with a different accent colour.
     *
     * <p>The copy is registered under a generated id so that the original
     * theme stays untouched.</p>
     */
    public Theme withAccent(int accent) {
        Theme base = current;
        Theme derived = Theme.Builder.copyOf(base)
                .id(base.id() + ".accent")
                .name(base.name() + " (Custom)")
                .accent(accent)
                .build();
        register(derived);
        current = derived;
        return derived;
    }

    /**
     * Replaces the fonts of every registered theme.
     *
     * <p>Themes are immutable, so every theme is rebuilt with the new fonts.
     * The identifiers and the order of the themes are preserved, which keeps
     * the stored theme selection valid.</p>
     *
     * @param regular the regular weight
     * @param medium  the medium weight
     * @param bold    the bold weight
     */
    public void setFonts(dev.nanomango.gui.font.Font regular,
                         dev.nanomango.gui.font.Font medium,
                         dev.nanomango.gui.font.Font bold) {
        Map<String, Theme> rebuilt = new LinkedHashMap<>();
        for (Theme theme : themes.values()) {
            Theme copy = Theme.Builder.copyOf(theme)
                    .fonts(regular, medium, bold)
                    .build();
            rebuilt.put(copy.id(), copy);
        }
        themes.clear();
        themes.putAll(rebuilt);

        if (current != null) {
            current = rebuilt.get(current.id());
            if (current == null) {
                current = themes.values().iterator().next();
            }
        }
    }
}
