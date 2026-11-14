package dev.nanomango.gui.components;

import dev.nanomango.gui.animation.Easing;
import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

/**
 * A single notification that slides in from the edge of the screen.
 */
public class Toast extends Component {

    public enum Level {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    private final String title;
    private final String message;
    private final Level level;
    private float life = 4.0F;
    private float age;
    private float slide;

    public Toast(String title, String message, Level level) {
        this.title = title == null ? "" : title;
        this.message = message == null ? "" : message;
        this.level = level;
    }

    public Level level() {
        return level;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    public Toast life(float seconds) {
        this.life = seconds;
        return this;
    }

    public boolean expired() {
        return age >= life;
    }

    @Override
    public void update(GuiContext context) {
        super.update(context);
        age += context.deltaSeconds();
        float target = age < life - 0.3F ? 1.0F : 0.0F;
        float speed = Math.min(1.0F, context.deltaSeconds() * 10.0F);
        slide += (target - slide) * speed;
    }

    private int levelColor(GuiContext context) {
        switch (level) {
            case SUCCESS:
                return context.theme().success();
            case WARNING:
                return context.theme().warning();
            case ERROR:
                return context.theme().danger();
            default:
                return context.theme().accent();
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = context.theme().radius();
        float offset = (1.0F - slide) * (width() + 16.0F);

        context.draw().shadow(x() + offset, y(), width(), height(), radius, context.theme().shadowBlur(),
                context.theme().shadow());
        context.draw().fillRounded(x() + offset, y(), width(), height(), radius, context.theme().surfaceElevated());
        context.draw().strokeRounded(x() + offset, y(), width(), height(), radius, context.theme().borderWidth(),
                context.theme().border());
        context.draw().fillRounded(x() + offset, y() + 4.0F, 3.0F, height() - 8.0F, 1.5F, levelColor(context));

        float textX = x() + offset + context.theme().padding();
        float cursor = y() + context.theme().padding() * 0.7F;
        if (!title.isEmpty()) {
            context.draw().text(textX, cursor, context.boldFont().id(), title, context.theme().text());
            cursor += context.boldFont().height() + 2.0F;
        }
        if (!message.isEmpty()) {
            context.draw().text(textX, cursor, context.font().id(), message, context.theme().textMuted());
        }
    }

    /**
     * Fades the toast out over the remaining life.
     */
    public float fade() {
        return MathUtil.saturate((life - age) / 0.3F);
    }
}
