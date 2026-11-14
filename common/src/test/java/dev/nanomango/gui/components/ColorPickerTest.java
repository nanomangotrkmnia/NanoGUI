package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.test.TestSupport;
import dev.nanomango.gui.util.Colors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the spectrum square and the shade strip of the colour picker.
 */
class ColorPickerTest {

    private GuiContext context;

    @BeforeEach
    void setUp() {
        context = TestSupport.context();
    }

    private ColorPicker picker() {
        ColorPicker picker = new ColorPicker(0xFF4C8DFF);
        picker.setBounds(0.0F, 0.0F, 180.0F, 180.0F);
        picker.setContext(context);
        picker.setOpen(true);
        picker.render(context);
        return picker;
    }

    private float squareY() {
        return context.theme().controlHeight() + context.theme().spacing();
    }

    private float squareSize() {
        return Math.min(180.0F - context.theme().controlHeight() - 8.0F, 140.0F);
    }

    @Test
    @DisplayName("the spectrum square spans every hue, not only red")
    void spectrumCoversEveryHue() {
        ColorPicker picker = picker();
        float size = squareSize();
        float y = squareY() + size * 0.5F;

        picker.mouseClicked(size * 0.25F, y, 0);
        float[] hsb = new float[3];
        Colors.rgbToHsv(picker.color(), hsb);
        assertEquals(0.25F, hsb[0], 0.02F, "a quarter across must be green-ish");

        // The middle of the square is fully saturated at full brightness.
        assertEquals(0.5F, hsb[1], 0.05F);
        assertEquals(1.0F, hsb[2], 0.02F);

        picker.mouseClicked(size * 0.66F, y, 0);
        Colors.rgbToHsv(picker.color(), hsb);
        assertEquals(0.66F, hsb[0], 0.02F, "two thirds across must be blue-ish");
    }

    @Test
    @DisplayName("the top of the square is white and the bottom is the pure hue")
    void saturationRunsDownTheSquare() {
        ColorPicker picker = picker();
        float size = squareSize();
        float x = size * 0.5F;

        picker.mouseClicked(x, squareY() + 1.0F, 0);
        int white = picker.color();
        assertTrue(Colors.red(white) > 240 && Colors.green(white) > 240 && Colors.blue(white) > 240,
                "the top of the square must be white: " + Integer.toHexString(white));

        picker.mouseClicked(x, squareY() + size - 1.0F, 0);
        float[] hsb = new float[3];
        Colors.rgbToHsv(picker.color(), hsb);
        assertTrue(hsb[1] > 0.9F, "the bottom must be saturated");
    }

    @Test
    @DisplayName("the shade strip chooses the brightness of the selected colour")
    void shadeStrip() {
        ColorPicker picker = picker();
        float size = squareSize();
        float x = size + 8.0F + context.theme().controlHeight() * 0.5F;

        picker.mouseClicked(size * 0.5F, squareY() + size * 0.5F, 0);
        picker.mouseClicked(x, squareY() + 1.0F, 0);
        assertTrue(Colors.red(picker.color()) > 200 || Colors.green(picker.color()) > 200,
                "the top of the strip is the full brightness colour");

        picker.mouseClicked(x, squareY() + size - 1.0F, 0);
        float[] hsb = new float[3];
        Colors.rgbToHsv(picker.color(), hsb);
        assertEquals(0.0F, hsb[2], 0.02F, "the bottom of the strip is black");
    }

    @Test
    @DisplayName("dragging the shade strip keeps updating the colour")
    void dragShade() {
        ColorPicker picker = picker();
        float size = squareSize();
        float x = size + 8.0F + context.theme().controlHeight() * 0.5F;
        AtomicInteger changes = new AtomicInteger();
        picker.onChange(value -> changes.incrementAndGet());

        picker.mouseClicked(size * 0.5F, squareY() + size * 0.5F, 0);
        picker.mouseClicked(x, squareY() + 1.0F, 0);
        picker.mouseDragged(x, squareY() + size * 0.5F, 0, 0.0, 0.0);
        float[] hsb = new float[3];
        Colors.rgbToHsv(picker.color(), hsb);
        assertEquals(0.5F, hsb[2], 0.05F);
        assertTrue(changes.get() >= 3);
    }
}
