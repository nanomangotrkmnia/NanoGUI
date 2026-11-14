package dev.nanomango.gui.theme;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeManagerTest {

    private ThemeManager manager;

    @BeforeEach
    void setUp() {
        manager = new ThemeManager();
    }

    @Test
    @DisplayName("the dark theme is active by default")
    void defaultTheme() {
        assertEquals(ThemeManager.DARK_ID, manager.current().id());
        assertTrue(manager.current().isDark());
        assertEquals(2, manager.all().size());
    }

    @Test
    @DisplayName("switching to an unknown theme fails")
    void unknownTheme() {
        assertFalse(manager.setTheme("does.not.exist"));
        assertEquals(ThemeManager.DARK_ID, manager.current().id());

        assertTrue(manager.setTheme(ThemeManager.LIGHT_ID));
        assertEquals(ThemeManager.LIGHT_ID, manager.current().id());
        assertFalse(manager.current().isDark());
    }

    @Test
    @DisplayName("a custom accent keeps the rest of the theme")
    void customAccent() {
        Theme base = manager.current();
        Theme derived = manager.withAccent(0xFFFF0000);

        assertEquals(0xFFFF0000, derived.accent());
        assertEquals(base.id() + ".accent", derived.id());
        assertEquals(base.background(), derived.background());
        assertEquals(base.text(), derived.text());
        assertEquals(base.radius(), derived.radius());
        assertEquals(base.controlHeight(), derived.controlHeight());
        assertSame(derived, manager.current());
    }

    @Test
    @DisplayName("replacing the fonts rebuilds every theme")
    void setFonts() {
        Font regular = TestSupport.fallbackFont(11.0F);
        Font medium = TestSupport.fallbackFont(12.0F);
        Font bold = TestSupport.fallbackFont(13.0F);

        manager.setFonts(regular, medium, bold);

        for (Theme theme : manager.all()) {
            assertSame(regular, theme.regularFont());
            assertSame(medium, theme.mediumFont());
            assertSame(bold, theme.boldFont());
        }
        assertSame(regular, manager.current().regularFont());
    }

    @Test
    @DisplayName("the identifiers of the themes survive a font change")
    void setFontsKeepsIds() {
        manager.setTheme(ThemeManager.LIGHT_ID);
        manager.setFonts(TestSupport.fallbackFont(), TestSupport.fallbackFont(), TestSupport.fallbackFont());

        assertEquals(ThemeManager.LIGHT_ID, manager.current().id());
        assertTrue(manager.setTheme(ThemeManager.DARK_ID));
    }

    @Test
    @DisplayName("a copied theme keeps all of its properties")
    void copyOf() {
        Theme source = Theme.builder("custom.id", "Custom")
                .dark(false)
                .background(0x11223344)
                .accent(0xFF00FF00)
                .radius(12.0F)
                .padding(4.0F)
                .controlHeight(30.0F)
                .shadowEnabled(false)
                .build();

        Theme copy = Theme.Builder.copyOf(source).build();

        assertEquals(source.id(), copy.id());
        assertEquals(source.name(), copy.name());
        assertEquals(source.isDark(), copy.isDark());
        assertEquals(source.background(), copy.background());
        assertEquals(source.accent(), copy.accent());
        assertEquals(source.radius(), copy.radius());
        assertEquals(source.padding(), copy.padding());
        assertEquals(source.controlHeight(), copy.controlHeight());
        assertEquals(source.shadowEnabled(), copy.shadowEnabled());
        assertSame(source.regularFont(), copy.regularFont());
    }

    @Test
    @DisplayName("a copied theme can be renamed")
    void copyWithNewId() {
        Theme source = Theme.builder("original", "Original").build();
        Theme copy = Theme.Builder.copyOf(source)
                .id("derived")
                .name("Derived")
                .build();

        assertEquals("derived", copy.id());
        assertEquals("Derived", copy.name());
        assertEquals("original", source.id());
    }

    @Test
    @DisplayName("a theme without fonts falls back to the regular weight")
    void fontFallback() {
        Theme theme = Theme.builder("a", "A").build();
        assertNull(theme.regularFont());
        assertNull(theme.mediumFont());
        assertNull(theme.boldFont());
    }

    @Test
    @DisplayName("a theme with a regular font uses it for the other weights")
    void fontFallbackChain() {
        Font regular = TestSupport.fallbackFont();
        Theme theme = Theme.builder("a", "A").regularFont(regular).build();

        assertNotNull(theme.regularFont());
        assertSame(regular, theme.mediumFont());
        assertSame(regular, theme.boldFont());
    }
}
