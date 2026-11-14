package dev.nanomango.nanogui.backend;

import com.mojang.blaze3d.platform.NativeImage;
import dev.nanomango.gui.font.FontTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Uploads the glyph atlases of the rasterising fonts into Minecraft textures.
 *
 * <p>Every atlas is backed by a {@link DynamicTexture} that is registered with
 * the texture manager, so it can be drawn with the regular Minecraft blit
 * calls. The pixels are only copied to the GPU when the atlas changed, which
 * keeps the per frame cost of the text rendering at zero.</p>
 */
public final class FontAtlasRegistry {

    /** Namespace of the generated texture identifiers. */
    public static final String NAMESPACE = "nanogui";

    private final Map<Integer, Atlas> atlases = new HashMap<>();

    /**
     * Returns the texture of a font, uploading the atlas when it changed.
     *
     * @param fontId  id of the font
     * @param texture the glyph atlas of the font
     * @return the atlas or {@code null} when the font has no atlas
     */
    public Atlas atlas(int fontId, FontTexture texture) {
        if (texture == null) {
            return null;
        }
        int width = texture.width();
        int height = texture.height();
        if (width <= 0 || height <= 0) {
            return null;
        }

        Atlas atlas = atlases.get(fontId);
        if (atlas == null || !atlas.matches(width, height)) {
            atlas = new Atlas(fontId, width, height);
            atlases.put(fontId, atlas);
        }
        atlas.update(texture);
        return atlas;
    }

    /**
     * Releases every texture that was created by this registry.
     */
    public void close() {
        for (Atlas atlas : atlases.values()) {
            atlas.close();
        }
        atlases.clear();
    }

    /**
     * A single glyph atlas that is backed by a Minecraft texture.
     */
    public static final class Atlas {

        private final Identifier identifier;
        private final DynamicTexture texture;
        private final NativeImage image;
        private final int[] pixels;
        private int revision = -1;

        Atlas(int fontId, int width, int height) {
            this.identifier = Identifier.fromNamespaceAndPath(NAMESPACE, "font/atlas_" + fontId);
            this.texture = new DynamicTexture(() -> "NanoGUI Font Atlas", width, height, false);
            NativeImage created = texture.getPixels();
            if (created == null) {
                throw new IllegalStateException("The font atlas texture has no pixel buffer");
            }
            this.image = created;
            this.pixels = new int[width * height];

            Minecraft.getInstance().getTextureManager().register(identifier, texture);
        }

        boolean matches(int width, int height) {
            return image.getWidth() == width && image.getHeight() == height;
        }

        /**
         * Copies the atlas into the GPU texture when it changed.
         */
        void update(FontTexture source) {
            int sourceRevision = source.revision();
            if (sourceRevision == revision) {
                return;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            source.writePixels(pixels);

            for (int y = 0; y < height; y++) {
                int row = y * width;
                for (int x = 0; x < width; x++) {
                    image.setPixel(x, y, pixels[row + x]);
                }
            }

            texture.upload();
            revision = sourceRevision;
        }

        /**
         * Identifier of the Minecraft texture that holds the atlas.
         */
        public Identifier identifier() {
            return identifier;
        }

        /**
         * Width of the atlas in pixels.
         */
        public int width() {
            return image.getWidth();
        }

        /**
         * Height of the atlas in pixels.
         */
        public int height() {
            return image.getHeight();
        }

        void close() {
            texture.close();
        }
    }
}
