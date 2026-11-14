package dev.nanomango.nanoarray;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.components.Button;
import dev.nanomango.gui.components.Checkbox;
import dev.nanomango.gui.components.ColorPicker;
import dev.nanomango.gui.components.CollapsibleSection;
import dev.nanomango.gui.components.Dropdown;
import dev.nanomango.gui.components.Graph;
import dev.nanomango.gui.components.KeybindSelector;
import dev.nanomango.gui.components.NotificationManager;
import dev.nanomango.gui.components.NumberInput;
import dev.nanomango.gui.components.Panel;
import dev.nanomango.gui.components.ProgressBar;
import dev.nanomango.gui.components.RadioGroup;
import dev.nanomango.gui.components.SearchField;
import dev.nanomango.gui.components.SearchableList;
import dev.nanomango.gui.components.Separator;
import dev.nanomango.gui.components.Slider;
import dev.nanomango.gui.components.Tabs;
import dev.nanomango.gui.components.Text;
import dev.nanomango.gui.components.TextBox;
import dev.nanomango.gui.components.Toggle;
import dev.nanomango.gui.core.Gui;
import dev.nanomango.gui.layout.VerticalLayout;

/**
 * Builds the default user interface of the framework.
 *
 * <p>It is a showcase of the component library and doubles as a template for
 * projects that build on top of NanoArray.</p>
 */
public final class NanoArrayPanels {

    private NanoArrayPanels() {
    }

    /**
     * Populates the GUI with the default panels.
     */
    public static void build(Gui gui) {
        Container root = gui.root();
        // The root keeps the default stack layout, so the tab view fills the
        // whole canvas.
        root.clip(false);

        Tabs tabs = new Tabs();
        root.add(tabs);

        tabs.addTab("General", generalPanel(gui));
        tabs.addTab("Appearance", appearancePanel(gui));
        tabs.addTab("Modules", modulesPanel(gui));
        tabs.addTab("About", aboutPanel(gui));
    }

    private static Component generalPanel(Gui gui) {
        Panel panel = new Panel("General settings");
        panel.layout(new VerticalLayout(gui.themes().current().spacing()));

        NotificationManager notifications = new NotificationManager();
        panel.add(notifications);

        Toggle notificationToggle = new Toggle("Notifications", true);
        notificationToggle.onChange(value -> {
            if (value) {
                notifications.info("NanoArray", "Notifications enabled");
            }
        });
        panel.add(notificationToggle);

        Slider volume = new Slider("Volume", 0.0F, 100.0F, 65.0F);
        volume.onChange(value -> gui.configManager().set("general.volume", value));
        panel.add(volume);

        NumberInput limit = new NumberInput(1.0, 999.0, 128.0, 1.0);
        limit.onValueChange(value -> gui.configManager().set("general.limit", value));
        panel.add(limit);

        Dropdown mode = new Dropdown("Render mode");
        mode.options("Balanced", "Performance", "Quality");
        mode.onChange(value -> gui.configManager().set("general.mode", value));
        panel.add(mode);

        RadioGroup group = new RadioGroup();
        group.addOption("Automatic");
        group.addOption("Manual");
        group.addOption("Disabled");
        group.setSelectedIndex(0);
        panel.add(group);

        TextBox username = new TextBox("NanoMango");
        username.placeholder("Enter username");
        panel.add(username);

        SearchField search = new SearchField();
        panel.add(search);

        Checkbox experimental = new Checkbox("Enable experimental features", false);
        panel.add(experimental);

        panel.add(new Separator());

        Button apply = new Button("Apply changes");
        apply.variant(Button.Variant.PRIMARY);
        apply.onClick(() -> notifications.success("NanoArray", "Settings applied"));
        panel.add(apply);

        return panel;
    }

    private static Component appearancePanel(Gui gui) {
        Panel panel = new Panel("Appearance");
        panel.layout(new VerticalLayout(gui.themes().current().spacing()));

        Text themeLabel = new Text("Theme");
        panel.add(themeLabel);

        Dropdown themes = new Dropdown();
        for (dev.nanomango.gui.theme.Theme theme : gui.themes().all()) {
            themes.options().add(theme.name());
        }
        themes.setSelectedIndex(0);
        themes.onChange(value -> {
            int index = themes.options().indexOf(value);
            if (index >= 0 && index < gui.themes().all().size()) {
                gui.themes().setTheme(gui.themes().all().get(index).id());
            }
        });
        panel.add(themes);

        ColorPicker accent = new ColorPicker(gui.themes().current().accent());
        accent.onChange(value -> gui.themes().withAccent(value));
        panel.add(accent);

        Slider radius = new Slider("Corner radius", 0.0F, 16.0F, gui.themes().current().radius());
        panel.add(radius);

        Slider spacing = new Slider("Spacing", 2.0F, 16.0F, gui.themes().current().spacing());
        panel.add(spacing);

        KeybindSelector keybind = new KeybindSelector(NanoArrayKeybinds.openBind());
        panel.add(keybind);

        panel.add(new Separator());

        CollapsibleSection advanced = new CollapsibleSection("Advanced");
        ProgressBar progress = new ProgressBar(0.35F, 1.0F);
        advanced.add(progress);
        Graph graph = new Graph();
        graph.maximumSamples(60);
        advanced.add(graph);
        panel.add(advanced);

        return panel;
    }

    private static Component modulesPanel(Gui gui) {
        Panel panel = new Panel("Modules");
        panel.layout(new VerticalLayout(gui.themes().current().spacing()));

        SearchableList list = new SearchableList();
        list.addItem("Auto Tool", "auto-tool");
        list.addItem("ESP", "esp");
        list.addItem("Nametags", "nametags");
        list.addItem("Waypoints", "waypoints");
        panel.add(list);

        return panel;
    }

    private static Component aboutPanel(Gui gui) {
        Panel panel = new Panel("About NanoArray");
        panel.layout(new VerticalLayout(gui.themes().current().spacing()));

        panel.add(new Text("NanoArray is a modern, high performance GUI framework for Minecraft."));
        panel.add(new Text("Supported versions: 1.21.11, 26.1.2, 26.2, 26.3"));
        panel.add(new Text("Press Right Shift to open or close this window."));

        NotificationManager notifications = new NotificationManager();
        panel.add(notifications);

        Button notify = new Button("Show a notification");
        notify.onClick(() -> notifications.info("NanoArray", "Everything works as expected"));
        panel.add(notify);

        return panel;
    }
}
