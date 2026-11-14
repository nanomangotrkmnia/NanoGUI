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
 * every graphics backend, including Vulkan. Rounded rectangles are drawn
 * with two plain rectangles and four anti-aliased corner sprites, see
 * {@link RoundedRectangles}.</p>
 *
 * <p>Text is drawn in two different ways. The Minecraft font is rendered by
 * Minecraft itself, the rasterising fonts of the framework (the native
 * renderer and the Java2D fallback) push their glyphs into
 * {@link #glyph(int, int, float, float, float, float, float, float, float, float, int)},
 * where they are blitted from the glyph atlas.</p>
 */
public final class MinecraftRenderBackend implements RenderBackend {

    private NanoArrayGraphics graphics;
    private final FontAtlasRegistry atlases;
    private final RoundedRectangles rounded;
    private final Deque<int[]> scissorStack = new ArrayDeque<>();

    /** Registry of the fonts of the current frame. */
    private FontRegistry fonts;

    /** Resolves the numeric texture ids of the draw list. */
    private TextureResolver textureResolver;

    public MinecraftRenderBackend(FontAtlasRegistry atlases, RoundedCornerAtlas corners) {
        this.atlases = atlases;
        this.rounded = new RoundedRectangles(corners);
    }

    /**
     * Installs the graphics object of the current frame.
     *
     * <p>Minecraft creates a new graphics object for every frame, so the
     * backend keeps a single instance and only swaps the target.</p>
     */
    public MinecraftRenderBackend graphics(NanoArrayGraphics graphics) {
        this.graphics = graphics;
        return this;
    }

    /**
     * Installs the resolver that maps the texture ids of the draw list to
     * Minecraft textures.
     */
    public MinecraftRenderBackend textureResolver(TextureResolver resolver) {
        this.textureResolver = resolver;
        return this;
    }

    @Override
    public void beginFrame(RenderContext context) {
        scissorStack.clear();
    }

    @Override
    public void render(RenderContext context, DrawList commands, FontRegistry fonts) {
        if (graphics == null) {
            return;
        }
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
                    rounded.draw(graphics, x, y, w, h, radius, color);
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
                    rounded.stroke(graphics, x, y, w, h, radius, thickness, color);
                }
                case DrawList.OP_SHADOW -> {
                    float x = commands.floatAt(floatCursor++);
                    float y = commands.floatAt(floatCursor++);
                    float w = commands.floatAt(floatCursor++);
                    float h = commands.floatAt(floatCursor++);
                    float radius = commands.floatAt(floatCursor++);
                    float blur = commands.floatAt(floatCursor++);
                    int color = commands.intAt(intCursor++);
                    rounded.shadow(graphics, x, y, w, h, radius, blur, color);
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
                        // Fonts that are not backed by the Minecraft atlas push
                        // their glyphs into this backend.
                        font.draw(this, text, x, y, color);
                    }
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
                    // The texture has to be registered by the host, textures
                    // that are unknown are simply skipped.
                    drawTexture(textureId, srcX, srcY, srcWidth, srcHeight,
                            dstX, dstY, dstWidth, dstHeight, color);
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
        if (width <= 0.0F || height <= 0.0F || fonts == null || graphics == null) {
            return;
        }
        Font font = fonts.findById(fontId);
        if (font == null) {
            return;
        }
        FontAtlasRegistry.Atlas atlas = atlases.atlas(fontId, font.texture());
        if (atlas == null) {
            return;
        }

        int textureWidth = atlas.width();
        int textureHeight = atlas.height();

        // The source rectangle is addressed in pixels of the atlas.
        int sourceX = Math.round(u0 * textureWidth);
        int sourceY = Math.round(v0 * textureHeight);
        int sourceWidth = Math.max(1, Math.round((u1 - u0) * textureWidth));
        int sourceHeight = Math.max(1, Math.round((v1 - v0) * textureHeight));

        // Minecraft draws at integer positions. The atlas is rasterised at the
        // physical resolution, so rounding a glyph to the nearest GUI pixel
        // keeps it one to one on screen instead of stretching it.
        int destinationX = Math.round(x);
        int destinationY = Math.round(y);
        int destinationWidth = Math.max(1, Math.round(width));
        int destinationHeight = Math.max(1, Math.round(height));

        graphics.blit(atlas.identifier(), sourceX, sourceY,
                destinationX, destinationY, destinationWidth, destinationHeight,
                textureWidth, textureHeight, argb);
    }

    @Override
    public void endFrame(RenderContext context) {
        if (graphics == null) {
            return;
        }
        while (!scissorStack.isEmpty()) {
            graphics.disableScissor();
            scissorStack.pop();
        }
    }

    private void drawTexture(int textureId, float srcX, float srcY, float srcWidth, float srcHeight,
                             float dstX, float dstY, float dstWidth, float dstHeight, int color) {
        // Textures are registered by the host application, the framework only
        // knows their numeric id.
        NanoArrayTexture texture = textureResolver == null ? null : textureResolver.resolve(textureId);
        if (texture == null) {
            return;
        }
        int textureWidth = Math.max(1, texture.width());
        int textureHeight = Math.max(1, texture.height());

        graphics.blit(texture.identifier(),
                srcX * textureWidth, srcY * textureHeight,
                Math.round(dstX), Math.round(dstY),
                Math.max(1, Math.round(dstWidth)), Math.max(1, Math.round(dstHeight)),
                textureWidth, textureHeight, color);
    }

    /**
     * Resolves the numeric texture ids of the draw list.
     */
    @FunctionalInterface
    public interface TextureResolver {
        NanoArrayTexture resolve(int textureId);
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
