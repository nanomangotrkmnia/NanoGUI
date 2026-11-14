package dev.nanomango.nanogui.backend;

import net.minecraft.resources.Identifier;

/**
 * A texture that the framework can draw.
 *
 * <p>The framework identifies textures with a small integer id so that the
 * component code stays independent of Minecraft. The host registers the
 * textures it wants to use and resolves the ids again when the draw list is
 * replayed.</p>
 */
public interface NanoGUITexture {

    /**
     * Identifier of the Minecraft texture.
     */
    Identifier identifier();

    /**
     * Width of the texture in pixels.
     */
    int width();

    /**
     * Height of the texture in pixels.
     */
    int height();

    /**
     * Aspect ratio of the texture, {@code width / height}.
     */
    default float aspect() {
        int h = height();
        return h <= 0 ? 0.0F : (float) width() / h;
    }
}
