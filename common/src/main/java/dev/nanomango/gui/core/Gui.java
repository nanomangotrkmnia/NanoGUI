package dev.nanomango.gui.core;

import dev.nanomango.gui.animation.Easing;
import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.config.ConfigManager;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.input.Clipboard;
import dev.nanomango.gui.input.InputState;
import dev.nanomango.gui.input.Keybinds;
import dev.nanomango.gui.layout.StackLayout;
import dev.nanomango.gui.module.ModuleManager;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderBackend;
import dev.nanomango.gui.render.RenderContext;
import dev.nanomango.gui.theme.ThemeManager;

/**
 * The root of the GUI framework.
 *
 * <p>A {@code Gui} owns the component tree, the theme, the fonts, the
 * keybinds and the input state. It is completely independent of Minecraft:
 * the host renders it through a {@link RenderBackend} and forwards raw input
 * events to it.</p>
 */
public final class Gui {

    private final ThemeManager themes = new ThemeManager();
    private final FontRegistry fonts = new FontRegistry();
    private final Keybinds keybinds = new Keybinds();
    private final InputState input = new InputState();
    private final DrawList drawList = new DrawList();
    private final GuiContext context = new GuiContext(themes.current(), input, drawList, fonts);
    private final OverlayLayer overlay = context.overlay();
    private final RenderContext renderContext = new RenderContext();
    private final ModuleManager modules = new ModuleManager();

    private final Container root = new Container(new StackLayout());
    private ConfigManager configManager;

    private GuiHost host;
    private boolean open;
    private float openProgress;
    private float openDuration = 0.18F;
    private boolean dirty = true;
    private float lastDelta = 0.0F;
    private boolean autoSizeRoot = true;
    private float pixelScale = 1.0F;

    public Gui() {
        root.setVisible(true);
        context.setClipboard(new Clipboard.Memory());
    }

    // ------------------------------------------------------------------
    // Wiring
    // ------------------------------------------------------------------

    public Gui host(GuiHost host) {
        this.host = host;
        return this;
    }

    public GuiHost host() {
        return host;
    }

    public ThemeManager themes() {
        return themes;
    }

    public FontRegistry fonts() {
        return fonts;
    }

    public Keybinds keybinds() {
        return keybinds;
    }

    public InputState input() {
        return input;
    }

    public DrawList drawList() {
        return drawList;
    }

    public GuiContext context() {
        return context;
    }

    public OverlayLayer overlay() {
        return overlay;
    }

    public ModuleManager modules() {
        return modules;
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public Gui configManager(ConfigManager configManager) {
        this.configManager = configManager;
        return this;
    }

    public Container root() {
        return root;
    }

    /**
     * Whether the root container is resized to the viewport every frame.
     *
     * <p>This is enabled by default. The root then acts as the canvas of the
     * GUI and every top level component is positioned relative to it. Disable
     * it when the root should keep the bounds that were assigned to it.</p>
     */
    public Gui autoSizeRoot(boolean autoSizeRoot) {
        this.autoSizeRoot = autoSizeRoot;
        return this;
    }

    public boolean autoSizeRoot() {
        return autoSizeRoot;
    }

    /**
     * Ratio between physical pixels and GUI units.
     *
     * <p>Everything inside the framework is measured in GUI units. Backends
     * that render directly into physical pixels, such as NanoVG, need the
     * ratio to keep text and geometry sharp on a high density display. The
     * Minecraft backend already renders in GUI space and therefore ignores
     * it.</p>
     */
    public Gui pixelScale(float pixelScale) {
        this.pixelScale = pixelScale > 0.0F ? pixelScale : 1.0F;
        return this;
    }

    public float pixelScale() {
        return pixelScale;
    }

    public Font defaultFont() {
        return fonts.defaultFont();
    }

    // ------------------------------------------------------------------
    // Open / close
    // ------------------------------------------------------------------

    public boolean isOpen() {
        return open;
    }

    /**
     * Opens the GUI. The host is notified so that it can show the screen.
     */
    public void open() {
        if (open) {
            return;
        }
        open = true;
        openProgress = 0.0F;
        input.reset();
        overlay.clear();
        if (host != null) {
            host.openGui();
        }
    }

    /**
     * Closes the GUI. The host is notified so that it can hide the screen.
     */
    public void close() {
        if (!open) {
            return;
        }
        open = false;
        // Reset the input state so that no key or mouse button stays stuck
        // when the GUI is closed while a key is held down.
        input.reset();
        overlay.clear();
        if (host != null) {
            host.closeGui();
        }
    }

    public void toggle() {
        if (open) {
            close();
        } else {
            open();
        }
    }

    /**
     * Duration of the open and close transition in seconds.
     */
    public Gui openDuration(float openDuration) {
        this.openDuration = openDuration;
        return this;
    }

    // ------------------------------------------------------------------
    // Frame
    // ------------------------------------------------------------------

    /**
     * Renders the GUI.
     *
     * @param backend        backend that replays the recorded commands
     * @param deltaSeconds   time since the last frame
     * @param viewportWidth  width of the drawable area
     * @param viewportHeight height of the drawable area
     * @param mouseX        mouse position in GUI space
     * @param mouseY        mouse position in GUI space
     */
    public void render(RenderBackend backend, float deltaSeconds, int viewportWidth, int viewportHeight,
                       double mouseX, double mouseY) {
        this.lastDelta = deltaSeconds;

        float target = open ? 1.0F : 0.0F;
        float speed = Math.min(1.0F, deltaSeconds / Math.max(0.0001F, openDuration));
        openProgress += (target - openProgress) * speed;
        if (Math.abs(target - openProgress) < 0.001F) {
            openProgress = target;
        }

        if (openProgress <= 0.0F && !open) {
            // Fully closed, nothing to draw.
            return;
        }

        drawList.clear();

        context.setTheme(themes.current());
        context.setDeltaSeconds(deltaSeconds);
        context.setViewport(viewportWidth, viewportHeight);
        context.setMouse(mouseX, mouseY);
        context.setScale(pixelScale);

        input.setMousePosition(mouseX, mouseY);

        // The root is the canvas of the GUI: it covers the whole viewport so
        // that the top level components can position themselves relative to
        // the screen.
        if (autoSizeRoot) {
            root.setBounds(0.0F, 0.0F, viewportWidth, viewportHeight);
        }

        // Components update their animations.
        root.setContext(context);
        root.update(context);
        for (Component component : overlay.components()) {
            component.setContext(context);
            component.update(context);
        }

        // Fade the whole GUI in and out.
        int fade = (int) (Math.min(1.0F, Math.max(0.0F, openProgress)) * 255.0F);

        root.render(context);

        // Overlays (popups, menus, toasts) are drawn last.
        for (Component component : overlay.components()) {
            component.setContext(context);
            component.render(context);
        }

        renderContext.setViewport(viewportWidth, viewportHeight)
                .setDeltaSeconds(deltaSeconds)
                .setMouse(mouseX, mouseY)
                .setScale(context.scale());

        backend.beginFrame(renderContext);
        backend.render(renderContext, drawList, fonts);
        backend.endFrame(renderContext);

        if (fade < 255) {
            // The fade is applied by the host screen, see NanoGUIScreen.
        }
        dirty = false;
        input.endFrame();
    }

    /**
     * Updates animations without rendering. Useful for tick based updates.
     */
    public void tick(float deltaSeconds) {
        this.lastDelta = deltaSeconds;
    }

    public float lastDelta() {
        return lastDelta;
    }

    /**
     * Progress of the open transition in {@code [0, 1]}.
     */
    public float openProgress() {
        return openProgress;
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    public void mouseMoved(double mouseX, double mouseY) {
        if (!open) {
            return;
        }
        input.setMousePosition(mouseX, mouseY);
        context.setMouse(mouseX, mouseY);
        for (Component component : overlay.components()) {
            component.setContext(context);
            component.mouseMoved(mouseX, mouseY);
        }
        root.mouseMoved(mouseX, mouseY);
    }

    /**
     * @return {@code true} when the event was consumed by the GUI
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        input.setMousePosition(mouseX, mouseY);
        context.setMouse(mouseX, mouseY);
        input.setButton(button, true);

        // A click moves the keyboard focus: everything loses it first and the
        // component under the cursor takes it back.
        root.clearFocus();
        for (Component component : overlay.components()) {
            component.clearFocus();
        }

        for (int i = overlay.components().size() - 1; i >= 0; i--) {
            Component component = overlay.components().get(i);
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return root.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        input.setMousePosition(mouseX, mouseY);
        context.setMouse(mouseX, mouseY);
        input.setButton(button, false);

        boolean consumed = false;
        for (Component component : overlay.components()) {
            consumed |= component.mouseReleased(mouseX, mouseY, button);
        }
        consumed |= root.mouseReleased(mouseX, mouseY, button);
        return consumed;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!open) {
            return false;
        }
        input.setMousePosition(mouseX, mouseY);
        context.setMouse(mouseX, mouseY);

        boolean consumed = false;
        for (Component component : overlay.components()) {
            consumed |= component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        consumed |= root.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return consumed;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!open) {
            return false;
        }
        input.setMousePosition(mouseX, mouseY);
        context.setMouse(mouseX, mouseY);
        input.addScroll(deltaX, deltaY);

        for (int i = overlay.components().size() - 1; i >= 0; i--) {
            Component component = overlay.components().get(i);
            if (component.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                return true;
            }
        }
        return root.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    public boolean keyPressed(int keyCode, int modifiers) {
        if (!open) {
            return false;
        }
        input.setModifiers(
                (modifiers & dev.nanomango.gui.input.Modifiers.SHIFT) != 0,
                (modifiers & dev.nanomango.gui.input.Modifiers.CONTROL) != 0,
                (modifiers & dev.nanomango.gui.input.Modifiers.ALT) != 0,
                false);

        for (int i = overlay.components().size() - 1; i >= 0; i--) {
            Component component = overlay.components().get(i);
            if (component.keyPressed(keyCode, modifiers)) {
                return true;
            }
        }
        return root.keyPressed(keyCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int modifiers) {
        if (!open) {
            return false;
        }
        for (Component component : overlay.components()) {
            component.keyReleased(keyCode, modifiers);
        }
        return root.keyReleased(keyCode, modifiers);
    }

    public boolean charTyped(int codePoint) {
        if (!open) {
            return false;
        }
        input.addTypedText(new String(Character.toChars(codePoint)));
        for (int i = overlay.components().size() - 1; i >= 0; i--) {
            Component component = overlay.components().get(i);
            if (component.charTyped(codePoint)) {
                return true;
            }
        }
        return root.charTyped(codePoint);
    }

    public void focusChanged(boolean focused) {
        input.setFocused(focused);
    }

    /**
     * True when the GUI currently holds a modal overlay.
     */
    public boolean hasModal() {
        for (Component component : overlay.components()) {
            if (component instanceof dev.nanomango.gui.components.ModalDialog dialog && dialog.isOpen()) {
                return true;
            }
        }
        return false;
    }
}
