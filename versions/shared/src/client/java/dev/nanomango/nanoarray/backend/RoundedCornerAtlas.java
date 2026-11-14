package dev.nanomango.nanoarray.backend;

import dev.nanomango.gui.render.CornerSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Uploads the anti-aliased corner sprites of the rounded rectangles into
 * Minecraft textures.
 *
 * <p>Every texture holds the four corners of one radius, so a rounded
 * rectangle is drawn with two rectangles for the straight edges and four
 * blits for the corners. The corners are generated from the exact geometry
 * of a disc, which makes them smooth at every GUI scale.</p>
 *
 * <p>The textures are created lazily on the render thread and are reused for
 * the rest of the session.</p>
 */
public final class RoundedCornerAtlas {

    /** Namespace of the generated texture identifiers. */
    public static final String NAMESPACE = "nanoarray";

    private final Map<String, Sprite> sprites = new HashMap<>();

    /**
     * Returns the corners of a filled rounded rectangle.
     *
     * @param radius the corner radius in pixels
     * @return the sprite, never {@code null}
     */
    public Sprite fill(int radius) {
        int r = Math.max(1, radius);
        return sprite("fill/" + r, r, 0, false);
    }

    /**
     * Returns the corners of the outline of a rounded rectangle.
     *
     * @param radius    the corner radius in pixels
     * @param thickness the width of the outline in pixels
     * @return the sprite, never {@code null}
     */
    public Sprite stroke(int radius, int thickness) {
        int r = Math.max(1, radius);
        int t = Math.max(1, Math.min(thickness, r));
        return sprite("stroke/" + r + "/" + t, r, t, true);
    }

    private Sprite sprite(String key, int radius, int thickness, boolean outline) {
        Sprite cached = sprites.get(key);
        if (cached != null) {
            return cached;
        }

        int width = CornerSprites.textureWidth(radius);
        int height = CornerSprites.textureHeight(radius);
        int[] pixels = outline
                ? CornerSprites.generateStroke(radius, thickness)
                : CornerSprites.generateFill(radius);

        Identifier identifier = Identifier.fromNamespaceAndPath(NAMESPACE, "rounded/" + key);
        DynamicTexture texture = new DynamicTexture(() -> "NanoArray Rounded Corners", width, height, false);
        com.mojang.blaze3d.platform.NativeImage image = texture.getPixels();
        if (image == null) {
            throw new IllegalStateException("The rounded corner texture has no pixel buffer");
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixel(x, y, pixels[y * width + x]);
            }
        }
        texture.upload();

        Minecraft.getInstance().getTextureManager().register(identifier, texture);

        Sprite sprite = new Sprite(identifier, texture, width, height, radius);
        sprites.put(key, sprite);
        return sprite;
    }

    /**
     * Releases every texture that was created by this atlas.
     */
    public void close() {
        for (Sprite sprite : sprites.values()) {
            sprite.close();
        }
        sprites.clear();
    }

    /**
     * The four corners of one radius.
     */
    public static final class Sprite {

        private final Identifier identifier;
        private final int textureWidth;
        private final int textureHeight;
        private final int cellSize;
        private DynamicTexture texture;

        Sprite(Identifier identifier, DynamicTexture texture, int textureWidth, int textureHeight, int cellSize) {
            this.identifier = identifier;
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.cellSize = cellSize;
        }

        /**
         * Identifier of the Minecraft texture.
         */
        public Identifier identifier() {
            return identifier;
        }

        /**
         * Width of the texture in pixels.
         */
        public int textureWidth() {
            return textureWidth;
        }

        /**
         * Height of the texture in pixels.
         */
        public int textureHeight() {
            return textureHeight;
        }

        /**
         * Size of a single corner in pixels.
         */
        public int cellSize() {
            return cellSize;
        }

        /**
         * Horizontal offset of a corner inside the texture, in pixels.
         *
         * @param orientation one of the constants of {@link CornerSprites}
         */
        public float sourceU(int orientation) {
            return orientation * cellSize;
        }

        void close() {
            if (texture != null) {
                texture.close();
                texture = null;
            }
        }
    }
}
