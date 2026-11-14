package dev.nanomango.nanogui;

import dev.nanomango.gui.config.ConfigManager;
import dev.nanomango.gui.core.Gui;
import dev.nanomango.gui.core.GuiHost;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontLoader;
import dev.nanomango.gui.theme.Theme;
import dev.nanomango.nanogui.backend.FontAtlasRegistry;
import dev.nanomango.nanogui.backend.MinecraftRenderBackend;
import dev.nanomango.nanogui.backend.NanoGUIGraphics;
import dev.nanomango.nanogui.backend.NanoGUITextureRegistry;
import dev.nanomango.nanogui.backend.RoundedCornerAtlas;
import dev.nanomango.nanogui.font.MinecraftFont;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

/**
 * Central object of the mod.
 *
 * <p>It owns the shared GUI framework instance, the configuration, the fonts
 * and the keybinds, and it translates between Minecraft and the framework.</p>
 */
public final class NanoGUICore implements GuiHost {

    private static final NanoGUICore INSTANCE = new NanoGUICore();

    /** Size of the bundled font in GUI pixels. */
    public static final float FONT_SIZE = 13.0F;

    /** Id of the Minecraft font, the first font that is registered. */
    public static final int MINECRAFT_FONT_ID = 0;

    /** Id of the regular weight of the bundled font. */
    public static final int REGULAR_FONT_ID = 1;

    /** Id of the medium weight of the bundled font. */
    public static final int MEDIUM_FONT_ID = 2;

    /** Id of the bold weight of the bundled font. */
    public static final int BOLD_FONT_ID = 3;

    private final Gui gui = new Gui();

    private final FontAtlasRegistry atlases = new FontAtlasRegistry();
    private final RoundedCornerAtlas corners = new RoundedCornerAtlas();
    private final NanoGUITextureRegistry textures = new NanoGUITextureRegistry();
    private final MinecraftRenderBackend backend = new MinecraftRenderBackend(atlases, corners);

    private NanoGUIPlatform platform;
    private ConfigManager configManager;
    private Font defaultFont;
    private boolean initialized;
    private boolean fontsRegistered;

    private NanoGUICore() {
        gui.host(this);
        backend.textureResolver(textures::byId);
    }

    public static NanoGUICore get() {
        return INSTANCE;
    }

    public Gui gui() {
        return gui;
    }

    public ConfigManager config() {
        return configManager;
    }

    public Font defaultFont() {
        return defaultFont;
    }

    public NanoGUIPlatform platform() {
        return platform;
    }

    /**
     * Registry of the textures that the framework can draw.
     */
    public NanoGUITextureRegistry textures() {
        return textures;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialises the framework. Called from the client entry point.
     */
    public synchronized void initialize(NanoGUIPlatform platform) {
        if (initialized) {
            return;
        }
        this.platform = platform;

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("nanogui.json");
        configManager = new ConfigManager(configPath);
        configManager.load();
        gui.configManager(configManager);

        applyStoredTheme();
        NanoGUIKeybinds.register();

        NanoGUIPanels.build(gui);
        initialized = true;

        // The fonts are registered on the first tick or frame. The client
        // entrypoint runs inside the Minecraft constructor, before the window
        // and the default font exist.
        ensureFonts();
    }

    /**
     * Registers the fonts once the client is far enough along that the window
     * and the Minecraft font are available.
     */
    private void ensureFonts() {
        if (fontsRegistered) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.font == null) {
            return;
        }
        registerFonts();
        fontsRegistered = true;
    }

    /**
     * Registers the Minecraft font and the bundled font family.
     *
     * <p>The bundled font is rendered by the native renderer when it is
     * available and by the Java2D fallback otherwise. Both produce the same
     * result, so the rest of the framework does not have to care about it.</p>
     */
    private void registerFonts() {
        MinecraftFont minecraftFont = new MinecraftFont(MINECRAFT_FONT_ID, "Minecraft",
                Minecraft.getInstance().font);
        gui.fonts().register(minecraftFont);

        // The GUI is laid out in GUI units, but on a Retina display (and at
        // every Minecraft GUI scale above one) a GUI unit covers several
        // physical pixels. Rasterising the atlas at that density is what keeps
        // text sharp instead of upscaled.
        float rasterScale = Math.max(1.0F, Minecraft.getInstance().getWindow().getGuiScale());
        gui.pixelScale(rasterScale);

        try {
            Font regular = FontLoader.create(REGULAR_FONT_ID, "Inter",
                    FontLoader.INTER_REGULAR, FONT_SIZE, java.awt.Font.PLAIN, rasterScale);
            Font medium = FontLoader.create(MEDIUM_FONT_ID, "Inter Medium",
                    FontLoader.INTER_MEDIUM, FONT_SIZE, java.awt.Font.BOLD, rasterScale);
            Font bold = FontLoader.create(BOLD_FONT_ID, "Inter Bold",
                    FontLoader.INTER_BOLD, FONT_SIZE, java.awt.Font.BOLD, rasterScale);

            gui.fonts().register(regular);
            gui.fonts().register(medium);
            gui.fonts().register(bold);

            gui.themes().setFonts(regular, medium, bold);
            defaultFont = regular;
        } catch (java.io.IOException exception) {
            // The bundled font is missing, the Minecraft font is used instead.
            defaultFont = minecraftFont;
        }

        gui.fonts().setDefaultFont(defaultFont);
    }

    private void applyStoredTheme() {
        String themeId = configManager.getString("theme", dev.nanomango.gui.theme.ThemeManager.DARK_ID);
        if (!gui.themes().setTheme(themeId)) {
            gui.themes().setTheme(dev.nanomango.gui.theme.ThemeManager.DARK_ID);
        }
    }

    /**
     * Called once per client tick.
     */
    public void onClientTick() {
        if (!initialized) {
            return;
        }
        ensureFonts();
        if (NanoGUIKeybinds.openKey() != null && NanoGUIKeybinds.openKey().consumeClick()) {
            toggle();
        }
    }

    /**
     * Opens or closes the GUI.
     */
    public void toggle() {
        if (gui.isOpen()) {
            gui.close();
        } else {
            gui.open();
        }
    }

    @Override
    public void openGui() {
        if (platform != null) {
            platform.openScreen(platform.createScreen());
        }
    }

    @Override
    public void closeGui() {
        if (platform != null) {
            platform.closeScreen();
        }
    }

    /**
     * Renders the GUI with the Minecraft backend.
     *
     * <p>{@code partialTick} is the fraction of a game tick and not a time
     * delta, so the frame time is measured here instead. Passing it through
     * made every animation and notification run at the wrong speed.</p>
     */
    public void render(NanoGUIGraphics graphics, int width, int height, float partialTick) {
        ensureFonts();
        if (!fontsRegistered) {
            return;
        }
        if (!gui.isOpen() && gui.openProgress() <= 0.0F) {
            return;
        }
        long now = System.nanoTime();
        float deltaSeconds = lastFrameNanos == 0L
                ? 1.0F / 60.0F
                : (now - lastFrameNanos) / 1_000_000_000.0F;
        lastFrameNanos = now;
        // A long pause (menu, window drag) must not fast forward the toasts.
        deltaSeconds = Math.max(0.0F, Math.min(deltaSeconds, 0.1F));

        backend.graphics(graphics);
        gui.context().setTextureAspectProvider(textures::aspect);
        gui.render(backend, deltaSeconds, width, height, lastMouseX, lastMouseY);
    }

    private long lastFrameNanos;

    private double lastMouseX;
    private double lastMouseY;

    public void mouseMoved(double x, double y) {
        lastMouseX = x;
        lastMouseY = y;
        gui.mouseMoved(x, y);
    }

    /**
     * Writes pending configuration changes to disk.
     */
    public void save() {
        if (configManager != null) {
            configManager.set("theme", gui.themes().current().id());
            configManager.flush();
        }
    }
}
