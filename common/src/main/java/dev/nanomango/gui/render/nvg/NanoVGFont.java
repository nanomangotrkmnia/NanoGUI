package dev.nanomango.gui.render.nvg;

/**
 * A font that is registered with NanoVG.
 *
 * <p>NanoVG can rasterise TrueType fonts itself. When a framework font is
 * registered that way the backend can draw whole text runs with a single call
 * instead of one textured quad per glyph, which is both faster and sharper at
 * small sizes.</p>
 *
 * @param handle NanoVG font handle, must be positive
 * @param size   font size in GUI units
 */
public record NanoVGFont(int handle, float size) {

    /**
     * Creates a font descriptor.
     *
     * @throws IllegalArgumentException if the handle is not positive or the
     *                                  size is not positive
     */
    public NanoVGFont {
        if (handle <= 0) {
            throw new IllegalArgumentException("The NanoVG font handle must be positive");
        }
        if (!(size > 0.0F)) {
            throw new IllegalArgumentException("The font size must be positive");
        }
    }

    /**
     * Returns whether this descriptor points at a usable font.
     */
    public boolean valid() {
        return handle > 0 && size > 0.0F;
    }
}
