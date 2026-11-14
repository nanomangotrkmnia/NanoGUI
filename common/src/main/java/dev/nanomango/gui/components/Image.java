package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.core.GuiContext;

/**
 * Draws a texture that is identified by a numeric texture id.
 *
 * <p>The id is resolved by the render backend, which keeps the component free
 * of any graphics API specific type.</p>
 */
public class Image extends Component {

    private final int textureId;
    private int color = 0xFFFFFFFF;
    private boolean preserveAspectRatio = true;

    public Image(int textureId) {
        this.textureId = textureId;
    }

    public Image color(int color) {
        this.color = color;
        return this;
    }

    public Image preserveAspectRatio(boolean preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
        return this;
    }

    public int textureId() {
        return textureId;
    }

    @Override
    public void render(GuiContext context) {
        float destX = x();
        float destY = y();
        float destWidth = width();
        float destHeight = height();

        if (preserveAspectRatio) {
            float sourceAspect = context.textureAspect(textureId);
            if (sourceAspect > 0.0F) {
                float targetAspect = destWidth / Math.max(1.0F, destHeight);
                if (targetAspect > sourceAspect) {
                    float newWidth = destHeight * sourceAspect;
                    destX += (destWidth - newWidth) * 0.5F;
                    destWidth = newWidth;
                } else {
                    float newHeight = destWidth / sourceAspect;
                    destY += (destHeight - newHeight) * 0.5F;
                    destHeight = newHeight;
                }
            }
        }

        context.draw().texture(textureId, 0.0F, 0.0F, 1.0F, 1.0F, destX, destY, destWidth, destHeight, color);
    }
}
