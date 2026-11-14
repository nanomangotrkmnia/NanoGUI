package dev.nanomango.gui.render;

/**
 * Per frame information handed to a {@link RenderBackend}.
 */
public final class RenderContext {

    private int viewportWidth;
    private int viewportHeight;
    private float scale = 1.0F;
    private double mouseX;
    private double mouseY;
    private float deltaSeconds;
    private boolean unicodeFonts;

    public int viewportWidth() {
        return viewportWidth;
    }

    public int viewportHeight() {
        return viewportHeight;
    }

    public float scale() {
        return scale;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public float deltaSeconds() {
        return deltaSeconds;
    }

    public boolean unicodeFonts() {
        return unicodeFonts;
    }

    public RenderContext setViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        return this;
    }

    public RenderContext setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public RenderContext setMouse(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
        return this;
    }

    public RenderContext setDeltaSeconds(float deltaSeconds) {
        this.deltaSeconds = deltaSeconds;
        return this;
    }

    public RenderContext setUnicodeFonts(boolean unicodeFonts) {
        this.unicodeFonts = unicodeFonts;
        return this;
    }
}
