package dev.nanomango.gui.component;

import dev.nanomango.gui.animation.Animation;
import dev.nanomango.gui.animation.Easing;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.input.Keys;
import dev.nanomango.gui.theme.ComponentStyle;
import dev.nanomango.gui.theme.Theme;
import dev.nanomango.gui.util.MathUtil;
import dev.nanomango.gui.util.Rect;

/**
 * Base class of every element in the GUI.
 *
 * <p>Components are organised in a tree. A component knows its position and
 * size, whether it is visible and enabled, and it receives mouse and keyboard
 * events after its parent decided that the event belongs to it.</p>
 *
 * <p>All state changes happen through the small set of protected methods
 * below, which keeps the interaction rules (for example that a disabled
 * component never reacts to a click) in a single place.</p>
 */
public abstract class Component {

    private float x;
    private float y;
    private float width;
    private float height;

    private boolean visible = true;
    private boolean enabled = true;

    private ComponentStyle style;
    private Component parent;
    private String id;

    private final Animation hoverAnimation = new Animation(0.0F, 0.12F, Easing.CUBIC_OUT);
    private final Animation pressAnimation = new Animation(0.0F, 0.08F, Easing.CUBIC_OUT);
    private final Animation focusAnimation = new Animation(0.0F, 0.15F, Easing.CUBIC_OUT);
    private final Animation visibilityAnimation = new Animation(1.0F, 0.18F, Easing.CUBIC_OUT);

    protected GuiContext context;
    private boolean hovered;
    private boolean pressed;
    private boolean focused;
    private String tooltip;

    // ------------------------------------------------------------------
    // Geometry
    // ------------------------------------------------------------------

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Rectangle of this component in GUI space.
     *
     * <p>Layouts assign absolute coordinates to their children, so the stored
     * position is already in GUI space and must not be offset by the parents
     * again. Adding the parents here put hit testing above the rendered
     * widget.</p>
     */
    public Rect bounds() {
        return new Rect(x, y, width, height);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds().contains(mouseX, mouseY);
    }

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;
        visibilityAnimation.animateTo(visible ? 1.0F : 0.0F, 0.18F, Easing.CUBIC_OUT);
        if (!visible) {
            hovered = false;
            pressed = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            pressed = false;
        }
    }

    public ComponentStyle style() {
        return style;
    }

    public Component style(ComponentStyle style) {
        this.style = style;
        return this;
    }

    public Component parent() {
        return parent;
    }

    /**
     * The context of the frame that is currently being processed.
     *
     * <p>It is assigned by the container before the component is updated or
     * rendered, which allows components to access the theme, the input state
     * and the draw list from inside event handlers.</p>
     */
    public GuiContext context() {
        return context;
    }

    /**
     * Assigns the current frame context.
     */
    public void setContext(GuiContext context) {
        this.context = context;
    }

    /**
     * Optional identifier, used to look components up by name.
     */
    public String getId() {
        return id;
    }

    public Component id(String id) {
        this.id = id;
        return this;
    }

    void setParent(Component parent) {
        this.parent = parent;
    }

    public String tooltip() {
        return tooltip;
    }

    public Component tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    // ------------------------------------------------------------------
    // Interaction state
    // ------------------------------------------------------------------

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isFocused() {
        return focused;
    }

    /** Hover progress in {@code [0, 1]}. */
    public float hoverProgress() {
        return hoverAnimation.value();
    }

    /** Press progress in {@code [0, 1]}. */
    public float pressProgress() {
        return pressAnimation.value();
    }

    /** Focus progress in {@code [0, 1]}. */
    public float focusProgress() {
        return focusAnimation.value();
    }

    /** Visibility progress in {@code [0, 1]}. */
    public float visibilityProgress() {
        return visibilityAnimation.value();
    }

    /**
     * True when the component takes keyboard focus.
     */
    public boolean isFocusable() {
        return false;
    }

    public void focus() {
        if (!enabled || !visible) {
            return;
        }
        setFocused(true);
    }

    public void setFocused(boolean focused) {
        if (this.focused == focused) {
            return;
        }
        this.focused = focused;
        focusAnimation.animateTo(focused ? 1.0F : 0.0F, 0.15F, Easing.CUBIC_OUT);
        onFocusChanged(focused);
    }

    protected void onFocusChanged(boolean focused) {
    }

    /**
     * Removes the keyboard focus from this component and its descendants.
     */
    public void clearFocus() {
        setFocused(false);
    }

    // ------------------------------------------------------------------
    // Frame lifecycle
    // ------------------------------------------------------------------

    /**
     * Advances the internal animations. Called once per frame for every
     * component, no matter whether it is visible.
     */
    public void update(GuiContext context) {
        this.context = context;
        hoverAnimation.update(context.deltaSeconds());
        pressAnimation.update(context.deltaSeconds());
        focusAnimation.update(context.deltaSeconds());
        visibilityAnimation.update(context.deltaSeconds());
    }

    /**
     * Renders the component. The component is expected to draw into
     * {@link GuiContext#draw()}.
     */
    public abstract void render(GuiContext context);

    /**
     * Preferred width of this component, used by the layout engine.
     */
    public float preferredWidth(GuiContext context) {
        return width;
    }

    /**
     * Preferred height of this component, used by the layout engine.
     */
    public float preferredHeight(GuiContext context) {
        return height;
    }

    // ------------------------------------------------------------------
    // Mouse
    // ------------------------------------------------------------------

    /**
     * Handles a mouse press.
     *
     * @return {@code true} when the event was consumed
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) {
            return false;
        }
        if (isMouseOver(mouseX, mouseY)) {
            hovered = true;
            if (button == 0) {
                pressed = true;
                pressAnimation.animateTo(1.0F, 0.08F, Easing.CUBIC_OUT);
            }
            return true;
        }
        return false;
    }

    /**
     * Handles a mouse release.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) {
            return false;
        }
        boolean wasPressed = pressed;
        pressed = false;
        pressAnimation.animateTo(0.0F, 0.12F, Easing.CUBIC_OUT);
        if (wasPressed && button == 0 && isMouseOver(mouseX, mouseY)) {
            onClick();
            return true;
        }
        return wasPressed;
    }

    /**
     * Handles a mouse drag.
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    /**
     * Handles mouse movement. Used to update the hover state.
     */
    public void mouseMoved(double mouseX, double mouseY) {
        if (!visible || !enabled) {
            hovered = false;
            return;
        }
        boolean over = isMouseOver(mouseX, mouseY);
        if (over != hovered) {
            hovered = over;
            hoverAnimation.animateTo(over ? 1.0F : 0.0F, 0.12F, Easing.CUBIC_OUT);
        }
    }

    /**
     * Handles a mouse wheel event.
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return false;
    }

    /**
     * Called when the component was clicked with the primary button.
     */
    protected void onClick() {
    }

    // ------------------------------------------------------------------
    // Keyboard
    // ------------------------------------------------------------------

    /**
     * Handles a key press.
     *
     * @return {@code true} when the event was consumed
     */
    public boolean keyPressed(int keyCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int keyCode, int modifiers) {
        return false;
    }

    /**
     * Handles a typed character.
     */
    public boolean charTyped(int codePoint) {
        return false;
    }

    // ------------------------------------------------------------------
    // Style helpers
    // ------------------------------------------------------------------

    protected int backgroundColor(GuiContext context) {
        Integer color = style == null ? null : style.background();
        return color == null ? context.theme().surface() : color;
    }

    protected int hoverBackgroundColor(GuiContext context) {
        Integer color = style == null ? null : style.backgroundHover();
        if (color != null) {
            return color;
        }
        return MathUtil.lerpColor(backgroundColor(context), context.theme().surfaceVariant(), hoverProgress());
    }

    protected int textColor(GuiContext context) {
        Integer color = style == null ? null : style.text();
        if (color != null) {
            return color;
        }
        return enabled ? context.theme().text() : context.theme().textDisabled();
    }

    protected int borderColor(GuiContext context) {
        Integer color = style == null ? null : style.border();
        return color == null ? context.theme().border() : color;
    }

    protected int accentColor(GuiContext context) {
        Integer color = style == null ? null : style.accent();
        return color == null ? context.theme().accent() : color;
    }

    protected float radius(GuiContext context) {
        Float value = style == null ? null : style.radius();
        return value == null ? context.theme().radius() : value;
    }

    protected float padding(GuiContext context) {
        Float value = style == null ? null : style.padding();
        return value == null ? context.theme().padding() : value;
    }

    protected boolean shadowEnabled(GuiContext context) {
        Boolean value = style == null ? null : style.shadow();
        return value == null ? context.theme().shadowEnabled() : value;
    }

    protected float opacity(GuiContext context) {
        Float value = style == null ? null : style.opacity();
        return value == null ? context.theme().opacity() : value;
    }

    protected boolean isEscape(int keyCode) {
        return keyCode == Keys.ESCAPE;
    }

    /**
     * Draws the standard panel background: rounded rectangle, optional border
     * and optional shadow.
     */
    protected void renderPanel(GuiContext context, float px, float py, float pw, float ph, int color) {
        Theme theme = context.theme();
        float r = radius(context);
        float alpha = opacity(context);
        int effective = alpha >= 1.0F ? color : dev.nanomango.gui.util.Colors.withAlphaFactor(color, alpha);
        if (shadowEnabled(context)) {
            context.draw().shadow(px, py + theme.shadowOffsetY(), pw, ph, r, theme.shadowBlur(), theme.shadow());
        }
        context.draw().fillRounded(px, py, pw, ph, r, effective);
    }
}
