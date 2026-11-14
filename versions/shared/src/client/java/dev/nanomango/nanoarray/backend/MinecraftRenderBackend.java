package dev.nanomango.nanoarray.backend;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderBackend;
import dev.nanomango.gui.render.RenderContext;
import dev.nanomango.gui.util.Colors;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Renders the GUI with Minecraft's own drawing primitives.
 *
 * <p>The backend is available on every supported Minecraft version and on
 * every graphics backend, including Vulkan. Rounded corners are approximated
 * with axis aligned rectangles, see {@link RoundedRectangles}.</p>
 */
public final class MinecraftRenderBackend implements RenderBackend {

    private final NanoArrayGraphics graphics;
    private final Deque<int[]> scissorStack = new ArrayDeque<>();

    public MinecraftRenderBackend(NanoArrayGraphics graphics) {
        this.graphics = graphics;
    }

    @Override
    public void beginFrame(RenderContext context) {
        scissorStack.clear();
    }

    @Override
    public void render(RenderContext context, DrawList commands, FontRegistry fonts) {
        int opCursor = 0;
        int floatCursor = 0;
        int intCursor = 0;

        for (int command = 0; command < commands.commandCount(); command++) {
            int opcode = commands.opcodes(opCursor++);
            switch (opcode) {
                case DrawList.OP_FILL_RECT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    graphics.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), color);
                }
                case DrawList.OP_FILL_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    RoundedRectangles.draw(graphics, x, y, w, h, radius, color);
                }
                case DrawList.OP_FILL_GRADIENT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++); // radius, approximated
                    int top = commands.intAt(intCursor++);
                    int bottom = commands.intAt(intCursor++);
                    graphics.fillGradient(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h),
                            top, bottom);
                }
                case DrawList.OP_STROKE_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float thickness = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    RoundedRectangles.stroke(graphics, x, y, w, h, radius, thickness, color);
                }
                case DrawList.OP_SHADOW -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++); // blur
                    int color = commands.intAt(intCursor++);
                    RoundedRectangles.draw(graphics, x, y, w, h, radius, color);
                }
                case DrawList.OP_TEXT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    int fontId = commands.intAt(intCursor++);
                    int color = commands.intAt(intCursor++);
                    int stringIndex = commands.intAt(intCursor++);
                    String text = commands.stringAt(stringIndex);
                    Font font = fonts.byId(fontId);
                    if (font instanceof dev.nanomango.nanoarray.font.MinecraftFont minecraftFont) {
                        graphics.drawString(minecraftFont.minecraftFont(), text, Math.round(x), Math.round(y), color);
                    } else {
                        // Fonts that are not backed by the Minecraft atlas are
                        // drawn through the vector backend instead.
                        font.draw(this, text, x, y, color);
                    }
                }
                case DrawList.OP_TEXTURE -> {
                    commands.floatAt(floatCursor + 7);
                    floatCursor += 8;
                    intCursor += 2;
                }
                case DrawList.OP_LINE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawLine(x1, y1, x2, y2, color);
                }
                case DrawList.OP_TRIANGLE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    float x3 = commands.floatAt(floatCursor++);
                    float y3 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawTriangle(x1, y1, x2, y2, x3, y3, color);
                }
                case DrawList.OP_ARC -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawArc(x, y, radius, color);
                }
                case DrawList.OP_CLIP_PUSH -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    pushScissor(x, y, w, h);
                }
                case DrawList.OP_CLIP_POP -> {
                    popScissor();
                }
                default -> {
                    // Unknown opcodes are skipped safely.
                    floatCursor += DrawList.floatArgCount(opcode);
                    intCursor += DrawList.intArgCount(opcode);
                }
            }
        }
    }

    @Override
    public void builtin(int fontId, String text, float x, float y, int argb) {
        // The Minecraft backend draws text through the Minecraft font atlas.
        // Fonts that are not backed by that atlas are handled by the vector
        // renderer instead.
    }

    @Override
    public void glyph(int fontId, int textureId,
                      float u0, float v0, float u1, float v1,
                      float x, float y, float width, float height, int argb) {
        // Rasterised glyphs require a texture capable backend. The Minecraft
        // backend only supports the built in font atlas.
    }

    @Override
    public void endFrame(RenderContext context) {
        while (!scissorStack.isEmpty()) {
            graphics.disableScissor();
            scissorStack.pop();
        }
    }

    private void pushScissor(float x, float y, float width, float height) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        if (!scissorStack.isEmpty()) {
            int[] parent = scissorStack.peek();
            left = Math.max(left, parent[0]);
            top = Math.max(top, parent[1]);
            right = Math.min(right, parent[2]);
            bottom = Math.min(bottom, parent[3]);
        }
        scissorStack.push(new int[]{left, top, right, bottom});
        graphics.enableScissor(left, top, right, bottom);
    }

    private void popScissor() {
        if (scissorStack.isEmpty()) {
            return;
        }
        scissorStack.pop();
        graphics.disableScissor();
    }

    private void drawLine(float x1, float y1, float x2, float y2, int color) {
        int ax = Math.round(x1);
        int ay = Math.round(y1);
        int bx = Math.round(x2);
        int by = Math.round(y2);

        if (ax == bx) {
            int top = Math.min(ay, by);
            int bottom = Math.max(ay, by);
            graphics.fill(ax, top, ax + 1, bottom, color);
            return;
        }
        if (ay == by) {
            int left = Math.min(ax, bx);
            int right = Math.max(ax, bx);
            graphics.fill(left, ay, right, ay + 1, color);
            return;
        }

        // Diagonal lines are approximated with a short staircase.
        int steps = Math.min(64, Math.max(Math.abs(bx - ax), Math.abs(by - ay)));
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int px = Math.round(ax + (bx - ax) * t);
            int py = Math.round(ay + (by - ay) * t);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        int minY = Math.round(Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.round(Math.max(y1, Math.max(y2, y3)));
        int bands = Math.min(32, Math.max(1, maxY - minY));

        for (int band = 0; band < bands; band++) {
            float t0 = (float) band / bands;
            float t1 = (float) (band + 1) / bands;
            float topY = minY + (maxY - minY) * t0;
            float bottomY = minY + (maxY - minY) * t1;

            float left = Float.MAX_VALUE;
            float right = -Float.MAX_VALUE;
            boolean any = false;
            float[][] points = {{x1, y1}, {x2, y2}, {x3, y3}};
            for (int i = 0; i < 3; i++) {
                float ax = points[i][0];
                float ay = points[i][1];
                float bx = points[(i + 1) % 3][0];
                float by = points[(i + 1) % 3][1];
                if (ay == by) {
                    continue;
                }
                float lower = Math.min(ay, by);
                float upper = Math.max(ay, by);
                if (upper < topY || lower > bottomY) {
                    continue;
                }
                float t = (topY - ay) / (by - ay);
                float edgeX = ax + (bx - ax) * t;
                left = Math.min(left, edgeX);
                right = Math.max(right, edgeX);
                any = true;
            }
            if (any && right > left) {
                graphics.fill(Math.round(left), Math.round(topY), Math.round(right), Math.round(bottomY) + 1, color);
            }
        }
    }

    private void drawArc(float x, float y, float radius, int color) {
        int steps = 16;
        int inner = Math.max(1, Math.round(radius * 0.6F));
        for (int i = 0; i < steps; i++) {
            double angle = Math.PI * 2.0 * i / steps;
            int px = Math.round(x + (float) Math.cos(angle) * radius);
            int py = Math.round(y + (float) Math.sin(angle) * radius);
            graphics.fill(px, py, px + 1, py + 1, color);
        }
        if (inner > 0) {
            // The inner hole is punched out by the caller drawing the
            // background over it, so nothing has to be done here.
            return;
        }
    }

    /**
     * Colour helper used by the components.
     */
    public static int fade(int color, float factor) {
        return Colors.withAlphaFactor(color, factor);
    }
}
