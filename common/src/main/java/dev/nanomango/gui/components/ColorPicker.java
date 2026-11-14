package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.function.IntConsumer;

/**
 * A colour picker with a hue strip, a saturation/value square and a hex
 * input field.
 */
public class ColorPicker extends Component {

    private int color = 0xFF4C8DFF;
    private IntConsumer onChange;
    private boolean open;

    private float hue = 0.58F;
    private float saturation = 1.0F;
    private float brightness = 1.0F;

    private final TextBox hexField = new TextBox();

    public ColorPicker() {
        setHeight(150.0F);
    }

    public ColorPicker(int color) {
        this();
        setColor(color);
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        float[] hsb = new float[3];
        Colors.rgbToHsv(color, hsb);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        hexField.setText(String.format(java.util.Locale.ROOT, "%08X", color));
    }

    public ColorPicker onChange(IntConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private void applyHsb() {
        int alpha = Colors.alpha(color);
        color = Colors.withAlpha(Colors.hsv(hue, saturation, brightness, 1.0F), alpha);
        if (onChange != null) {
            onChange.accept(color);
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        int previewSize = (int) Math.min(width(), context.theme().controlHeight());

        context.draw().fillRounded(x(), y(), previewSize, previewSize, radius, color);
        context.draw().strokeRounded(x(), y(), previewSize, previewSize, radius,
                context.theme().borderWidth(), borderColor(context));

        String label = String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF);
        float textY = y() + (previewSize - context.font().height()) * 0.5F;
        context.draw().text(x() + previewSize + context.theme().spacing(), textY, context.font().id(), label,
                textColor(context));

        if (!open) {
            return;
        }

        float squareX = x();
        float squareY = y() + previewSize + context.theme().spacing();
        float squareSize = Math.min(width() - context.theme().controlHeight() - 8.0F, 100.0F);

        // Saturation / brightness square.
        int steps = 24;
        for (int i = 0; i < steps; i++) {
            for (int j = 0; j < steps; j++) {
                float s = (float) i / (steps - 1);
                float b = 1.0F - (float) j / (steps - 1);
                int cellColor = Colors.hsv(hue, s, b, 1.0F);
                context.draw().fillRect(squareX + i * (squareSize / steps), squareY + j * (squareSize / steps),
                        squareSize / steps + 1.0F, squareSize / steps + 1.0F, cellColor);
            }
        }

        // Hue strip.
        float stripX = squareX + squareSize + 8.0F;
        float stripWidth = context.theme().controlHeight();
        for (int i = 0; i < 16; i++) {
            int hueColor = Colors.hsv(i / 16.0F, 1.0F, 1.0F, 1.0F);
            context.draw().fillRect(stripX, squareY + i * (squareSize / 16.0F), stripWidth,
                    squareSize / 16.0F + 1.0F, hueColor);
        }

        // Selection markers.
        float markerX = squareX + saturation * squareSize;
        float markerY = squareY + (1.0F - brightness) * squareSize;
        context.draw().strokeRounded(markerX - 4.0F, markerY - 4.0F, 8.0F, 8.0F, 4.0F, 1.0F, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        if (!open) {
            if (isMouseOver(mouseX, mouseY)) {
                setOpen(true);
                return true;
            }
            return false;
        }

        float previewSize = Math.min(width(), context0ControlHeight());
        float squareX = x();
        float squareY = y() + previewSize + context0Spacing();
        float squareSize = Math.min(width() - context0ControlHeight() - 8.0F, 100.0F);

        if (mouseX >= squareX && mouseX < squareX + squareSize
                && mouseY >= squareY && mouseY < squareY + squareSize) {
            saturation = MathUtil.clamp((float) ((mouseX - squareX) / squareSize), 0.0F, 1.0F);
            brightness = MathUtil.clamp(1.0F - (float) ((mouseY - squareY) / squareSize), 0.0F, 1.0F);
            applyHsb();
            return true;
        }

        float stripX = squareX + squareSize + 8.0F;
        float stripWidth = context0ControlHeight();
        if (mouseX >= stripX && mouseX < stripX + stripWidth
                && mouseY >= squareY && mouseY < squareY + squareSize) {
            hue = MathUtil.clamp((float) ((mouseY - squareY) / squareSize), 0.0F, 1.0F);
            applyHsb();
            return true;
        }

        return true;
    }

    private float context0ControlHeight() {
        return 24.0F;
    }

    private float context0Spacing() {
        return 6.0F;
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 180.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return open ? 170.0F : context.theme().controlHeight();
    }
}
