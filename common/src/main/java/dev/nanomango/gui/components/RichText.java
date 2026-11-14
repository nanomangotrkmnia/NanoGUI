package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.font.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * A label that mixes differently coloured and styled runs of text.
 *
 * <p>Rich text is built from {@link Span}s. Every span carries its own
 * colour and can use a different font, which makes it possible to render
 * things like {@code [Enabled]} with the accent colour inside a sentence.</p>
 */
public class RichText extends Component {

    private final List<Span> spans = new ArrayList<>();
    private Alignment alignment = Alignment.LEFT;
    private float lineHeight = -1.0F;

    public RichText() {
    }

    public RichText(Span... spans) {
        for (Span span : spans) {
            this.spans.add(span);
        }
    }

    public RichText add(String text) {
        spans.add(new Span(text, null, -1));
        return this;
    }

    public RichText add(Span span) {
        spans.add(span);
        return this;
    }

    public RichText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public RichText lineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public List<Span> spans() {
        return spans;
    }

    public String plainText() {
        StringBuilder builder = new StringBuilder();
        for (Span span : spans) {
            builder.append(span.text());
        }
        return builder.toString();
    }

    private Font fontFor(GuiContext context, Span span) {
        if (span.font() != null) {
            return span.font();
        }
        return context.theme().regularFont() != null ? context.theme().regularFont() : context.font();
    }

    @Override
    public void render(GuiContext context) {
        float total = 0.0F;
        for (Span span : spans) {
            total += fontFor(context, span).width(span.text());
        }
        float cursor = x();
        switch (alignment) {
            case CENTER -> cursor = x() + (width() - total) * 0.5F;
            case RIGHT -> cursor = x() + width() - total;
            default -> {
            }
        }
        for (Span span : spans) {
            Font font = fontFor(context, span);
            int color = span.color() >= 0 ? span.color() : textColor(context);
            float textY = y() + (lineHeight >= 0.0F ? (lineHeight - font.height()) * 0.5F : 0.0F);
            context.draw().text(cursor, textY, font.id(), span.text(), color);
            cursor += font.width(span.text());
        }
    }

    @Override
    public float preferredWidth(GuiContext context) {
        float total = 0.0F;
        for (Span span : spans) {
            total += fontFor(context, span).width(span.text());
        }
        return total;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        if (lineHeight >= 0.0F) {
            return lineHeight;
        }
        float max = 0.0F;
        for (Span span : spans) {
            max = Math.max(max, fontFor(context, span).height());
        }
        return max;
    }

    /**
     * Horizontal alignment of the text block.
     */
    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * A styled run of text.
     *
     * @param text  the text
     * @param font  optional font override
     * @param color packed colour, or {@code -1} to use the default text colour
     */
    public record Span(String text, Font font, int color) {

        public static Span of(String text, int color) {
            return new Span(text, null, color);
        }

        public static Span of(String text, Font font, int color) {
            return new Span(text, font, color);
        }
    }
}
