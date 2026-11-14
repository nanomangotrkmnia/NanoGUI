package dev.nanomango.gui.render.nvg;

/**
 * A texture that is registered with NanoVG.
 *
 * <p>Only the handle is needed for drawing. The dimensions are kept so that
 * backends can convert pixel based source rectangles into the normalised
 * coordinates that NanoVG expects.</p>
 *
 * @param handle NanoVG image handle, must be positive
 * @param width  width of the image in pixels
 * @param height height of the image in pixels
 */
public record NanoVGImage(int handle, int width, int height) {

    /**
     * Creates an image descriptor.
     *
     * @throws IllegalArgumentException if the handle is not positive or the
     *                                  dimensions are not positive
     */
    public NanoVGImage {
        if (handle <= 0) {
            throw new IllegalArgumentException("The NanoVG image handle must be positive");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("The image dimensions must be positive");
        }
    }
}
