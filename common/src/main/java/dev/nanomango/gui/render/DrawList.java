package dev.nanomango.gui.render;

import java.util.Arrays;

/**
 * A flat, allocation free command stream that describes everything the GUI
 * wants to draw for a single frame.
 *
 * <p>The framework never touches a graphics API directly. Components append
 * commands to a {@code DrawList} and a {@link RenderBackend} replays the list
 * against the host (Minecraft {@code GuiGraphics} / {@code GuiGraphicsExtractor},
 * NanoVG or Dear ImGui).</p>
 *
 * <p>The stream is stored in parallel primitive arrays which keeps the hot
 * path free of object allocation and lets backends iterate the data with a
 * simple cursor walk. Text is kept in a separate string pool so that repeated
 * labels are stored once per frame.</p>
 */
public final class DrawList {

    // ---- opcodes ---------------------------------------------------------
    public static final int OP_FILL_RECT = 1;
    public static final int OP_FILL_ROUNDED = 2;
    public static final int OP_FILL_GRADIENT = 3;
    public static final int OP_STROKE_ROUNDED = 4;
    public static final int OP_SHADOW = 5;
    public static final int OP_TEXT = 6;
    public static final int OP_TEXTURE = 7;
    public static final int OP_LINE = 8;
    public static final int OP_TRIANGLE = 9;
    public static final int OP_CLIP_PUSH = 10;
    public static final int OP_CLIP_POP = 11;
    public static final int OP_ARC = 12;

    // ---- argument tables -------------------------------------------------
    private static final int[] FLOAT_ARGS = new int[16];
    private static final int[] INT_ARGS = new int[16];

    static {
        FLOAT_ARGS[OP_FILL_RECT] = 4;
        INT_ARGS[OP_FILL_RECT] = 1;

        FLOAT_ARGS[OP_FILL_ROUNDED] = 5;
        INT_ARGS[OP_FILL_ROUNDED] = 1;

        FLOAT_ARGS[OP_FILL_GRADIENT] = 5;
        INT_ARGS[OP_FILL_GRADIENT] = 2;

        FLOAT_ARGS[OP_STROKE_ROUNDED] = 6;
        INT_ARGS[OP_STROKE_ROUNDED] = 1;

        FLOAT_ARGS[OP_SHADOW] = 6;
        INT_ARGS[OP_SHADOW] = 1;

        FLOAT_ARGS[OP_TEXT] = 2;
        INT_ARGS[OP_TEXT] = 3;

        FLOAT_ARGS[OP_TEXTURE] = 8;
        INT_ARGS[OP_TEXTURE] = 2;

        FLOAT_ARGS[OP_LINE] = 4;
        INT_ARGS[OP_LINE] = 1;

        FLOAT_ARGS[OP_TRIANGLE] = 6;
        INT_ARGS[OP_TRIANGLE] = 1;

        FLOAT_ARGS[OP_CLIP_PUSH] = 4;
        FLOAT_ARGS[OP_CLIP_POP] = 0;

        FLOAT_ARGS[OP_ARC] = 7; // x, y, radius, startAngle, endAngle, innerRadius, unused
        INT_ARGS[OP_ARC] = 1;
    }

    private int[] opcodes = new int[512];
    private float[] floats = new float[2048];
    private int[] ints = new int[512];
    private String[] strings = new String[64];

    private int commandCount;
    private int floatCursor;
    private int intCursor;
    private int stringCursor;

    /**
     * Current clipping rectangle in GUI space. Every {@link #clipPush} must be
     * balanced by a {@link #clipPop}.
     */
    private float clipX;
    private float clipY;
    private float clipWidth;
    private float clipHeight;
    private int clipDepth;

    public void clear() {
        commandCount = 0;
        floatCursor = 0;
        intCursor = 0;
        stringCursor = 0;
        clipDepth = 0;
        clipX = 0.0F;
        clipY = 0.0F;
        clipWidth = 0.0F;
        clipHeight = 0.0F;
    }

    public int commandCount() {
        return commandCount;
    }

    public int clipDepth() {
        return clipDepth;
    }

    // ----------------------------------------------------------------------
    // Command emission
    // ----------------------------------------------------------------------

    public void fillRect(float x, float y, float width, float height, int argb) {
        ensureCapacity(1, 4, 1);
        opcodes[commandCount++] = OP_FILL_RECT;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        ints[intCursor++] = argb;
    }

    public void fillRounded(float x, float y, float width, float height, float radius, int argb) {
        ensureCapacity(1, 5, 1);
        opcodes[commandCount++] = OP_FILL_ROUNDED;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        floats[floatCursor++] = radius;
        ints[intCursor++] = argb;
    }

    /**
     * Fills a rounded rectangle with a vertical gradient.
     *
     * @param topColor colour at the top edge
     * @param bottomColor colour at the bottom edge
     */
    public void fillGradient(float x, float y, float width, float height, float radius, int topColor, int bottomColor) {
        ensureCapacity(1, 5, 2);
        opcodes[commandCount++] = OP_FILL_GRADIENT;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        floats[floatCursor++] = radius;
        ints[intCursor++] = topColor;
        ints[intCursor++] = bottomColor;
    }

    public void strokeRounded(float x, float y, float width, float height, float radius, float thickness, int argb) {
        ensureCapacity(1, 6, 1);
        opcodes[commandCount++] = OP_STROKE_ROUNDED;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        floats[floatCursor++] = radius;
        floats[floatCursor++] = thickness;
        ints[intCursor++] = argb;
    }

    /**
     * Draws a soft drop shadow. Backends that cannot render soft shadows may
     * approximate the effect with a translucent rounded rectangle.
     */
    public void shadow(float x, float y, float width, float height, float radius, float blur, int argb) {
        ensureCapacity(1, 6, 1);
        opcodes[commandCount++] = OP_SHADOW;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        floats[floatCursor++] = radius;
        floats[floatCursor++] = blur;
        ints[intCursor++] = argb;
    }

    public void text(float x, float y, int fontId, String value, int argb) {
        if (value == null || value.isEmpty()) {
            return;
        }
        ensureCapacity(1, 2, 3);
        opcodes[commandCount++] = OP_TEXT;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        ints[intCursor++] = fontId;
        ints[intCursor++] = argb;
        ints[intCursor++] = internString(value);
    }

    public void texture(int textureId, float srcX, float srcY, float srcWidth, float srcHeight,
                        float dstX, float dstY, float dstWidth, float dstHeight, int argb) {
        ensureCapacity(1, 8, 2);
        opcodes[commandCount++] = OP_TEXTURE;
        floats[floatCursor++] = srcX;
        floats[floatCursor++] = srcY;
        floats[floatCursor++] = srcWidth;
        floats[floatCursor++] = srcHeight;
        floats[floatCursor++] = dstX;
        floats[floatCursor++] = dstY;
        floats[floatCursor++] = dstWidth;
        floats[floatCursor++] = dstHeight;
        ints[intCursor++] = textureId;
        ints[intCursor++] = argb;
    }

    public void line(float x1, float y1, float x2, float y2, int argb) {
        ensureCapacity(1, 4, 1);
        opcodes[commandCount++] = OP_LINE;
        floats[floatCursor++] = x1;
        floats[floatCursor++] = y1;
        floats[floatCursor++] = x2;
        floats[floatCursor++] = y2;
        ints[intCursor++] = argb;
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
        ensureCapacity(1, 6, 1);
        opcodes[commandCount++] = OP_TRIANGLE;
        floats[floatCursor++] = x1;
        floats[floatCursor++] = y1;
        floats[floatCursor++] = x2;
        floats[floatCursor++] = y2;
        floats[floatCursor++] = x3;
        floats[floatCursor++] = y3;
        ints[intCursor++] = argb;
    }

    /**
     * Draws a filled arc segment (a slice of a donut), used by radial gauges
     * and progress rings.
     *
     * @param startAngle start angle in radians
     * @param endAngle   end angle in radians
     */
    public void arc(float x, float y, float radius, float startAngle, float endAngle, float innerRadius, int argb) {
        ensureCapacity(1, 7, 1);
        opcodes[commandCount++] = OP_ARC;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = radius;
        floats[floatCursor++] = startAngle;
        floats[floatCursor++] = endAngle;
        floats[floatCursor++] = innerRadius;
        floats[floatCursor++] = 0.0F;
        ints[intCursor++] = argb;
    }

    public void clipPush(float x, float y, float width, float height) {
        ensureCapacity(1, 4, 0);
        opcodes[commandCount++] = OP_CLIP_PUSH;
        floats[floatCursor++] = x;
        floats[floatCursor++] = y;
        floats[floatCursor++] = width;
        floats[floatCursor++] = height;
        if (clipDepth == 0) {
            clipX = x;
            clipY = y;
            clipWidth = width;
            clipHeight = height;
        } else {
            float nx = Math.max(clipX, x);
            float ny = Math.max(clipY, y);
            float nr = Math.min(clipX + clipWidth, x + width);
            float nb = Math.min(clipY + clipHeight, y + height);
            if (nr <= nx || nb <= ny) {
                clipX = 0.0F;
                clipY = 0.0F;
                clipWidth = 0.0F;
                clipHeight = 0.0F;
            } else {
                clipX = nx;
                clipY = ny;
                clipWidth = nr - nx;
                clipHeight = nb - ny;
            }
        }
        clipDepth++;
    }

    public void clipPop() {
        if (clipDepth <= 0) {
            return;
        }
        ensureCapacity(1, 0, 0);
        opcodes[commandCount++] = OP_CLIP_POP;
        clipDepth--;
        if (clipDepth == 0) {
            clipX = 0.0F;
            clipY = 0.0F;
            clipWidth = 0.0F;
            clipHeight = 0.0F;
        }
    }

    // ----------------------------------------------------------------------
    // Cursor based iteration, used by the render backends
    // ----------------------------------------------------------------------

    public int opcodes(int index) {
        return opcodes[index];
    }

    public float floatAt(int index) {
        return floats[index];
    }

    public int intAt(int index) {
        return ints[index];
    }

    public String stringAt(int index) {
        return strings[index];
    }

    public static int floatArgCount(int opcode) {
        return FLOAT_ARGS[opcode];
    }

    public static int intArgCount(int opcode) {
        return INT_ARGS[opcode];
    }

    // ----------------------------------------------------------------------
    // Internals
    // ----------------------------------------------------------------------

    private int internString(String value) {
        // Linear scan over a small pool is faster than a hash map for the
        // handful of distinct strings a single frame contains.
        for (int i = 0; i < stringCursor; i++) {
            if (strings[i].equals(value)) {
                return i;
            }
        }
        if (stringCursor == strings.length) {
            strings = Arrays.copyOf(strings, strings.length * 2);
        }
        strings[stringCursor] = value;
        return stringCursor++;
    }

    private void ensureCapacity(int commands, int floatsNeeded, int intsNeeded) {
        if (commandCount + commands > opcodes.length) {
            opcodes = Arrays.copyOf(opcodes, Math.max(opcodes.length * 2, commandCount + commands));
        }
        if (floatCursor + floatsNeeded > floats.length) {
            floats = Arrays.copyOf(floats, Math.max(floats.length * 2, floatCursor + floatsNeeded));
        }
        if (intCursor + intsNeeded > ints.length) {
            ints = Arrays.copyOf(ints, Math.max(ints.length * 2, intCursor + intsNeeded));
        }
    }
}
