package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.test.TestSupport;
import dev.nanomango.gui.util.Colors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.nanomango.gui.render.DrawList;

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

    @Test
    @DisplayName("the gradient fills stay inside the rounded corners")
    void gradientsStayInsideTheRoundedCorners() {
        ColorPicker picker = picker();
        float size = squareSize();
        float squareTop = squareY();
        List<Gradient> gradients = gradients();

        // The shade strip must be drawn as a rounded gradient, not as a
        // square one that leaks out of its outline.
        Gradient strip = gradients.stream()
                .filter(gradient -> gradient.x() > size && gradient.y() >= squareTop)
                .findFirst().orElseThrow(() -> new AssertionError("the shade strip gradient is missing"));
        assertTrue(strip.radius() > 0.0F, "the shade strip must be drawn with rounded corners");

        List<Gradient> columns = gradients.stream()
                .filter(gradient -> gradient.x() < size && gradient.y() >= squareTop)
                .sorted(Comparator.comparingDouble(Gradient::x))
                .toList();
        assertTrue(columns.size() >= size - 1, "the square must be filled column by column");

        // The outermost columns are clipped to the rounded silhouette while
        // the middle columns span the whole square.
        Gradient first = columns.get(0);
        assertTrue(first.y() > squareTop,
                "the left column must start below the top edge, was " + first.y());
        assertTrue(first.y() + first.height() < squareTop + size,
                "the left column must end above the bottom edge");
        Gradient middle = columns.get(columns.size() / 2);
        assertEquals(Math.round(squareTop), middle.y(), 0.001F,
                "a middle column must span the full height");

        // The clipped column starts part way down the white to hue ramp, so
        // its top colour must be mixed toward white instead of being pure.
        assertTrue(Colors.green(first.top()) > 0 && Colors.blue(first.top()) > 0,
                "the clipped column must start on the ramp, was "
                        + Integer.toHexString(first.top()));
    }

    /** One emitted OP_FILL_GRADIENT command. */
    private record Gradient(float x, float y, float width, float height, float radius,
                            int top, int bottom) {
    }

    /** Collects the gradient commands of the last rendered frame. */
    private List<Gradient> gradients() {
        DrawList draw = context.draw();
        List<Gradient> result = new ArrayList<>();
        int floatCursor = 0;
        int intCursor = 0;
        for (int index = 0; index < draw.commandCount(); index++) {
            int opcode = draw.opcodes(index);
            if (opcode == DrawList.OP_FILL_GRADIENT) {
                result.add(new Gradient(draw.floatAt(floatCursor), draw.floatAt(floatCursor + 1),
                        draw.floatAt(floatCursor + 2), draw.floatAt(floatCursor + 3),
                        draw.floatAt(floatCursor + 4), draw.intAt(intCursor),
                        draw.intAt(intCursor + 1)));
            }
            floatCursor += DrawList.floatArgCount(opcode);
            intCursor += DrawList.intArgCount(opcode);
        }
        return result;
    }
}
