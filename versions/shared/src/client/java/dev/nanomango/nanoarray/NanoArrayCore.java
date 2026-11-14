package dev.nanomango.nanoarray;

import dev.nanomango.gui.config.ConfigManager;
import dev.nanomango.gui.core.Gui;
import dev.nanomango.gui.core.GuiHost;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.theme.Theme;
import dev.nanomango.nanoarray.backend.MinecraftRenderBackend;
import dev.nanomango.nanoarray.backend.NanoArrayGraphics;
import dev.nanomango.nanoarray.font.MinecraftFont;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

/**
 * Central object of the mod.
 *
 * <p>It owns the shared GUI framework instance, the configuration, the fonts
 * and the keybinds, and it translates between Minecraft and the framework.</p>
 */
public final class NanoArrayCore implements GuiHost {

    private static final NanoArrayCore INSTANCE = new NanoArrayCore();

    private final Gui gui = new Gui();

    private NanoArrayPlatform platform;
    private ConfigManager configManager;
    private Font defaultFont;
    private boolean initialized;

    private NanoArrayCore() {
        gui.host(this);
    }

    public static NanoArrayCore get() {
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

    public NanoArrayPlatform platform() {
        return platform;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialises the framework. Called from the client entry point.
     */
    public synchronized void initialize(NanoArrayPlatform platform) {
        if (initialized) {
            return;
        }
        this.platform = platform;

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("nanoarray.json");
        configManager = new ConfigManager(configPath);
        configManager.load();
        gui.configManager(configManager);

        MinecraftFont minecraftFont = new MinecraftFont(0, "Minecraft", Minecraft.getInstance().font);
        gui.fonts().register(minecraftFont);
        gui.fonts().setDefaultFont(minecraftFont);
        defaultFont = minecraftFont;

        applyStoredTheme();
        NanoArrayKeybinds.register();

        NanoArrayPanels.build(gui);
        initialized = true;
    }

    private void applyStoredTheme() {
        String themeId = configManager.getString("theme", ThemeManagerDefaults.DEFAULT_THEME);
        if (!gui.themes().setTheme(themeId)) {
            gui.themes().setTheme(ThemeManagerDefaults.DEFAULT_THEME);
        }
    }

    /**
     * Called once per client tick.
     */
    public void onClientTick() {
        if (!initialized) {
            return;
        }
        if (NanoArrayKeybinds.openKey() != null && NanoArrayKeybinds.openKey().consumeClick()) {
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
     */
    public void render(NanoArrayGraphics graphics, int width, int height, float partialTick) {
        if (!gui.isOpen() && gui.openProgress() <= 0.0F) {
            return;
        }
        MinecraftRenderBackend backend = new MinecraftRenderBackend(graphics);
        gui.render(backend, partialTick, width, height, lastMouseX, lastMouseY);
    }

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

    /**
     * Default theme identifiers, kept in a separate class so that the shared
     * code does not depend on the theme implementation details.
     */
    public static final class ThemeManagerDefaults {
        public static final String DEFAULT_THEME = dev.nanomango.gui.theme.ThemeManager.DARK_ID;

        private ThemeManagerDefaults() {
        }
    }
}
