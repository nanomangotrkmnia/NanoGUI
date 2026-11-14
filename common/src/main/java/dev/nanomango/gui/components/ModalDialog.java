package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A modal dialog that blocks interaction with the rest of the GUI.
 */
public class ModalDialog extends Container {

    private final String title;
    private final List<Component> actions = new ArrayList<>();
    private boolean open;
    private Runnable onClose;

    public ModalDialog(String title) {
        super(new VerticalLayout(8.0F));
        this.title = title == null ? "" : title;
        clip(false);
        setVisible(false);
    }

    public ModalDialog onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public ModalDialog action(Component component) {
        actions.add(component);
        add(component);
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
        setVisible(true);
    }

    public void close() {
        open = false;
        setVisible(false);
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = context.theme().radius();
        float shadow = context.theme().shadowBlur();

        // Dim the background.
        context.draw().fillRect(0.0F, 0.0F, context.viewportWidth(), context.viewportHeight(),
                context.theme().overlay());

        float dialogWidth = width();
        float dialogHeight = height();
        float dialogX = (context.viewportWidth() - dialogWidth) * 0.5F;
        float dialogY = (context.viewportHeight() - dialogHeight) * 0.5F;
        setBounds(dialogX, dialogY, dialogWidth, dialogHeight);

        context.draw().shadow(dialogX, dialogY, dialogWidth, dialogHeight, radius, shadow, context.theme().shadow());
        context.draw().fillRounded(dialogX, dialogY, dialogWidth, dialogHeight, radius,
                context.theme().surfaceElevated());
        context.draw().strokeRounded(dialogX, dialogY, dialogWidth, dialogHeight, radius,
                context.theme().borderWidth(), context.theme().borderStrong());

        float cursor = dialogY + context.theme().padding();
        if (!title.isEmpty()) {
            context.draw().text(dialogX + context.theme().padding(), cursor, context.boldFont().id(), title,
                    context.theme().text());
            cursor += context.boldFont().height() + context.theme().spacing();
        }
        super.render(context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) {
            return false;
        }
        super.mouseClicked(mouseX, mouseY, button);
        // Modal dialogs swallow every click so the world is not affected.
        return true;
    }
}
