package dev.nanomango.gui.font;

import dev.nanomango.gui.util.MathUtil;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure Java font implementation that is used when the native renderer is not
 * available.
 *
 * <p>It rasterises glyphs into a texture atlas on demand and caches both the
 * bitmap and the advance width of every glyph. The implementation relies on
 * {@code java.desktop}, which is part of every Minecraft client runtime, and
 * degrades gracefully when the module is unavailable.</p>
 */
public final class AwtFont implements Font {

    private static final int ATLAS_SIZE = 512;
    private static final int PADDING = 2;

    private final int id;
    private final String name;

    /** Physical pixels per GUI unit, see {@link #AwtFont(int, String, byte[], float, int, float)}. */
    private final float rasterScale;

    private final java.awt.Font awtFont;
    private final FontRenderContext renderContext;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    /** Cache of measured widths, the GUI measures the same strings every frame. */
    private final MeasurementCache measurements = new MeasurementCache();

    private int atlasWidth = ATLAS_SIZE;
    private int atlasHeight = ATLAS_SIZE;
    private int penX = PADDING;
    private int penY = PADDING;
    private int rowHeight = 0;
    private boolean full;

    private BufferedImage atlas;
    private int[] pixels;
    private int revision;

    /**
     * Creates a font from a TrueType or OpenType file.
     *
     * @param id          identifier used by the render backends
     * @param name        display name
     * @param fontData    raw font file contents
     * @param size        pixel size
     * @param style       {@link java.awt.Font#PLAIN}, {@code BOLD} or {@code ITALIC}
     */
    public AwtFont(int id, String name, byte[] fontData, float size, int style)
            throws FontFormatException, IOException {
        this(id, name, fontData, size, style, 1.0F);
    }

    /**
     * Creates a font whose atlas is rasterised for a high density display.
     *
     * <p>Metrics and advances stay in GUI units; only the glyph bitmaps are
     * rendered at {@code size * rasterScale} pixels.</p>
     *
     * @param rasterScale physical pixels per GUI unit, usually the Minecraft
     *                    GUI scale
     */
    public AwtFont(int id, String name, byte[] fontData, float size, int style, float rasterScale)
            throws FontFormatException, IOException {
        this.id = id;
        this.name = name;
        this.rasterScale = rasterScale > 0.0F ? rasterScale : 1.0F;
        this.awtFont = java.awt.Font
                .createFont(java.awt.Font.TRUETYPE_FONT, new java.io.ByteArrayInputStream(fontData))
                .deriveFont(style, size * this.rasterScale);
        this.renderContext = new FontRenderContext(null, true, true);
        this.atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        this.pixels = new int[atlasWidth * atlasHeight];
    }

    /**
     * Creates a font from a TrueType or OpenType file on disk.
     */
    public static AwtFont fromFile(int id, String name, Path path, float size, int style)
            throws IOException, FontFormatException {
        return new AwtFont(id, name, Files.readAllBytes(path), size, style);
    }

    /**
     * Creates a font from a resource on the classpath.
     */
    public static AwtFont fromResource(int id, String name, String resource, float size, int style)
            throws IOException, FontFormatException {
        try (InputStream stream = AwtFont.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Font resource not found: " + resource);
            }
            return new AwtFont(id, name, stream.readAllBytes(), size, style);
        }
    }

    /**
     * True when the Java2D font support is available.
     */
    public static boolean isAvailable() {
        try {
            return GraphicsEnvironment.isHeadless() || java.awt.GraphicsEnvironment.class != null;
        } catch (Throwable throwable) {
            return false;
        }
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
        return awtFont.getLineMetrics("Ag", renderContext).getHeight() / rasterScale;
    }

    @Override
    public float ascent() {
        return awtFont.getLineMetrics("Ag", renderContext).getAscent() / rasterScale;
    }

    @Override
    public float descent() {
        return awtFont.getLineMetrics("Ag", renderContext).getDescent() / rasterScale;
    }

    @Override
    public float width(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }
        Float cached = measurements.get(text);
        if (cached != null) {
            return cached;
        }
        float total = 0.0F;
        for (int i = 0; i < text.length(); i++) {
            total += advance(text.charAt(i));
        }
        measurements.put(text, total);
        return total;
    }

    @Override
    public float width(CharSequence text, int start, int end) {
        if (text == null) {
            return 0.0F;
        }
        int from = Math.max(0, start);
        int to = Math.min(text.length(), end);
        if (from >= to) {
            return 0.0F;
        }
        float total = 0.0F;
        for (int i = from; i < to; i++) {
            total += advance(text.charAt(i));
        }
        return total;
    }

    @Override
    public boolean supportsUnicode() {
        return awtFont.canDisplayUpTo("你好世界Ω") == -1;
    }

    private float advance(char character) {
        Glyph glyph = glyph(character);
        return glyph == null ? 0.0F : glyph.advance;
    }

    private Glyph glyph(char character) {
        Glyph cached = glyphs.get((int) character);
        if (cached != null) {
            return cached;
        }
        Glyph glyph = rasterise(character);
        glyphs.put((int) character, glyph);
        return glyph;
    }

    private Glyph rasterise(char character) {
        String value = String.valueOf(character);
        GlyphVector vector = awtFont.createGlyphVector(renderContext, value);
        // The glyph position is the offset of the glyph inside the vector,
        // the advance is reported by the glyph metrics.
        float advance = (float) vector.getGlyphMetrics(0).getAdvanceX() / rasterScale;

        Rectangle2D bounds = vector.getOutline().getBounds2D();
        if (bounds.isEmpty()) {
            // Whitespace and other glyphs without an outline still advance
            // the pen.
            return new Glyph(advance, 0, 0, 0, 0, 0, 0);
        }

        int glyphWidth = Math.max(1, (int) Math.ceil(bounds.getWidth()));
        int glyphHeight = Math.max(1, (int) Math.ceil(bounds.getHeight()));
        // The outline of a glyph vector is relative to the baseline, so the
        // offsets are negative for glyphs that sit on the baseline.
        int offsetX = (int) Math.floor(bounds.getX());
        int offsetY = (int) Math.floor(bounds.getY());

        if (!reserve(glyphWidth, glyphHeight)) {
            return new Glyph(advance, 0, 0, 0, 0, 0, 0);
        }

        BufferedImage image = new BufferedImage(glyphWidth, glyphHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setColor(java.awt.Color.WHITE);
        graphics.translate(-offsetX, -offsetY);
        graphics.fill(vector.getOutline());
        graphics.dispose();

        int[] argb = image.getRGB(0, 0, glyphWidth, glyphHeight, null, 0, glyphWidth);
        copyIntoAtlas(argb, glyphWidth, glyphHeight, penX, penY);

        Glyph glyph = new Glyph(advance, penX, penY, glyphWidth, glyphHeight, offsetX, offsetY);
        advancePen(glyphWidth, glyphHeight);
        return glyph;
    }

    private void copyIntoAtlas(int[] argb, int width, int height, int targetX, int targetY) {
        for (int y = 0; y < height; y++) {
            int sourceRow = y * width;
            int targetRow = (targetY + y) * atlasWidth + targetX;
            System.arraycopy(argb, sourceRow, pixels, targetRow, width);
        }
        revision++;
    }

    private void advancePen(int glyphWidth, int glyphHeight) {
        penX += glyphWidth + PADDING;
        rowHeight = Math.max(rowHeight, glyphHeight);
        if (penX + PADDING >= atlasWidth) {
            penX = PADDING;
            penY += rowHeight + PADDING;
            rowHeight = 0;
        }
    }

    private boolean reserve(int width, int height) {
        if (full) {
            return false;
        }
        if (penX + width + PADDING > atlasWidth) {
            penX = PADDING;
            penY += rowHeight + PADDING;
            rowHeight = 0;
        }
        if (penY + height + PADDING > atlasHeight) {
            full = true;
            return false;
        }
        return true;
    }

    @Override
    public FontTexture texture() {
        return new FontTexture() {
            @Override
            public int width() {
                return atlasWidth;
            }

            @Override
            public int height() {
                return atlasHeight;
            }

            @Override
            public int revision() {
                return revision;
            }

            @Override
            public void writePixels(int[] argbOut) {
                System.arraycopy(pixels, 0, argbOut, 0, Math.min(pixels.length, argbOut.length));
            }
        };
    }

    @Override
    public void draw(TextSink sink, String text, float x, float y, int argb) {
        if (text == null || text.isEmpty()) {
            return;
        }
        // The framework works with the top of the line, Java2D with the
        // baseline.
        // Glyphs are placed on whole physical pixels so the atlas is drawn
        // without resampling. The pen itself keeps its exact position.
        float baseline = Math.round((y + ascent()) * rasterScale);
        float cursor = x;
        for (int i = 0; i < text.length(); i++) {
            Glyph glyph = glyph(text.charAt(i));
            if (glyph.width > 0 && glyph.height > 0) {
                sink.glyph(id, -1,
                        (float) glyph.x / atlasWidth,
                        (float) glyph.y / atlasHeight,
                        (float) (glyph.x + glyph.width) / atlasWidth,
                        (float) (glyph.y + glyph.height) / atlasHeight,
                        (Math.round(cursor * rasterScale) + glyph.offsetX) / rasterScale,
                        (baseline + glyph.offsetY) / rasterScale,
                        glyph.width / rasterScale,
                        glyph.height / rasterScale,
                        argb);
            }
            cursor += glyph.advance;
        }
    }

    /**
     * Clamps a fractional advance so that text does not shimmer.
     */
    public static float snapAdvance(float value) {
        return MathUtil.roundToPixel(value, 1.0F);
    }

    /**
     * A rasterised glyph inside the atlas.
     */
    private static final class Glyph {
        final float advance;
        final int x;
        final int y;
        final int width;
        final int height;
        final int offsetX;
        final int offsetY;

        Glyph(float advance, int x, int y, int width, int height, int offsetX, int offsetY) {
            this.advance = advance;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
}
