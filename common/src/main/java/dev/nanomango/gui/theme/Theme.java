package dev.nanomango.gui.theme;

import dev.nanomango.gui.font.Font;

/**
 * Immutable visual description of the GUI.
 *
 * <p>Themes are created with a {@link ThemeBuilder}. Components never read
 * colours directly; they ask the active theme which keeps the look consistent
 * and makes re-theming possible at runtime.</p>
 */
public final class Theme {

    private final String id;
    private final String name;
    private final boolean dark;

    private final int background;
    private final int surface;
    private final int surfaceVariant;
    private final int surfaceElevated;
    private final int overlay;
    private final int accent;
    private final int accentHover;
    private final int accentPressed;
    private final int onAccent;
    private final int text;
    private final int textMuted;
    private final int textDisabled;
    private final int border;
    private final int borderStrong;
    private final int success;
    private final int warning;
    private final int danger;
    private final int scrollbar;
    private final int scrollbarHover;
    private final int shadow;
    private final int selection;

    private final float radius;
    private final float smallRadius;
    private final float spacing;
    private final float padding;
    private final float borderWidth;
    private final float iconSize;
    private final float scrollbarWidth;
    private final float controlHeight;
    private final float opacity;

    private final boolean shadowEnabled;
    private final float shadowBlur;
    private final float shadowOffsetY;

    private final Font regularFont;
    private final Font mediumFont;
    private final Font boldFont;

    Theme(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.dark = builder.dark;
        this.background = builder.background;
        this.surface = builder.surface;
        this.surfaceVariant = builder.surfaceVariant;
        this.surfaceElevated = builder.surfaceElevated;
        this.overlay = builder.overlay;
        this.accent = builder.accent;
        this.accentHover = builder.accentHover;
        this.accentPressed = builder.accentPressed;
        this.onAccent = builder.onAccent;
        this.text = builder.text;
        this.textMuted = builder.textMuted;
        this.textDisabled = builder.textDisabled;
        this.border = builder.border;
        this.borderStrong = builder.borderStrong;
        this.success = builder.success;
        this.warning = builder.warning;
        this.danger = builder.danger;
        this.scrollbar = builder.scrollbar;
        this.scrollbarHover = builder.scrollbarHover;
        this.shadow = builder.shadow;
        this.selection = builder.selection;
        this.radius = builder.radius;
        this.smallRadius = builder.smallRadius;
        this.spacing = builder.spacing;
        this.padding = builder.padding;
        this.borderWidth = builder.borderWidth;
        this.iconSize = builder.iconSize;
        this.scrollbarWidth = builder.scrollbarWidth;
        this.controlHeight = builder.controlHeight;
        this.opacity = builder.opacity;
        this.shadowEnabled = builder.shadowEnabled;
        this.shadowBlur = builder.shadowBlur;
        this.shadowOffsetY = builder.shadowOffsetY;
        this.regularFont = builder.regularFont;
        this.mediumFont = builder.mediumFont;
        this.boldFont = builder.boldFont;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isDark() {
        return dark;
    }

    public int background() {
        return background;
    }

    public int surface() {
        return surface;
    }

    public int surfaceVariant() {
        return surfaceVariant;
    }

    public int surfaceElevated() {
        return surfaceElevated;
    }

    public int overlay() {
        return overlay;
    }

    public int accent() {
        return accent;
    }

    public int accentHover() {
        return accentHover;
    }

    public int accentPressed() {
        return accentPressed;
    }

    public int onAccent() {
        return onAccent;
    }

    public int text() {
        return text;
    }

    public int textMuted() {
        return textMuted;
    }

    public int textDisabled() {
        return textDisabled;
    }

    public int border() {
        return border;
    }

    public int borderStrong() {
        return borderStrong;
    }

    public int success() {
        return success;
    }

    public int warning() {
        return warning;
    }

    public int danger() {
        return danger;
    }

    public int scrollbar() {
        return scrollbar;
    }

    public int scrollbarHover() {
        return scrollbarHover;
    }

    public int shadow() {
        return shadow;
    }

    public int selection() {
        return selection;
    }

    public float radius() {
        return radius;
    }

    public float smallRadius() {
        return smallRadius;
    }

    public float spacing() {
        return spacing;
    }

    public float padding() {
        return padding;
    }

    public float borderWidth() {
        return borderWidth;
    }

    public float iconSize() {
        return iconSize;
    }

    public float scrollbarWidth() {
        return scrollbarWidth;
    }

    public float controlHeight() {
        return controlHeight;
    }

    public float opacity() {
        return opacity;
    }

    public boolean shadowEnabled() {
        return shadowEnabled;
    }

    public float shadowBlur() {
        return shadowBlur;
    }

    public float shadowOffsetY() {
        return shadowOffsetY;
    }

    public Font regularFont() {
        return regularFont;
    }

    public Font mediumFont() {
        return mediumFont;
    }

    public Font boldFont() {
        return boldFont;
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /**
     * Fluent builder for {@link Theme} instances.
     */
    public static final class Builder {

        private String id;
        private String name;
        private boolean dark = true;

        private int background = 0xE6121418;
        private int surface = 0xF21A1D23;
        private int surfaceVariant = 0xF222262D;
        private int surfaceElevated = 0xF2272C35;
        private int overlay = 0x99000000;
        private int accent = 0xFF4C8DFF;
        private int accentHover = 0xFF6BA1FF;
        private int accentPressed = 0xFF3A76E0;
        private int onAccent = 0xFFFFFFFF;
        private int text = 0xFFE8EBF0;
        private int textMuted = 0xFF9AA3B2;
        private int textDisabled = 0xFF5C6472;
        private int border = 0xFF2B3038;
        private int borderStrong = 0xFF3A414C;
        private int success = 0xFF3DD68C;
        private int warning = 0xFFF5A524;
        private int danger = 0xFFF04438;
        private int scrollbar = 0xFF39404B;
        private int scrollbarHover = 0xFF4A525F;
        private int shadow = 0x66000000;
        private int selection = 0x334C8DFF;

        private float radius = 8.0F;
        private float smallRadius = 5.0F;
        private float spacing = 6.0F;
        private float padding = 10.0F;
        private float borderWidth = 1.0F;
        private float iconSize = 14.0F;
        private float scrollbarWidth = 5.0F;
        private float controlHeight = 24.0F;
        private float opacity = 1.0F;

        private boolean shadowEnabled = true;
        private float shadowBlur = 14.0F;
        private float shadowOffsetY = 4.0F;

        private Font regularFont;
        private Font mediumFont;
        private Font boldFont;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         * Creates a builder that starts with the values of an existing theme.
         *
         * <p>Every property, including the fonts, is copied so that the new
         * theme only differs in the properties that are changed afterwards.
         * This is used to re-theme a theme at runtime without losing its
         * configuration.</p>
         *
         * @param theme the theme to copy, never {@code null}
         * @return a builder pre-filled with the values of the theme
         */
        public static Builder copyOf(Theme theme) {
            Builder builder = new Builder(theme.id, theme.name);
            builder.dark = theme.dark;
            builder.background = theme.background;
            builder.surface = theme.surface;
            builder.surfaceVariant = theme.surfaceVariant;
            builder.surfaceElevated = theme.surfaceElevated;
            builder.overlay = theme.overlay;
            builder.accent = theme.accent;
            builder.accentHover = theme.accentHover;
            builder.accentPressed = theme.accentPressed;
            builder.onAccent = theme.onAccent;
            builder.text = theme.text;
            builder.textMuted = theme.textMuted;
            builder.textDisabled = theme.textDisabled;
            builder.border = theme.border;
            builder.borderStrong = theme.borderStrong;
            builder.success = theme.success;
            builder.warning = theme.warning;
            builder.danger = theme.danger;
            builder.scrollbar = theme.scrollbar;
            builder.scrollbarHover = theme.scrollbarHover;
            builder.shadow = theme.shadow;
            builder.selection = theme.selection;
            builder.radius = theme.radius;
            builder.smallRadius = theme.smallRadius;
            builder.spacing = theme.spacing;
            builder.padding = theme.padding;
            builder.borderWidth = theme.borderWidth;
            builder.iconSize = theme.iconSize;
            builder.scrollbarWidth = theme.scrollbarWidth;
            builder.controlHeight = theme.controlHeight;
            builder.opacity = theme.opacity;
            builder.shadowEnabled = theme.shadowEnabled;
            builder.shadowBlur = theme.shadowBlur;
            builder.shadowOffsetY = theme.shadowOffsetY;
            builder.regularFont = theme.regularFont;
            builder.mediumFont = theme.mediumFont;
            builder.boldFont = theme.boldFont;
            return builder;
        }

        public Builder dark(boolean dark) {
            this.dark = dark;
            return this;
        }

        /**
         * Changes the identifier of the theme.
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Changes the display name of the theme.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder background(int color) {
            this.background = color;
            return this;
        }

        public Builder surface(int color) {
            this.surface = color;
            return this;
        }

        public Builder surfaceVariant(int color) {
            this.surfaceVariant = color;
            return this;
        }

        public Builder surfaceElevated(int color) {
            this.surfaceElevated = color;
            return this;
        }

        public Builder overlay(int color) {
            this.overlay = color;
            return this;
        }

        public Builder accent(int color) {
            this.accent = color;
            return this;
        }

        public Builder accentHover(int color) {
            this.accentHover = color;
            return this;
        }

        public Builder accentPressed(int color) {
            this.accentPressed = color;
            return this;
        }

        public Builder onAccent(int color) {
            this.onAccent = color;
            return this;
        }

        public Builder text(int color) {
            this.text = color;
            return this;
        }

        public Builder textMuted(int color) {
            this.textMuted = color;
            return this;
        }

        public Builder textDisabled(int color) {
            this.textDisabled = color;
            return this;
        }

        public Builder border(int color) {
            this.border = color;
            return this;
        }

        public Builder borderStrong(int color) {
            this.borderStrong = color;
            return this;
        }

        public Builder success(int color) {
            this.success = color;
            return this;
        }

        public Builder warning(int color) {
            this.warning = color;
            return this;
        }

        public Builder danger(int color) {
            this.danger = color;
            return this;
        }

        public Builder scrollbar(int color) {
            this.scrollbar = color;
            return this;
        }

        public Builder scrollbarHover(int color) {
            this.scrollbarHover = color;
            return this;
        }

        public Builder shadow(int color) {
            this.shadow = color;
            return this;
        }

        public Builder selection(int color) {
            this.selection = color;
            return this;
        }

        public Builder radius(float radius) {
            this.radius = radius;
            this.smallRadius = Math.min(this.smallRadius, radius);
            return this;
        }

        public Builder smallRadius(float radius) {
            this.smallRadius = radius;
            return this;
        }

        public Builder spacing(float spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder padding(float padding) {
            this.padding = padding;
            return this;
        }

        public Builder borderWidth(float borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public Builder iconSize(float iconSize) {
            this.iconSize = iconSize;
            return this;
        }

        public Builder scrollbarWidth(float scrollbarWidth) {
            this.scrollbarWidth = scrollbarWidth;
            return this;
        }

        public Builder controlHeight(float controlHeight) {
            this.controlHeight = controlHeight;
            return this;
        }

        public Builder opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder shadowEnabled(boolean shadowEnabled) {
            this.shadowEnabled = shadowEnabled;
            return this;
        }

        public Builder shadowBlur(float shadowBlur) {
            this.shadowBlur = shadowBlur;
            return this;
        }

        public Builder shadowOffsetY(float shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            return this;
        }

        public Builder fonts(Font regular, Font medium, Font bold) {
            this.regularFont = regular;
            this.mediumFont = medium;
            this.boldFont = bold;
            return this;
        }

        public Builder regularFont(Font font) {
            this.regularFont = font;
            return this;
        }

        public Builder mediumFont(Font font) {
            this.mediumFont = font;
            return this;
        }

        public Builder boldFont(Font font) {
            this.boldFont = font;
            return this;
        }

        public Theme build() {
            if (mediumFont == null) {
                mediumFont = regularFont;
            }
            if (boldFont == null) {
                boldFont = mediumFont;
            }
            return new Theme(this);
        }
    }
}
