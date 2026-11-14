package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;

/**
 * A single line of text.
 *
 * <p>The label supports left, centre and right alignment and can be drawn with
 * a drop shadow, which keeps it readable on top of bright backgrounds.</p>
 */
public class Text extends Component {

    private String text;
    private Font font;
    private int color = -1;
    private Alignment alignment = Alignment.LEFT;
    private boolean shadowed;
    private int shadowColor = 0x80000000;
    private float lineHeight = -1.0F;

    public Text(String text) {
        this.text = text == null ? "" : text;
    }

    public Text(String text, Font font) {
        this(text);
        this.font = font;
    }

    public String text() {
        return text;
    }

    public Text text(String text) {
        this.text = text == null ? "" : text;
        return this;
    }

    public Text font(Font font) {
        this.font = font;
        return this;
    }

    public Text color(int color) {
        this.color = color;
        return this;
    }

    public Text alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Text shadowed(boolean shadowed) {
        this.shadowed = shadowed;
        return this;
    }

    public Text shadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    public Text lineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    /**
     * Font used to draw this label. Falls back to the theme font when none was
     * set explicitly.
     */
    public Font resolvedFont(GuiContext context) {
        if (font != null) {
            return font;
        }
        return context.theme().regularFont() != null ? context.theme().regularFont() : context.font();
    }

    @Override
    public void render(GuiContext context) {
        Font resolved = resolvedFont(context);
        int effective = color >= 0 ? color : textColor(context);
        float textX = x();
        float textWidth = resolved.width(text);
        switch (alignment) {
            case CENTER -> textX = x() + (width() - textWidth) * 0.5F;
            case RIGHT -> textX = x() + width() - textWidth;
            default -> {
            }
        }
        float textY = y() + (lineHeight >= 0.0F ? (lineHeight - resolved.height()) * 0.5F : 0.0F);
        if (shadowed) {
            context.draw().text(textX + 1.0F, textY + 1.0F, resolved.id(), text, shadowColor);
        }
        context.draw().text(textX, textY, resolved.id(), text, effective);
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return resolvedFont(context).width(text);
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return lineHeight >= 0.0F ? lineHeight : resolvedFont(context).height();
    }

    /**
     * Horizontal alignment of the label inside its bounds.
     */
    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
