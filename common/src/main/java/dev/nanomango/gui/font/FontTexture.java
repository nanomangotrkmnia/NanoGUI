package dev.nanomango.gui.font;

/**
 * GPU side representation of a rasterising font.
 *
 * <p>The backend acquires the texture lazily, uploads it once and only
 * re-uploads when {@link #revision()} changes. That keeps the per frame cost
 * of native text at zero.</p>
 */
public interface FontTexture {

    int width();

    int height();

    /**
     * Monotonically increasing counter that changes whenever the pixel data
     * changed. The backend uses it to detect stale uploads.
     */
    int revision();

    /**
     * Copies the RGBA8 pixel data of the atlas into the supplied buffer.
     *
     * @param argbOut destination buffer, receives {@code width * height}
     *                packed ARGB values
     */
    void writePixels(int[] argbOut);
}
