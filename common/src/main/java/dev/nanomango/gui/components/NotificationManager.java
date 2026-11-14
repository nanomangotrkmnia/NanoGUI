package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.core.OverlayLayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Shows toast notifications in the corner of the screen.
 */
public class NotificationManager extends Component {

    private final List<Toast> toasts = new ArrayList<>();
    private float margin = 12.0F;
    private float spacing = 8.0F;
    private boolean topRight = true;

    public NotificationManager() {
        setVisible(true);
    }

    public NotificationManager topRight(boolean topRight) {
        this.topRight = topRight;
        return this;
    }

    public Toast notify(String title, String message, Toast.Level level) {
        Toast toast = new Toast(title, message, level);
        toasts.add(toast);
        return toast;
    }

    public Toast info(String title, String message) {
        return notify(title, message, Toast.Level.INFO);
    }

    public Toast success(String title, String message) {
        return notify(title, message, Toast.Level.SUCCESS);
    }

    public Toast warning(String title, String message) {
        return notify(title, message, Toast.Level.WARNING);
    }

    public Toast error(String title, String message) {
        return notify(title, message, Toast.Level.ERROR);
    }

    public List<Toast> toasts() {
        return toasts;
    }

    public void clear() {
        toasts.clear();
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        Iterator<Toast> iterator = toasts.iterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();
            toast.update(context);
            if (toast.expired()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void render(GuiContext context) {
        float cursor = topRight ? margin : context.viewportHeight() - margin;
        for (Toast toast : toasts) {
            float toastWidth = 220.0F;
            float toastHeight = Math.max(context.theme().controlHeight(),
                    context.font().height() + context.boldFont().height() + context.theme().padding() * 2.0F);
            float toastX = topRight ? context.viewportWidth() - margin - toastWidth : margin;
            float toastY = topRight ? cursor : cursor - toastHeight;
            toast.setBounds(toastX, toastY, toastWidth, toastHeight);
            toast.render(context);
            if (topRight) {
                cursor += toastHeight + spacing;
            } else {
                cursor -= toastHeight + spacing;
            }
        }
    }
}
