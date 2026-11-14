package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.util.Colors;

/**
 * A clickable button with a label.
 *
 * <pre>{@code
 * new Button("Apply").onClick(() -> apply());
 * }</pre>
 */
public class Button extends Component {

    private String label;
    private Font font;
    private Runnable onClick;
    private Runnable onPress;
    private Runnable onRelease;
    private int variant = Variant.DEFAULT;
    private float horizontalPadding = -1.0F;

    public Button(String label) {
        this.label = label == null ? "" : label;
    }

    public Button(String label, Runnable onClick) {
        this(label);
        this.onClick = onClick;
    }

    public String label() {
        return label;
    }

    public Button label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public Button font(Font font) {
        this.font = font;
        return this;
    }

    public Button onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public Button onPress(Runnable onPress) {
        this.onPress = onPress;
        return this;
    }

    public Button onRelease(Runnable onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    public Button variant(int variant) {
        this.variant = variant;
        return this;
    }

    public Button padding(float horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        return this;
    }

    private Font resolvedFont(GuiContext context) {
        if (font != null) {
            return font;
        }
        return context.theme().mediumFont() != null ? context.theme().mediumFont() : context.font();
    }

    private float resolvedPadding(GuiContext context) {
        return horizontalPadding >= 0.0F ? horizontalPadding : context.theme().padding() * 1.5F;
    }

    @Override
    protected void onClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseReleased(mouseX, mouseY, button);
        if (onRelease != null) {
            onRelease.run();
        }
        return consumed;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        if (consumed && button == 0 && onPress != null) {
            onPress.run();
        }
        return consumed;
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        int baseColor;
        int textColorValue;

        switch (variant) {
            case Variant.PRIMARY -> {
                baseColor = accentColor(context);
                if (isPressed()) {
                    baseColor = context.theme().accentPressed();
                } else if (isHovered()) {
                    baseColor = context.theme().accentHover();
                }
                textColorValue = context.theme().onAccent();
            }
            case Variant.DANGER -> {
                baseColor = context.theme().danger();
                textColorValue = 0xFFFFFFFF;
            }
            case Variant.GHOST -> {
                baseColor = Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.35F + 0.65F * hoverProgress());
                textColorValue = textColor(context);
            }
            default -> {
                baseColor = hoverBackgroundColor(context);
                textColorValue = textColor(context);
            }
        }

        float pressScale = 1.0F - 0.04F * pressProgress();
        float buttonHeight = height() * pressScale;
        float buttonY = y() + (height() - buttonHeight) * 0.5F;

        if (shadowEnabled(context) && variant == Variant.PRIMARY) {
            context.draw().shadow(x(), buttonY, width(), buttonHeight, radius,
                    context.theme().shadowBlur(), context.theme().shadow());
        }
        context.draw().fillRounded(x(), buttonY, width(), buttonHeight, radius, baseColor);
        if (variant == Variant.DEFAULT) {
            context.draw().strokeRounded(x(), buttonY, width(), buttonHeight, radius,
                    context.theme().borderWidth(), borderColor(context));
        }

        Font resolved = resolvedFont(context);
        float textWidth = resolved.width(label);
        float textX = x() + (width() - textWidth) * 0.5F;
        float textY = buttonY + (buttonHeight - resolved.height()) * 0.5F;
        context.draw().text(textX, textY, resolved.id(), label, textColorValue);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return resolvedFont(context).width(label) + resolvedPadding(context) * 2.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return Math.max(context.theme().controlHeight(), resolvedFont(context).height() + 8.0F);
    }

    /**
     * Visual variants of a button.
     */
    public static final class Variant {
        public static final int DEFAULT = 0;
        public static final int PRIMARY = 1;
        public static final int DANGER = 2;
        public static final int GHOST = 3;

        private Variant() {
        }
    }
}
