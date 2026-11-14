package dev.nanomango.gui.render.imgui;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderBackend;
import dev.nanomango.gui.render.RenderContext;

/**
 * Replays a {@link DrawList} against {@link ImGuiDrawList}.
 *
 * <p>Dear ImGui has a primitive for almost every command of the draw list, so
 * the translation is direct. The backend is the Java half of the integration;
 * the native binding implements {@link ImGuiDrawList} and forwards the calls
 * to {@code ImDrawList}.</p>
 *
 * <p><strong>Status.</strong> The translation is complete and unit tested.
 * Rendering it inside Minecraft additionally needs a custom
 * {@code RenderPipeline}: Minecraft's public GUI API only accepts rectangles
 * and textures, not the arbitrary triangles that an ImGui draw list contains.
 * The mod therefore does not claim an in game ImGui renderer; the Minecraft
 * GUI uses {@code MinecraftRenderBackend}. See {@code docs/DEVELOPING.md}.</p>
 */
public final class ImGuiRenderBackend implements RenderBackend {

    /** Number of concentric layers used to approximate a soft shadow. */
    private static final int SHADOW_LAYERS = 4;

    private final ImGuiDrawList drawList;
    private FontRegistry fonts;
    private int clipDepth;

    public ImGuiRenderBackend(ImGuiDrawList drawList) {
        if (drawList == null) {
            throw new IllegalArgumentException("The ImGui draw list must not be null");
        }
        this.drawList = drawList;
    }

    /**
     * The draw list that this backend writes to.
     */
    public ImGuiDrawList drawList() {
        return drawList;
    }

    @Override
    public void beginFrame(RenderContext context) {
        clipDepth = 0;
    }

    @Override
    public void render(RenderContext context, DrawList commands, FontRegistry fonts) {
        this.fonts = fonts;

        int opCursor = 0;
        int floatCursor = 0;
        int intCursor = 0;

        for (int command = 0; command < commands.commandCount(); command++) {
            int opcode = commands.opcodes(opCursor++);
            switch (opcode) {
                case DrawList.OP_FILL_RECT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addRectFilled(x, y, x + width, y + height, color, 0.0F);
                }
                case DrawList.OP_FILL_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addRectFilled(x, y, x + width, y + height, color, radius);
                }
                case DrawList.OP_FILL_GRADIENT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++); // radius, approximated by ImGui
                    int top = commands.intAt(intCursor++);
                    int bottom = commands.intAt(intCursor++);
                    // A vertical gradient means both top corners share the top
                    // colour and both bottom corners the bottom colour.
                    drawList.addRectFilledMultiColor(x, y, x + width, y + height,
                            top, top, bottom, bottom);
                }
                case DrawList.OP_STROKE_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float thickness = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addRect(x, y, x + width, y + height, color, radius, thickness);
                }
                case DrawList.OP_SHADOW -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float blur = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawShadow(x, y, width, height, radius, blur, color);
                }
                case DrawList.OP_TEXT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    int fontId = commands.intAt(intCursor++);
                    int color = commands.intAt(intCursor++);
                    int stringIndex = commands.intAt(intCursor++);
                    String text = commands.stringAt(stringIndex);
                    Font font = fonts == null ? null : fonts.findById(fontId);
                    float size = font == null ? 12.0F : font.height();
                    drawList.addText(fontId, size, x, y, color, text);
                }
                case DrawList.OP_TEXTURE -> {
                    float srcX = commands.floatAt(floatCursor++);
                    float srcY = commands.floatAt(floatCursor++);
                    float srcWidth = commands.floatAt(floatCursor++);
                    float srcHeight = commands.floatAt(floatCursor++);
                    float dstX = commands.floatAt(floatCursor++);
                    float dstY = commands.floatAt(floatCursor++);
                    float dstWidth = commands.floatAt(floatCursor++);
                    float dstHeight = commands.floatAt(floatCursor++);
                    int textureId = commands.intAt(intCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addImage(textureId, dstX, dstY, dstX + dstWidth, dstY + dstHeight,
                            srcX, srcY, srcX + srcWidth, srcY + srcHeight, color);
                }
                case DrawList.OP_LINE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addLine(x1, y1, x2, y2, color, 1.0F);
                }
                case DrawList.OP_TRIANGLE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    float x3 = commands.floatAt(floatCursor++);
                    float y3 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    drawList.addTriangleFilled(x1, y1, x2, y2, x3, y3, color);
                }
                case DrawList.OP_ARC -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float startAngle = commands.floatAt(floatCursor++);
                    float endAngle = commands.floatAt(floatCursor++);
                    float innerRadius = commands.floatAt(floatCursor++);
                    commands.floatAt(floatCursor++); // reserved
                    int color = commands.intAt(intCursor++);
                    drawList.addArcFilled(x, y, radius, Math.max(0.0F, innerRadius),
                            startAngle, endAngle, color);
                }
                case DrawList.OP_CLIP_PUSH -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    drawList.pushClipRect(x, y, x + width, y + height, true);
                    clipDepth++;
                }
                case DrawList.OP_CLIP_POP -> {
                    if (clipDepth > 0) {
                        drawList.popClipRect();
                        clipDepth--;
                    }
                }
                default -> {
                    floatCursor += DrawList.floatArgCount(opcode);
                    intCursor += DrawList.intArgCount(opcode);
                }
            }
        }
    }

    @Override
    public void builtin(int fontId, String text, float x, float y, int argb) {
        // Fonts that defer to the host font are drawn as ImGui text runs, which
        // is handled in the OP_TEXT case above.
    }

    @Override
    public void glyph(int fontId, int textureId,
                      float u0, float v0, float u1, float v1,
                      float x, float y, float width, float height, int argb) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        drawList.addImage(textureId, x, y, x + width, y + height, u0, v0, u1, v1, argb);
    }

    @Override
    public void endFrame(RenderContext context) {
        while (clipDepth > 0) {
            drawList.popClipRect();
            clipDepth--;
        }
    }

    private void drawShadow(float x, float y, float width, float height,
                            float radius, float blur, int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return;
        }

        float spread = Math.max(0.0F, blur);
        if (spread <= 0.0F) {
            drawList.addRectFilled(x, y, x + width, y + height, argb, radius);
            return;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        int weightSum = SHADOW_LAYERS * (SHADOW_LAYERS + 1) / 2;

        for (int layer = SHADOW_LAYERS; layer >= 1; layer--) {
            float t = (float) layer / SHADOW_LAYERS;
            float expand = spread * t;
            int weight = SHADOW_LAYERS - layer + 1;
            int layerAlpha = Math.max(1, Math.round(alpha * weight / (float) weightSum));
            int color = (layerAlpha << 24) | (red << 16) | (green << 8) | blue;
            drawList.addRectFilled(x - expand, y - expand,
                    x + width + expand, y + height + expand, color, radius + expand);
        }
    }
}
