package dev.nanomango.gui.font;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Font implementation backed by the bundled native renderer.
 *
 * <p>The renderer is written in Rust and uses a HarfBuzz compatible shaper
 * ({@code rustybuzz}) together with a rasteriser that produces sub pixel
 * accurate anti aliased glyphs. It supports the complete Unicode range of
 * the bundled font, kerning and ligatures.</p>
 *
 * <p>The class follows the same contract as every other {@link Font}: it
 * measures text, exposes the glyph atlas as a {@link FontTexture} and pushes
 * the shaped glyphs into a {@link TextSink}.</p>
 *
 * <p>Instances are not thread safe. Every instance owns native memory and
 * releases it in {@link #close()}.</p>
 */
public final class NativeFont implements Font, AutoCloseable {

    /** Number of floats that the native shape buffer uses per glyph. */
    private static final int GLYPH_STRIDE = 8;

    /** Initial size of the reusable glyph buffer. */
    private static final int INITIAL_GLYPH_CAPACITY = 128;

    private final int id;
    private final String name;

    /** Native font handle, {@code 0} when the font could not be created. */
    private long handle;

    /** Reusable buffer for the UTF-8 encoding of the measured text. */
    private byte[] utf8Buffer = new byte[64];

    /** Number of valid bytes in {@link #utf8Buffer}. */
    private int utf8Length;

    /** Reusable buffer for the shaped glyph quads. */
    private float[] glyphBuffer = new float[GLYPH_STRIDE * INITIAL_GLYPH_CAPACITY];

    /** Reusable buffer for the atlas pixels. */
    private int[] pixelBuffer = new int[512 * 512];

    /** Cached vertical metrics. */
    private float ascent;
    private float descent;
    private float lineGap;

    /** Cache of measured widths, the GUI measures the same strings every frame. */
    private final MeasurementCache measurements = new MeasurementCache();

    /** Lazily created view on the native glyph atlas. */
    private FontTexture texture;

    /**
     * Creates a font from the raw contents of a TrueType or OpenType file.
     *
     * @param id        identifier of the font
     * @param name      display name
     * @param fontData  raw contents of the font file
     * @param pixelSize the size of the font in pixels
     * @throws IllegalStateException when the native library is not available
     * @throws IllegalArgumentException when the font data cannot be parsed
     */
    public NativeFont(int id, String name, byte[] fontData, float pixelSize) {
        if (!NativeLibrary.load()) {
            Throwable failure = NativeLibrary.failure();
            throw new IllegalStateException("The native font renderer is not available", failure);
        }
        if (fontData == null || fontData.length == 0) {
            throw new IllegalArgumentException("The font data must not be empty");
        }

        this.id = id;
        this.name = name == null ? "Native" : name;

        long created = nCreate(fontData, pixelSize);
        if (created == 0) {
            throw new IllegalArgumentException("The font data could not be parsed: " + this.name);
        }
        this.handle = created;
        readMetrics();
    }

    /**
     * Creates a font from a TrueType or OpenType file on disk.
     */
    public static NativeFont fromFile(int id, String name, java.nio.file.Path path, float pixelSize)
            throws java.io.IOException {
        return new NativeFont(id, name, java.nio.file.Files.readAllBytes(path), pixelSize);
    }

    /**
     * Creates a font from a resource on the classpath.
     */
    public static NativeFont fromResource(int id, String name, String resource, float pixelSize)
            throws java.io.IOException {
        try (java.io.InputStream stream =
                     NativeFont.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new java.io.IOException("Font resource not found: " + resource);
            }
            return new NativeFont(id, name, stream.readAllBytes(), pixelSize);
        }
    }

    /**
     * True when the native renderer can be used on this machine.
     */
    public static boolean isAvailable() {
        return NativeLibrary.load();
    }

    /**
     * Version of the loaded native library.
     */
    public static int nativeVersion() {
        return NativeLibrary.load() ? nVersion() : 0;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public float height() {
        return ascent + descent + lineGap;
    }

    @Override
    public float ascent() {
        return ascent;
    }

    @Override
    public float descent() {
        return descent;
    }

    @Override
    public boolean supportsUnicode() {
        return true;
    }

    @Override
    public float width(String text) {
        if (text == null || text.isEmpty() || handle == 0) {
            return 0.0F;
        }
        Float cached = measurements.get(text);
        if (cached != null) {
            return cached;
        }
        int length = encode(text);
        float width = nMeasure(handle, utf8Buffer, length);
        measurements.put(text, width);
        return width;
    }

    @Override
    public float width(CharSequence text, int start, int end) {
        if (text == null || handle == 0) {
            return 0.0F;
        }
        int from = Math.max(0, start);
        int to = Math.min(text.length(), end);
        if (from >= to) {
            return 0.0F;
        }
        return width(text.subSequence(from, to).toString());
    }

    /**
     * Changes the pixel size of the font.
     *
     * <p>The glyph atlas is cleared and rebuilt lazily while the next glyphs
     * are rasterised.</p>
     */
    public void setPixelSize(float pixelSize) {
        if (handle == 0) {
            return;
        }
        nSetSize(handle, pixelSize);
        readMetrics();
        measurements.clear();
    }

    @Override
    public void draw(TextSink sink, String text, float x, float y, int argb) {
        if (text == null || text.isEmpty() || handle == 0 || sink == null) {
            return;
        }

        byte[] utf8 = utf8Buffer;
        int length = encode(text);
        int capacity = glyphBuffer.length / GLYPH_STRIDE;
        int count = nShape(handle, utf8, length, glyphBuffer, capacity);
        while (count == capacity && capacity < 4096) {
            capacity = Math.min(4096, capacity * 2);
            glyphBuffer = new float[GLYPH_STRIDE * capacity];
            count = nShape(handle, utf8, length, glyphBuffer, capacity);
        }

        // The native renderer reports the glyph offsets relative to the
        // baseline, the framework works with the top of the line.
        float baseline = y + ascent;

        for (int i = 0; i < count; i++) {
            int base = i * GLYPH_STRIDE;
            float glyphX = glyphBuffer[base];
            float glyphY = glyphBuffer[base + 1];
            float u0 = glyphBuffer[base + 2];
            float v0 = glyphBuffer[base + 3];
            float u1 = glyphBuffer[base + 4];
            float v1 = glyphBuffer[base + 5];
            float width = glyphBuffer[base + 6];
            float height = glyphBuffer[base + 7];
            if (width <= 0.0F || height <= 0.0F) {
                continue;
            }
            sink.glyph(id, id, u0, v0, u1, v1, x + glyphX, baseline + glyphY, width, height, argb);
        }
    }

    @Override
    public FontTexture texture() {
        if (handle == 0) {
            return null;
        }
        if (texture == null) {
            texture = new NativeTexture();
        }
        return texture;
    }

    /**
     * Releases the native memory of this font.
     */
    @Override
    public void close() {
        if (handle != 0) {
            nDestroy(handle);
            handle = 0;
        }
    }

    private void readMetrics() {
        float[] metrics = new float[3];
        nMetrics(handle, metrics);
        this.ascent = metrics[0];
        this.descent = metrics[1];
        this.lineGap = metrics[2];
        if (this.ascent <= 0.0F) {
            this.ascent = 8.0F;
        }
        if (this.descent <= 0.0F) {
            this.descent = 2.0F;
        }
    }

    /**
     * Encodes a string as UTF-8 into the reusable buffer.
     *
     * <p>The buffer grows when needed and is reused for every call, which
     * keeps the measurement path allocation free. The number of valid bytes
     * is stored in {@link #utf8Length} and handed to the native renderer
     * explicitly, because the buffer can be larger than the encoded text.</p>
     *
     * @return the number of bytes that were written
     */
    private int encode(String text) {
        int length = text.length();
        byte[] buffer = utf8Buffer;
        if (buffer.length < length * 3) {
            buffer = new byte[Math.max(length * 3, 64)];
            utf8Buffer = buffer;
        }

        int index = 0;
        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            if (character < 0x80) {
                buffer[index++] = (byte) character;
            } else if (character < 0x800) {
                buffer[index++] = (byte) (0xC0 | (character >> 6));
                buffer[index++] = (byte) (0x80 | (character & 0x3F));
            } else if (Character.isHighSurrogate(character)
                    && i + 1 < length
                    && Character.isLowSurrogate(text.charAt(i + 1))) {
                int codePoint = Character.toCodePoint(character, text.charAt(i + 1));
                i++;
                buffer[index++] = (byte) (0xF0 | (codePoint >> 18));
                buffer[index++] = (byte) (0x80 | ((codePoint >> 12) & 0x3F));
                buffer[index++] = (byte) (0x80 | ((codePoint >> 6) & 0x3F));
                buffer[index++] = (byte) (0x80 | (codePoint & 0x3F));
            } else {
                buffer[index++] = (byte) (0xE0 | (character >> 12));
                buffer[index++] = (byte) (0x80 | ((character >> 6) & 0x3F));
                buffer[index++] = (byte) (0x80 | (character & 0x3F));
            }
        }
        utf8Length = index;
        return index;
    }

    /**
     * View on the native glyph atlas.
     */
    private final class NativeTexture implements FontTexture {

        @Override
        public int width() {
            return nAtlasWidth(handle);
        }

        @Override
        public int height() {
            return nAtlasHeight(handle);
        }

        @Override
        public int revision() {
            return nAtlasRevision(handle);
        }

        @Override
        public void writePixels(int[] argbOut) {
            if (argbOut == null) {
                return;
            }
            int width = width();
            int height = height();
            int needed = width * height;
            if (needed <= 0) {
                Arrays.fill(argbOut, 0);
                return;
            }
            if (argbOut.length < needed) {
                throw new IllegalArgumentException(
                        "The destination buffer is too small: " + argbOut.length + " < " + needed);
            }
            if (pixelBuffer.length < needed) {
                pixelBuffer = new int[needed];
            }
            nAtlasPixels(handle, pixelBuffer);
            System.arraycopy(pixelBuffer, 0, argbOut, 0, needed);
        }
    }

    // ------------------------------------------------------------------
    // Native methods
    // ------------------------------------------------------------------

    private static native long nCreate(byte[] data, float pixelSize);

    private static native void nDestroy(long handle);

    private static native void nSetSize(long handle, float pixelSize);

    private static native float nMeasure(long handle, byte[] utf8, int length);

    private static native int nShape(long handle, byte[] utf8, int length, float[] out, int maxGlyphs);

    private static native int nAtlasWidth(long handle);

    private static native int nAtlasHeight(long handle);

    private static native int nAtlasRevision(long handle);

    private static native void nAtlasPixels(long handle, int[] out);

    private static native void nMetrics(long handle, float[] out);

    private static native int nVersion();

    /**
     * Returns the UTF-8 encoding of a string. Only used by the tests.
     */
    static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
