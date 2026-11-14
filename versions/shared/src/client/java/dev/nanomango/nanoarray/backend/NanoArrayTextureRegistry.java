package dev.nanomango.nanoarray.backend;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of the textures that the framework can draw.
 *
 * <p>Components refer to a texture by its numeric id, which keeps them free
 * of any Minecraft type. The host registers the textures it wants to use and
 * the render backend resolves the ids again.</p>
 */
public final class NanoArrayTextureRegistry {

    private final Map<Integer, NanoArrayTexture> textures = new HashMap<>();
    private int nextId = 1;

    /**
     * Registers a texture and returns its id.
     */
    public int register(NanoArrayTexture texture) {
        int id = nextId++;
        textures.put(id, texture);
        return id;
    }

    /**
     * Registers a Minecraft texture.
     *
     * @param identifier the texture identifier
     * @param width      width of the texture in pixels
     * @param height     height of the texture in pixels
     * @return the id of the registered texture
     */
    public int register(Identifier identifier, int width, int height) {
        return register(new MinecraftTexture(identifier, width, height));
    }

    /**
     * Looks a texture up by its id.
     */
    public NanoArrayTexture byId(int id) {
        return textures.get(id);
    }

    /**
     * Aspect ratio of a registered texture, or {@code 0} when it is unknown.
     */
    public float aspect(int id) {
        NanoArrayTexture texture = textures.get(id);
        return texture == null ? 0.0F : texture.aspect();
    }

    /**
     * Number of registered textures.
     */
    public int size() {
        return textures.size();
    }

    /**
     * A texture that is backed by a Minecraft resource.
     */
    public static final class MinecraftTexture implements NanoArrayTexture {

        private final Identifier identifier;
        private final int width;
        private final int height;

        public MinecraftTexture(Identifier identifier, int width, int height) {
            this.identifier = identifier;
            this.width = width;
            this.height = height;
        }

        @Override
        public Identifier identifier() {
            return identifier;
        }

        @Override
        public int width() {
            return width;
        }

        @Override
        public int height() {
            return height;
        }
    }
}
