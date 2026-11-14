package dev.nanomango.gui.theme;

import dev.nanomango.gui.font.Font;

/**
 * Per component style overrides.
 *
 * <p>Every value is optional. When a value is {@code null} the component
 * falls back to the active theme. This keeps the theme the single source of
 * truth while still allowing individual widgets to look different.</p>
 */
public final class ComponentStyle {

    private Integer background;
    private Integer backgroundHover;
    private Integer border;
    private Integer text;
    private Integer accent;
    private Float radius;
    private Float padding;
    private Float opacity;
    private Font font;
    private Boolean shadow;

    public ComponentStyle() {
    }

    public ComponentStyle(Theme theme) {
        this.background = theme.surface();
        this.text = theme.text();
        this.radius = theme.radius();
        this.padding = theme.padding();
    }

    public ComponentStyle background(Integer color) {
        this.background = color;
        return this;
    }

    public ComponentStyle backgroundHover(Integer color) {
        this.backgroundHover = color;
        return this;
    }

    public ComponentStyle border(Integer color) {
        this.border = color;
        return this;
    }

    public ComponentStyle text(Integer color) {
        this.text = color;
        return this;
    }

    public ComponentStyle accent(Integer color) {
        this.accent = color;
        return this;
    }

    public ComponentStyle radius(Float radius) {
        this.radius = radius;
        return this;
    }

    public ComponentStyle padding(Float padding) {
        this.padding = padding;
        return this;
    }

    public ComponentStyle opacity(Float opacity) {
        this.opacity = opacity;
        return this;
    }

    public ComponentStyle font(Font font) {
        this.font = font;
        return this;
    }

    public ComponentStyle shadow(Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Integer background() {
        return background;
    }

    public Integer backgroundHover() {
        return backgroundHover;
    }

    public Integer border() {
        return border;
    }

    public Integer text() {
        return text;
    }

    public Integer accent() {
        return accent;
    }

    public Float radius() {
        return radius;
    }

    public Float padding() {
        return padding;
    }

    public Float opacity() {
        return opacity;
    }

    public Font font() {
        return font;
    }

    public Boolean shadow() {
        return shadow;
    }
}
