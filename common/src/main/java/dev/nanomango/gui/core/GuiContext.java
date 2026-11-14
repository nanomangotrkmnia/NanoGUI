package dev.nanomango.gui.core;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.input.Clipboard;
import dev.nanomango.gui.input.InputState;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.theme.Theme;

/**
 * Everything a component needs to know about the current frame.
 *
 * <p>The context is created once per frame by the {@link Gui} and handed to
 * every component. It never contains a Minecraft type, which is what allows
 * the very same component code to run on every supported Minecraft version.</p>
 */
public final class GuiContext {

    private Theme theme;
    private final InputState input;
    private final DrawList draw;
    private final FontRegistry fonts;
    private Clipboard clipboard;
    private final OverlayLayer overlay = new OverlayLayer();
    private TextureAspectProvider textureAspectProvider;

    private float deltaSeconds;
    private double mouseX;
    private double mouseY;
    private int viewportWidth;
    private int viewportHeight;
    private float scale = 1.0F;
    private int textCursor;
    private int dragStartX;
    private int dragStartY;
    private boolean dragging;

    public GuiContext(Theme theme, InputState input, DrawList draw, FontRegistry fonts) {
        this.theme = theme;
        this.input = input;
        this.draw = draw;
        this.fonts = fonts;
    }

    public Theme theme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public InputState input() {
        return input;
    }

    public DrawList draw() {
        return draw;
    }

    public FontRegistry fonts() {
        return fonts;
    }

    /**
     * Clipboard used by text fields. May be {@code null} in which case the
     * framework falls back to an in-memory clipboard.
     */
    public Clipboard clipboard() {
        return clipboard;
    }

    public void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * Layer for popups and menus that must be drawn above the regular tree.
     */
    public OverlayLayer overlay() {
        return overlay;
    }

    /**
     * Aspect ratio of a texture registered with the backend, or {@code 0} when
     * the aspect ratio is unknown.
     */
    public float textureAspect(int textureId) {
        return textureAspectProvider == null ? 0.0F : textureAspectProvider.aspect(textureId);
    }

    public void setTextureAspectProvider(TextureAspectProvider provider) {
        this.textureAspectProvider = provider;
    }

    /**
     * Supplies the aspect ratio of backend textures.
     */
    @FunctionalInterface
    public interface TextureAspectProvider {
        float aspect(int textureId);
    }

    /**
     * Layer for popups and menus that must be drawn above the regular tree.
     */

    public float deltaSeconds() {
        return deltaSeconds;
    }

    public void setDeltaSeconds(float deltaSeconds) {
        this.deltaSeconds = deltaSeconds;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public void setMouse(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public int viewportWidth() {
        return viewportWidth;
    }

    public int viewportHeight() {
        return viewportHeight;
    }

    public void setViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public float scale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Position of the text caret inside the currently focused text field.
     */
    public int textCursor() {
        return textCursor;
    }

    public void setTextCursor(int textCursor) {
        this.textCursor = textCursor;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public int dragStartX() {
        return dragStartX;
    }

    public int dragStartY() {
        return dragStartY;
    }

    public void setDragStart(int x, int y) {
        this.dragStartX = x;
        this.dragStartY = y;
    }

    /**
     * Default font of the active theme.
     */
    public Font font() {
        return theme.regularFont() != null ? theme.regularFont() : fonts.defaultFont();
    }

    public Font mediumFont() {
        return theme.mediumFont() != null ? theme.mediumFont() : font();
    }

    public Font boldFont() {
        return theme.boldFont() != null ? theme.boldFont() : mediumFont();
    }
}
