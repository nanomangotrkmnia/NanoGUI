package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.util.Colors;
import dev.nanomango.gui.util.MathUtil;

import java.util.function.IntConsumer;

/**
 * A colour picker.
 *
 * <p>The large square shows every hue across its width and every saturation
 * across its height, both at full brightness, so it contains the whole RGB
 * spectrum. The strip on the right chooses the shade (brightness) of the
 * selected hue and saturation.</p>
 */
public class ColorPicker extends Component {

    /** Number of gradient columns used for the spectrum square. */
    private static final int SPECTRUM_COLUMNS = 64;

    private int color = 0xFF4C8DFF;
    private IntConsumer onChange;
    private boolean open;

    private float hue = 0.58F;
    private float saturation = 1.0F;
    private float brightness = 1.0F;

    private boolean draggingSpectrum;
    private boolean draggingShade;

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
        color = Colors.withAlpha(Colors.hsv(hue * 360.0F, saturation, brightness, 1.0F), alpha);
        if (onChange != null) {
            onChange.accept(color);
        }
    }

    @Override
    public void render(GuiContext context) {
        float radius = radius(context);
        float preview = previewSize();

        context.draw().fillRounded(x(), y(), preview, preview, radius, color);
        context.draw().strokeRounded(x(), y(), preview, preview, radius,
                context.theme().borderWidth(), borderColor(context));

        String label = String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF);
        float textY = y() + (preview - context.font().height()) * 0.5F;
        context.draw().text(x() + preview + context.theme().spacing(), textY, context.font().id(), label,
                textColor(context));

        if (!open) {
            return;
        }

        float squareX = squareX();
        float squareY = squareY();
        float size = squareSize();
        float columnWidth = size / SPECTRUM_COLUMNS;

        // Hue is horizontal, saturation is vertical. Every column runs from
        // white at the top to the pure hue at the bottom, so the square holds
        // every RGB colour at full brightness.
        for (int i = 0; i < SPECTRUM_COLUMNS; i++) {
            float columnHue = (float) i / (SPECTRUM_COLUMNS - 1);
            int pure = Colors.hsv(columnHue * 360.0F, 1.0F, 1.0F, 1.0F);
            context.draw().fillGradient(squareX + i * columnWidth, squareY,
                    columnWidth + 1.0F, size, 0.0F, 0xFFFFFFFF, pure);
        }
        context.draw().strokeRounded(squareX, squareY, size, size, radius * 0.5F,
                context.theme().borderWidth(), borderColor(context));

        // Shade strip: the selected colour at full brightness at the top and
        // black at the bottom.
        float stripX = stripX();
        float stripWidth = stripWidth();
        int full = Colors.hsv(hue * 360.0F, saturation, 1.0F, 1.0F);
        context.draw().fillGradient(stripX, squareY, stripWidth, size, stripWidth * 0.5F, full, 0xFF000000);
        context.draw().strokeRounded(stripX, squareY, stripWidth, size, stripWidth * 0.5F,
                context.theme().borderWidth(), borderColor(context));

        // Markers.
        float markerX = squareX + hue * size;
        float markerY = squareY + saturation * size;
        ring(context, markerX, markerY, 4.0F);
        float shadeY = squareY + (1.0F - brightness) * size;
        context.draw().fillRounded(stripX - 1.0F, shadeY - 2.0F, stripWidth + 2.0F, 4.0F, 2.0F, 0xFFFFFFFF);
    }

    private void ring(GuiContext context, float centreX, float centreY, float radius) {
        context.draw().strokeRounded(centreX - radius, centreY - radius, radius * 2.0F, radius * 2.0F,
                radius, 2.0F, 0xFF000000);
        context.draw().strokeRounded(centreX - radius + 1.0F, centreY - radius + 1.0F,
                radius * 2.0F - 2.0F, radius * 2.0F - 2.0F, radius - 1.0F, 1.0F, 0xFFFFFFFF);
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

        float squareX = squareX();
        float squareY = squareY();
        float size = squareSize();

        if (mouseX >= squareX && mouseX < squareX + size
                && mouseY >= squareY && mouseY < squareY + size) {
            draggingShade = false;
            draggingSpectrum = true;
            updateSpectrum(mouseX, mouseY);
            return true;
        }

        float stripX = stripX();
        float stripWidth = stripWidth();
        if (mouseX >= stripX && mouseX < stripX + stripWidth
                && mouseY >= squareY && mouseY < squareY + size) {
            draggingSpectrum = false;
            draggingShade = true;
            updateShade(mouseY);
            return true;
        }

        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!open || button != 0) {
            return false;
        }
        if (draggingSpectrum) {
            updateSpectrum(mouseX, mouseY);
            return true;
        }
        if (draggingShade) {
            updateShade(mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingSpectrum || draggingShade;
        draggingSpectrum = false;
        draggingShade = false;
        return wasDragging || super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSpectrum(double mouseX, double mouseY) {
        float size = Math.max(1.0F, squareSize());
        hue = MathUtil.clamp((float) ((mouseX - squareX()) / size), 0.0F, 1.0F);
        saturation = MathUtil.clamp((float) ((mouseY - squareY()) / size), 0.0F, 1.0F);
        if (brightness <= 0.0F) {
            // Picking a hue from black would stay black, which looks broken.
            brightness = 1.0F;
        }
        applyHsb();
    }

    private void updateShade(double mouseY) {
        float size = Math.max(1.0F, squareSize());
        brightness = MathUtil.clamp(1.0F - (float) ((mouseY - squareY()) / size), 0.0F, 1.0F);
        applyHsb();
    }

    private float controlHeight() {
        return context != null ? context.theme().controlHeight() : 24.0F;
    }

    private float spacing() {
        return context != null ? context.theme().spacing() : 6.0F;
    }

    private float previewSize() {
        return Math.min(width(), controlHeight());
    }

    private float squareX() {
        return x();
    }

    private float squareY() {
        return y() + previewSize() + spacing();
    }

    private float squareSize() {
        return Math.max(1.0F, Math.min(width() - controlHeight() - 8.0F, 140.0F));
    }

    private float stripX() {
        return squareX() + squareSize() + 8.0F;
    }

    private float stripWidth() {
        return Math.min(controlHeight(), Math.max(8.0F, width() - squareSize() - 8.0F));
    }

    @Override
    public float preferredWidth(GuiContext context) {
        return 180.0F;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        if (!open) {
            return context.theme().controlHeight();
        }
        return previewSize() + spacing() * 2.0F + squareSize();
    }
}
