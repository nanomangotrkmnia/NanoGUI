package dev.nanomango.nanoarray.backend;

import dev.nanomango.gui.render.CornerSprites;

/**
 * Draws rounded rectangles with Minecraft's drawing primitives.
 *
 * <p>A rounded rectangle is split into two axis aligned rectangles for the
 * straight edges and four anti-aliased corner sprites for the corners. The
 * corners are generated from the exact geometry of a disc, so they stay
 * smooth at every GUI scale and never look like a staircase.</p>
 *
 * <p>All coordinates follow the convention of Minecraft: the bottom right
 * corner of a rectangle is exclusive, so a rectangle that spans from
 * {@code left} to {@code right} is {@code right - left} pixels wide.</p>
 */
public final class RoundedRectangles {

    private final RoundedCornerAtlas atlas;

    public RoundedRectangles(RoundedCornerAtlas atlas) {
        this.atlas = atlas;
    }

    /**
     * Draws a filled rounded rectangle.
     */
    public void draw(NanoArrayGraphics graphics, float x, float y, float width, float height,
                     float radius, int argb) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        int w = right - left;
        int h = bottom - top;
        if (w <= 0 || h <= 0) {
            return;
        }

        int r = Math.min(Math.round(radius), Math.min(w / 2, h / 2));
        if (r <= 0) {
            graphics.fill(left, top, right, bottom, argb);
            return;
        }

        // The straight edges are filled with plain rectangles, which keeps
        // them perfectly crisp. The vertical band covers the full height of
        // the rectangle, the horizontal band the full width.
        graphics.fill(left + r, top, right - r, bottom, argb);
        graphics.fill(left, top + r, right, bottom - r, argb);

        RoundedCornerAtlas.Sprite sprite = atlas.fill(r);
        blit(graphics, sprite, CornerSprites.TOP_LEFT, left, top, r, argb);
        blit(graphics, sprite, CornerSprites.TOP_RIGHT, right - r, top, r, argb);
        blit(graphics, sprite, CornerSprites.BOTTOM_LEFT, left, bottom - r, r, argb);
        blit(graphics, sprite, CornerSprites.BOTTOM_RIGHT, right - r, bottom - r, r, argb);
    }

    /**
     * Draws the outline of a rounded rectangle.
     *
     * @param thickness the width of the outline in pixels
     */
    public void stroke(NanoArrayGraphics graphics, float x, float y, float width, float height,
                       float radius, float thickness, int argb) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        int w = right - left;
        int h = bottom - top;
        if (w <= 0 || h <= 0) {
            return;
        }

        int t = Math.max(1, Math.round(thickness));
        int r = Math.min(Math.round(radius), Math.min(w / 2, h / 2));
        if (r <= 0) {
            graphics.fill(left, top, right, top + t, argb);
            graphics.fill(left, bottom - t, right, bottom, argb);
            graphics.fill(left, top + t, left + t, bottom - t, argb);
            graphics.fill(right - t, top + t, right, bottom - t, argb);
            return;
        }

        t = Math.min(t, r);

        // The straight edges of the outline.
        graphics.fill(left + r, top, right - r, top + t, argb);
        graphics.fill(left + r, bottom - t, right - r, bottom, argb);
        graphics.fill(left, top + r, left + t, bottom - r, argb);
        graphics.fill(right - t, top + r, right, bottom - r, argb);

        RoundedCornerAtlas.Sprite sprite = atlas.stroke(r, t);
        blit(graphics, sprite, CornerSprites.TOP_LEFT, left, top, r, argb);
        blit(graphics, sprite, CornerSprites.TOP_RIGHT, right - r, top, r, argb);
        blit(graphics, sprite, CornerSprites.BOTTOM_LEFT, left, bottom - r, r, argb);
        blit(graphics, sprite, CornerSprites.BOTTOM_RIGHT, right - r, bottom - r, r, argb);
    }

    /**
     * Draws a soft shadow below a rounded rectangle.
     *
     * <p>Minecraft's GUI API has no blurred primitive, so the shadow is
     * approximated with two concentric rounded rectangles. The outer one is
     * slightly larger and more transparent than the inner one, which produces
     * a smooth falloff.</p>
     *
     * @param blur the radius of the shadow in pixels
     */
    public void shadow(NanoArrayGraphics graphics, float x, float y, float width, float height,
                       float radius, float blur, int argb) {
        int alpha = argb >>> 24;
        if (alpha == 0) {
            return;
        }
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        float spread = Math.max(0.0F, blur) * 0.45F;
        int outerAlpha = Math.round(alpha * 0.32F);
        int innerAlpha = alpha - outerAlpha;

        if (spread > 0.5F) {
            draw(graphics, x - spread, y - spread, width + spread * 2.0F, height + spread * 2.0F,
                    radius + spread, pack(red, green, blue, outerAlpha));
        }
        draw(graphics, x, y, width, height, radius, pack(red, green, blue, innerAlpha));
    }

    private static int pack(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private void blit(NanoArrayGraphics graphics, RoundedCornerAtlas.Sprite sprite,
                      int orientation, int x, int y, int size, int argb) {
        // The sprite stores white coverage, so the colour of the rounded
        // rectangle has to tint it. Drawing it white was why the corners of
        // every panel stood out against the fill. The source is rasterised at
        // the physical resolution and the destination is in GUI units, which
        // keeps the sprite one to one on screen.
        graphics.blit(sprite.identifier(), sprite.sourceU(orientation), 0.0F,
                x, y, size, size, sprite.cellSize(), sprite.cellSize(),
                sprite.textureWidth(), sprite.textureHeight(), argb);
    }
}
