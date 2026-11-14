package dev.nanomango.gui.render.nvg;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.render.DrawList;
import dev.nanomango.gui.render.RenderBackend;
import dev.nanomango.gui.render.RenderContext;

import java.util.function.IntFunction;

/**
 * Replays a {@link DrawList} against {@link NanoVG}.
 *
 * <p>NanoVG maps almost one to one onto the commands of the draw list, which
 * is why this backend is thin: rounded rectangles, gradients, outlines,
 * shadows, arcs and text all become a handful of NanoVG path calls. The same
 * translation is used by every platform; only the {@link NanoVG}
 * implementation differs (for example the LWJGL binding on desktop).</p>
 *
 * <p>Text is drawn with NanoVG's own font rasteriser when the host registered
 * a {@link NanoVGFont} for the font id. Fonts without a NanoVG handle fall
 * back to the framework's glyph atlas through {@link #glyph(int, int, float,
 * float, float, float, float, float, float, float, int)}, exactly like the
 * Minecraft backend does.</p>
 */
public final class NanoVGRenderBackend implements RenderBackend {

    /** Number of concentric layers used to approximate a soft shadow. */
    private static final int SHADOW_LAYERS = 4;

    private final NanoVG nanoVG;

    private IntFunction<NanoVGFont> fontResolver = id -> null;
    private IntFunction<NanoVGImage> imageResolver = id -> null;
    private FontRegistry fonts;

    private int clipDepth;

    public NanoVGRenderBackend(NanoVG nanoVG) {
        if (nanoVG == null) {
            throw new IllegalArgumentException("The NanoVG implementation must not be null");
        }
        this.nanoVG = nanoVG;
    }

    /**
     * Installs the resolver that maps font ids to NanoVG fonts.
     */
    public NanoVGRenderBackend fontResolver(IntFunction<NanoVGFont> resolver) {
        this.fontResolver = resolver == null ? id -> null : resolver;
        return this;
    }

    /**
     * Installs the resolver that maps texture ids to NanoVG images.
     */
    public NanoVGRenderBackend imageResolver(IntFunction<NanoVGImage> resolver) {
        this.imageResolver = resolver == null ? id -> null : resolver;
        return this;
    }

    /**
     * The NanoVG implementation that this backend draws with.
     */
    public NanoVG nanoVG() {
        return nanoVG;
    }

    @Override
    public void beginFrame(RenderContext context) {
        clipDepth = 0;
        float pixelRatio = context.scale() > 0.0F ? context.scale() : 1.0F;
        nanoVG.beginFrame(context.viewportWidth(), context.viewportHeight(), pixelRatio);
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
                    nanoVG.fill(x, y, width, height, color);
                }
                case DrawList.OP_FILL_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    nanoVG.fillRounded(x, y, width, height, radius, color);
                }
                case DrawList.OP_FILL_GRADIENT -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    int top = commands.intAt(intCursor++);
                    int bottom = commands.intAt(intCursor++);
                    nanoVG.fillRoundedGradient(x, y, width, height, radius, top, bottom);
                }
                case DrawList.OP_STROKE_ROUNDED -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float thickness = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    nanoVG.strokeRounded(x, y, width, height, radius, thickness, color);
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
                    drawText(fontId, text, x, y, color);
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
                    drawImage(textureId, dstX, dstY, dstWidth, dstHeight,
                            srcX, srcY, srcX + srcWidth, srcY + srcHeight, color);
                }
                case DrawList.OP_LINE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    nanoVG.line(x1, y1, x2, y2, 1.0F, color);
                }
                case DrawList.OP_TRIANGLE -> {
                    float x1 = commands.floatAt(floatCursor++);
                    float y1 = commands.floatAt(floatCursor++);
                    float x2 = commands.floatAt(floatCursor++);
                    float y2 = commands.floatAt(floatCursor++);
                    float x3 = commands.floatAt(floatCursor++);
                    float y3 = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    nanoVG.triangle(x1, y1, x2, y2, x3, y3, color);
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
                    nanoVG.arc(x, y, radius, Math.max(0.0F, innerRadius), startAngle, endAngle, color);
                }
                case DrawList.OP_CLIP_PUSH -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float width = commands.floatAt(floatCursor++);
                    float height = commands.floatAt(floatCursor++);
                    nanoVG.save();
                    nanoVG.scissor(x, y, width, height);
                    clipDepth++;
                }
                case DrawList.OP_CLIP_POP -> {
                    if (clipDepth > 0) {
                        nanoVG.restore();
                        clipDepth--;
                    }
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
        NanoVGFont nvgFont = resolveFont(fontId);
        if (nvgFont == null) {
            return;
        }
        Font font = fonts == null ? null : fonts.findById(fontId);
        float baseline = y + (font == null ? 0.0F : font.ascent());
        nanoVG.text(nvgFont.handle(), text, x, baseline, nvgFont.size(), argb);
    }

    @Override
    public void glyph(int fontId, int textureId,
                      float u0, float v0, float u1, float v1,
                      float x, float y, float width, float height, int argb) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        drawImage(textureId, x, y, width, height, u0, v0, u1, v1, argb);
    }

    @Override
    public void endFrame(RenderContext context) {
        // Any clip that the component tree forgot to pop would leak into the
        // next frame, so the remaining states are unwound here.
        while (clipDepth > 0) {
            nanoVG.restore();
            clipDepth--;
        }
        nanoVG.endFrame();
    }

    private void drawText(int fontId, String text, float x, float y, int color) {
        NanoVGFont nvgFont = resolveFont(fontId);
        if (nvgFont != null) {
            Font font = fonts.findById(fontId);
            float baseline = y + (font == null ? 0.0F : font.ascent());
            nanoVG.text(nvgFont.handle(), text, x, baseline, nvgFont.size(), color);
            return;
        }

        // No NanoVG font was registered: let the framework font rasterise the
        // run into the glyph atlas and draw the quads through this backend.
        Font font = fonts == null ? null : fonts.findById(fontId);
        if (font != null) {
            font.draw(this, text, x, y, color);
        }
    }

    private void drawImage(int textureId, float x, float y, float width, float height,
                           float u0, float v0, float u1, float v1, int argb) {
        NanoVGImage image = imageResolver.apply(textureId);
        if (image == null || width <= 0.0F || height <= 0.0F) {
            return;
        }
        nanoVG.image(image.handle(), x, y, width, height, u0, v0, u1, v1, argb);
    }

    private NanoVGFont resolveFont(int fontId) {
        NanoVGFont font = fontResolver.apply(fontId);
        return font != null && font.valid() ? font : null;
    }

    /**
     * Approximates a soft shadow with concentric rounded rectangles.
     *
     * <p>NanoVG has no blur filter, but it does anti-alias every fill. Stacking
     * a few increasingly large and increasingly transparent rounded rectangles
     * produces a smooth falloff that is indistinguishable from a real blur at
     * the sizes used by a GUI.</p>
     */
    private void drawShadow(float x, float y, float width, float height,
                            float radius, float blur, int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
            return;
        }

        float spread = Math.max(0.0F, blur);
        if (spread <= 0.0F) {
            nanoVG.fillRounded(x, y, width, height, radius, argb);
            return;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        // The outermost layer is the largest and the most transparent one. The
        // weights add up to a triangular distribution, so the layers overlap
        // into a smooth falloff instead of a hard edge.
        int weightSum = SHADOW_LAYERS * (SHADOW_LAYERS + 1) / 2;
        for (int layer = SHADOW_LAYERS; layer >= 1; layer--) {
            float t = (float) layer / SHADOW_LAYERS;
            float expand = spread * t;
            int weight = SHADOW_LAYERS - layer + 1;
            int layerAlpha = Math.max(1, Math.round(alpha * weight / (float) weightSum));
            int color = (layerAlpha << 24) | (red << 16) | (green << 8) | blue;
            nanoVG.fillRounded(x - expand, y - expand, width + expand * 2.0F, height + expand * 2.0F,
                    radius + expand, color);
        }
    }
}
