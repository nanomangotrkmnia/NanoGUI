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
        Theme derived = Theme.builder(base.id() + ".accent", base.name() + " (Custom)")
                .dark(base.isDark())
                .background(base.background())
                .surface(base.surface())
                .surfaceVariant(base.surfaceVariant())
                .surfaceElevated(base.surfaceElevated())
                .overlay(base.overlay())
                .accent(accent)
                .onAccent(base.onAccent())
                .text(base.text())
                .textMuted(base.textMuted())
                .textDisabled(base.textDisabled())
                .border(base.border())
                .borderStrong(base.borderStrong())
                .success(base.success())
                .warning(base.warning())
                .danger(base.danger())
                .scrollbar(base.scrollbar())
                .scrollbarHover(base.scrollbarHover())
                .shadow(base.shadow())
                .selection(base.selection())
                .radius(base.radius())
                .smallRadius(base.smallRadius())
                .spacing(base.spacing())
                .padding(base.padding())
                .borderWidth(base.borderWidth())
                .iconSize(base.iconSize())
                .scrollbarWidth(base.scrollbarWidth())
                .controlHeight(base.controlHeight())
                .opacity(base.opacity())
                .shadowEnabled(base.shadowEnabled())
                .shadowBlur(base.shadowBlur())
                .shadowOffsetY(base.shadowOffsetY())
                .fonts(base.regularFont(), base.mediumFont(), base.boldFont())
                .build();
        register(derived);
        current = derived;
        return derived;
    }
}
