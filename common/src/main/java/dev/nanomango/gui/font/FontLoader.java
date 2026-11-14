package dev.nanomango.gui.font;

import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates fonts from bundled resources.
 *
 * <p>The loader prefers the native renderer because it is faster and supports
 * the complete Unicode range of the font. When the native library cannot be
 * loaded, for example because the mod runs on an unsupported platform, the
 * Java2D implementation is used instead. The public API is identical in both
 * cases, therefore the rest of the framework never has to care about the
 * difference.</p>
 */
public final class FontLoader {

    /** Resource of the regular weight of the bundled font. */
    public static final String INTER_REGULAR = "/fonts/Inter-Regular.ttf";

    /** Resource of the medium weight of the bundled font. */
    public static final String INTER_MEDIUM = "/fonts/Inter-Medium.ttf";

    /** Resource of the semibold weight of the bundled font. */
    public static final String INTER_SEMIBOLD = "/fonts/Inter-SemiBold.ttf";

    /** Resource of the bold weight of the bundled font. */
    public static final String INTER_BOLD = "/fonts/Inter-Bold.ttf";

    private FontLoader() {
    }

    /**
     * Creates a font from a resource.
     *
     * @param id        identifier of the font
     * @param name      display name
     * @param resource  classpath location of the font file
     * @param pixelSize the size of the font in pixels
     * @param awtStyle  the Java2D style, used by the fallback implementation
     * @return the created font, never {@code null}
     * @throws IOException when the resource cannot be read
     */
    public static Font create(int id, String name, String resource, float pixelSize, int awtStyle)
            throws IOException {
        return create(id, name, resource, pixelSize, awtStyle, 1.0F);
    }

    /**
     * Creates a font from a resource and rasterises its atlas for the given
     * display density.
     *
     * @param rasterScale physical pixels per GUI unit, usually the Minecraft
     *                    GUI scale
     */
    public static Font create(int id, String name, String resource, float pixelSize, int awtStyle,
                              float rasterScale) throws IOException {
        byte[] data = read(resource);
        if (NativeLibrary.load()) {
            try {
                return new NativeFont(id, name, data, pixelSize, rasterScale);
            } catch (IllegalArgumentException exception) {
                // The native parser rejected the file, fall back to Java2D.
            }
        }
        try {
            return new AwtFont(id, name, data, pixelSize, awtStyle, rasterScale);
        } catch (FontFormatException exception) {
            throw new IOException("The font resource " + resource + " is not a valid font", exception);
        }
    }

    /**
     * Creates a font from raw bytes.
     *
     * @param id        identifier of the font
     * @param name      display name
     * @param data      raw contents of the font file
     * @param pixelSize the size of the font in pixels
     * @param awtStyle  the Java2D style, used by the fallback implementation
     */
    public static Font create(int id, String name, byte[] data, float pixelSize, int awtStyle)
            throws IOException {
        return create(id, name, data, pixelSize, awtStyle, 1.0F);
    }

    /**
     * Creates a font from raw bytes and rasterises its atlas for the given
     * display density.
     *
     * @param rasterScale physical pixels per GUI unit, usually the Minecraft
     *                    GUI scale
     */
    public static Font create(int id, String name, byte[] data, float pixelSize, int awtStyle,
                              float rasterScale) throws IOException {
        if (NativeLibrary.load()) {
            try {
                return new NativeFont(id, name, data, pixelSize, rasterScale);
            } catch (IllegalArgumentException exception) {
                // The native parser rejected the file, fall back to Java2D.
            }
        }
        try {
            return new AwtFont(id, name, data, pixelSize, awtStyle, rasterScale);
        } catch (FontFormatException exception) {
            throw new IOException("The font data is not a valid font", exception);
        }
    }

    /**
     * Creates the three weights of the bundled font family.
     *
     * @param regularId identifier of the regular font
     * @param mediumId  identifier of the medium font
     * @param boldId    identifier of the bold font
     * @param size      the size of the fonts in pixels
     * @return an array with the regular, the medium and the bold font
     */
    public static Font[] createInterFamily(int regularId, int mediumId, int boldId, float size)
            throws IOException {
        return createInterFamily(regularId, mediumId, boldId, size, 1.0F);
    }

    /**
     * Creates the three weights of the bundled font family for a display
     * density.
     *
     * @param rasterScale physical pixels per GUI unit, usually the Minecraft
     *                    GUI scale
     */
    public static Font[] createInterFamily(int regularId, int mediumId, int boldId, float size,
                                           float rasterScale) throws IOException {
        Font regular = create(regularId, "Inter", INTER_REGULAR, size, java.awt.Font.PLAIN, rasterScale);
        Font medium = create(mediumId, "Inter Medium", INTER_MEDIUM, size, java.awt.Font.BOLD, rasterScale);
        Font bold = create(boldId, "Inter Bold", INTER_BOLD, size, java.awt.Font.BOLD, rasterScale);
        return new Font[]{regular, medium, bold};
    }

    /**
     * Reads a classpath resource into a byte array.
     *
     * <p>The resource name may start with a slash, which is the convention
     * for absolute classpath locations.</p>
     */
    public static byte[] read(String resource) throws IOException {
        String path = resource.startsWith("/") ? resource.substring(1) : resource;
        try (InputStream stream = FontLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Font resource not found: " + resource);
            }
            return stream.readAllBytes();
        }
    }

    /**
     * True when the native renderer is used instead of the Java2D fallback.
     */
    public static boolean isNative() {
        return NativeLibrary.load();
    }
}
