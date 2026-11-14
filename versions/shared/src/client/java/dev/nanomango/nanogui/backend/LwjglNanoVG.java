package dev.nanomango.nanogui.backend;

import dev.nanomango.gui.render.nvg.NanoVG;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVGGL3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_BASELINE;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_CCW;
import static org.lwjgl.nanovg.NanoVG.NVG_CW;
import static org.lwjgl.nanovg.NanoVG.nvgArc;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgClosePath;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgCreateImageRGBA;
import static org.lwjgl.nanovg.NanoVG.nvgDeleteImage;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFillPaint;
import static org.lwjgl.nanovg.NanoVG.nvgFontFaceId;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgImagePattern;
import static org.lwjgl.nanovg.NanoVG.nvgLineTo;
import static org.lwjgl.nanovg.NanoVG.nvgLinearGradient;
import static org.lwjgl.nanovg.NanoVG.nvgMoveTo;
import static org.lwjgl.nanovg.NanoVG.nvgRGBA;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgResetScissor;
import static org.lwjgl.nanovg.NanoVG.nvgRestore;
import static org.lwjgl.nanovg.NanoVG.nvgRoundedRect;
import static org.lwjgl.nanovg.NanoVG.nvgSave;
import static org.lwjgl.nanovg.NanoVG.nvgScissor;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeColor;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeWidth;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;
import static org.lwjgl.nanovg.NanoVG.nvgTranslate;
import static org.lwjgl.nanovg.NanoVG.nvgUpdateImage;

/**
 * {@link NanoVG} implementation backed by the LWJGL NanoVG binding.
 *
 * <p>The context is created by the host, because NanoVG needs a current
 * OpenGL context of the matching version. {@link #createGL3(int)} and
 * {@link #createGL2(int)} create one on the render thread; {@link #close()}
 * releases it again.</p>
 *
 * <p>Colours are converted from the {@code 0xAARRGGBB} representation of the
 * framework to the byte order that NanoVG expects. The conversion objects are
 * reused, so replaying a frame does not allocate.</p>
 */
public final class LwjglNanoVG implements NanoVG {

    private final long context;

    private final NVGColor fill = NVGColor.create();
    private final NVGColor stroke = NVGColor.create();
    private final NVGColor gradientTop = NVGColor.create();
    private final NVGColor gradientBottom = NVGColor.create();
    private final NVGPaint paint = NVGPaint.create();

    /** Buffers that must stay alive for the lifetime of the context. */
    private final List<ByteBuffer> retained = new ArrayList<>();

    private boolean closed;

    private LwjglNanoVG(long context) {
        if (context == 0L) {
            throw new IllegalStateException("NanoVG could not create a context");
        }
        this.context = context;
    }

    /**
     * Creates a NanoVG context that renders with OpenGL 3.
     *
     * <p>The caller must have made an OpenGL 3 context current on the render
     * thread.</p>
     *
     * @param flags combination of the {@code NVG_ANTIALIAS},
     *              {@code NVG_STENCIL_STROKES} and {@code NVG_DEBUG} constants
     *              of {@link org.lwjgl.nanovg.NanoVGGL3}
     */
    public static LwjglNanoVG createGL3(int flags) {
        return new LwjglNanoVG(NanoVGGL3.nvgCreate(flags));
    }

    /**
     * Creates a NanoVG context that renders with OpenGL 2.
     *
     * @param flags combination of the {@code NVG_ANTIALIAS},
     *              {@code NVG_STENCIL_STROKES} and {@code NVG_DEBUG} constants
     *              of {@link org.lwjgl.nanovg.NanoVGGL2}
     */
    public static LwjglNanoVG createGL2(int flags) {
        return new LwjglNanoVG(org.lwjgl.nanovg.NanoVGGL2.nvgCreate(flags));
    }

    /**
     * Wraps a context that was created elsewhere.
     *
     * @param context the native NanoVG context
     */
    public static LwjglNanoVG wrap(long context) {
        return new LwjglNanoVG(context);
    }

    /**
     * The native NanoVG context.
     */
    public long context() {
        return context;
    }

    /**
     * Registers a TrueType font that is stored in memory.
     *
     * @param name the name the font is addressed by
     * @param data the contents of the font file
     * @return the NanoVG font handle
     */
    public int createFont(String name, ByteBuffer data) {
        ByteBuffer direct = direct(data);
        retained.add(direct);
        return nvgCreateFontMem(context, name, direct, false);
    }

    /**
     * Uploads an RGBA8 texture.
     *
     * @param width  width of the texture in pixels
     * @param height height of the texture in pixels
     * @param pixels the pixel data in RGBA order
     * @return the NanoVG image handle
     */
    public int createImage(int width, int height, ByteBuffer pixels) {
        ByteBuffer direct = direct(pixels);
        retained.add(direct);
        return nvgCreateImageRGBA(context, width, height, 0, direct);
    }

    /**
     * Replaces the pixels of an existing image.
     */
    public void updateImage(int handle, int width, int height, ByteBuffer pixels) {
        nvgUpdateImage(context, handle, direct(pixels));
    }

    /**
     * Deletes an image that was created with {@link #createImage}.
     */
    public void deleteImage(int handle) {
        nvgDeleteImage(context, handle);
    }

    /**
     * Releases the NanoVG context.
     */
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        NanoVGGL3.nvgDelete(context);
        retained.clear();
    }

    // ------------------------------------------------------------------
    // NanoVG
    // ------------------------------------------------------------------

    @Override
    public void beginFrame(float width, float height, float pixelRatio) {
        nvgBeginFrame(context, width, height, pixelRatio <= 0.0F ? 1.0F : pixelRatio);
    }

    @Override
    public void endFrame() {
        nvgEndFrame(context);
    }

    @Override
    public void save() {
        nvgSave(context);
    }

    @Override
    public void restore() {
        nvgRestore(context);
    }

    @Override
    public void translate(float x, float y) {
        nvgTranslate(context, x, y);
    }

    @Override
    public void scissor(float x, float y, float width, float height) {
        nvgScissor(context, x, y, width, height);
    }

    @Override
    public void resetScissor() {
        nvgResetScissor(context);
    }

    @Override
    public void fill(float x, float y, float width, float height, int argb) {
        nvgBeginPath(context);
        nvgRect(context, x, y, width, height);
        nvgFillColor(context, colour(fill, argb));
        nvgFill(context);
    }

    @Override
    public void fillRounded(float x, float y, float width, float height, float radius, int argb) {
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, Math.min(radius, Math.min(width, height) * 0.5F));
        nvgFillColor(context, colour(fill, argb));
        nvgFill(context);
    }

    @Override
    public void fillRoundedGradient(float x, float y, float width, float height, float radius,
                                    int topArgb, int bottomArgb) {
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, Math.min(radius, Math.min(width, height) * 0.5F));
        nvgLinearGradient(context, x, y, x, y + height,
                colour(gradientTop, topArgb), colour(gradientBottom, bottomArgb), paint);
        nvgFillPaint(context, paint);
        nvgFill(context);
    }

    @Override
    public void strokeRounded(float x, float y, float width, float height, float radius,
                              float thickness, int argb) {
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, Math.min(radius, Math.min(width, height) * 0.5F));
        nvgStrokeWidth(context, Math.max(0.1F, thickness));
        nvgStrokeColor(context, colour(stroke, argb));
        nvgStroke(context);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2, float thickness, int argb) {
        nvgBeginPath(context);
        nvgMoveTo(context, x1, y1);
        nvgLineTo(context, x2, y2);
        nvgStrokeWidth(context, Math.max(0.1F, thickness));
        nvgStrokeColor(context, colour(stroke, argb));
        nvgStroke(context);
    }

    @Override
    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
        nvgBeginPath(context);
        nvgMoveTo(context, x1, y1);
        nvgLineTo(context, x2, y2);
        nvgLineTo(context, x3, y3);
        nvgClosePath(context);
        nvgFillColor(context, colour(fill, argb));
        nvgFill(context);
    }

    @Override
    public void arc(float centerX, float centerY, float outerRadius, float innerRadius,
                    float startAngle, float endAngle, int argb) {
        nvgBeginPath(context);
        nvgArc(context, centerX, centerY, outerRadius, startAngle, endAngle, NVG_CW);
        if (innerRadius > 0.0F) {
            nvgArc(context, centerX, centerY, innerRadius, endAngle, startAngle, NVG_CCW);
        } else {
            nvgLineTo(context, centerX, centerY);
        }
        nvgClosePath(context);
        nvgFillColor(context, colour(fill, argb));
        nvgFill(context);
    }

    @Override
    public void text(int fontHandle, String text, float x, float baseline, float size, int argb) {
        nvgFontFaceId(context, fontHandle);
        nvgFontSize(context, size);
        nvgTextAlign(context, NVG_ALIGN_LEFT | NVG_ALIGN_BASELINE);
        nvgFillColor(context, colour(fill, argb));
        nvgText(context, x, baseline, text);
    }

    @Override
    public void image(int imageHandle, float x, float y, float width, float height,
                      float u0, float v0, float u1, float v1, int argb) {
        float uSpan = u1 - u0;
        float vSpan = v1 - v0;
        if (uSpan <= 0.0F || vSpan <= 0.0F) {
            return;
        }

        // The pattern covers the whole image. Positioning it so that the
        // requested sub rectangle lands on the destination rectangle draws
        // exactly that sub rectangle.
        float patternWidth = width / uSpan;
        float patternHeight = height / vSpan;
        float patternX = x - u0 * patternWidth;
        float patternY = y - v0 * patternHeight;

        float alpha = ((argb >>> 24) & 0xFF) / 255.0F;
        nvgBeginPath(context);
        nvgRect(context, x, y, width, height);
        nvgImagePattern(context, patternX, patternY, patternWidth, patternHeight, 0.0F,
                imageHandle, alpha, paint);
        nvgFillPaint(context, paint);
        nvgFill(context);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Converts a framework colour into the mutable LWJGL colour object.
     *
     * <p>The byte ordering is spelled out in one place so that the rest of
     * the class can keep working with the {@code 0xAARRGGBB} representation.</p>
     */
    private static NVGColor colour(NVGColor target, int argb) {
        byte red = (byte) ((argb >>> 16) & 0xFF);
        byte green = (byte) ((argb >>> 8) & 0xFF);
        byte blue = (byte) (argb & 0xFF);
        byte alpha = (byte) ((argb >>> 24) & 0xFF);
        return nvgRGBA(red, green, blue, alpha, target);
    }

    /**
     * Copies a buffer into direct memory when necessary.
     */
    private static ByteBuffer direct(ByteBuffer data) {
        if (data.isDirect()) {
            return data;
        }
        ByteBuffer copy = ByteBuffer.allocateDirect(data.remaining()).order(ByteOrder.nativeOrder());
        copy.put(data.duplicate());
        copy.flip();
        return copy;
    }
}
