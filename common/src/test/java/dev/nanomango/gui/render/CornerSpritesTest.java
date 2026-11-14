package dev.nanomango.gui.render;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CornerSpritesTest {

    private static int alpha(int[] pixels, int width, int x, int y) {
        return pixels[y * width + x] >>> 24;
    }

    @Test
    @DisplayName("the texture holds four square cells")
    void dimensions() {
        assertEquals(4 * 8, CornerSprites.textureWidth(8));
        assertEquals(8, CornerSprites.textureHeight(8));
        assertEquals(4 * 1, CornerSprites.textureWidth(1));
        assertEquals(1, CornerSprites.textureHeight(1));
    }

    @Test
    @DisplayName("the top left corner is empty at the outer edge")
    void topLeftCorner() {
        int radius = 8;
        int[] pixels = CornerSprites.generateFill(radius);
        int width = CornerSprites.textureWidth(radius);

        // The disc of the corner is centred at the bottom right of the cell,
        // so the outer corner stays empty and the inner one is covered.
        assertEquals(0, alpha(pixels, width, 0, 0));
        assertEquals(255, alpha(pixels, width, radius - 1, radius - 1));
    }

    @Test
    @DisplayName("every orientation is the mirror of the top left corner")
    void orientations() {
        int radius = 8;
        int[] pixels = CornerSprites.generateFill(radius);
        int width = CornerSprites.textureWidth(radius);

        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                int topLeft = alpha(pixels, width, x, y);
                int topRight = alpha(pixels, width, radius + x, y);
                int bottomLeft = alpha(pixels, width, 2 * radius + x, y);
                int bottomRight = alpha(pixels, width, 3 * radius + x, y);

                assertEquals(alpha(pixels, width, radius - 1 - x, y), topRight,
                        "the top right corner must be mirrored horizontally");
                assertEquals(alpha(pixels, width, x, radius - 1 - y), bottomLeft,
                        "the bottom left corner must be mirrored vertically");
                assertEquals(alpha(pixels, width, radius - 1 - x, radius - 1 - y), bottomRight,
                        "the bottom right corner must be mirrored in both axes");
            }
        }
    }

    @Test
    @DisplayName("the covered area of a corner is a quarter disc")
    void quarterDiscArea() {
        for (int radius : new int[]{4, 8, 16, 32}) {
            int[] pixels = CornerSprites.generateFill(radius);
            int width = CornerSprites.textureWidth(radius);

            double covered = 0.0;
            for (int y = 0; y < radius; y++) {
                for (int x = 0; x < radius; x++) {
                    covered += alpha(pixels, width, x, y) / 255.0;
                }
            }

            double expected = Math.PI * radius * radius / 4.0;
            assertTrue(Math.abs(covered - expected) < 1.5,
                    "the corner of radius " + radius + " must cover " + expected
                            + " pixels but covered " + covered);
        }
    }

    @Test
    @DisplayName("the outline covers less than the filled corner")
    void strokeArea() {
        int radius = 12;
        int width = CornerSprites.textureWidth(radius);
        int[] fill = CornerSprites.generateFill(radius);
        int[] stroke = CornerSprites.generateStroke(radius, 2);

        double fillArea = 0.0;
        double strokeArea = 0.0;
        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                fillArea += alpha(fill, width, x, y) / 255.0;
                strokeArea += alpha(stroke, width, x, y) / 255.0;
            }
        }

        assertTrue(strokeArea > 0.0, "the outline must cover something");
        assertTrue(strokeArea < fillArea,
                "the outline must cover less than the filled corner: " + strokeArea + " vs " + fillArea);
    }

    @Test
    @DisplayName("the outline is hollow in the middle of the corner")
    void strokeHollow() {
        int radius = 12;
        int width = CornerSprites.textureWidth(radius);
        int[] stroke = CornerSprites.generateStroke(radius, 2);

        // The disc is centred at the bottom right of the cell, so the middle
        // of the corner is inside the inner radius and therefore empty.
        assertEquals(0, alpha(stroke, width, radius - 1, radius - 1));
        // The outer edge of the corner is covered.
        assertEquals(255, alpha(stroke, width, 0, radius - 1));
        // The corner of the square lies outside of the arc.
        assertEquals(0, alpha(stroke, width, 0, 0));
    }

    @Test
    @DisplayName("a thick outline fills the whole corner")
    void thickStroke() {
        int radius = 8;
        int width = CornerSprites.textureWidth(radius);
        int[] stroke = CornerSprites.generateStroke(radius, radius);
        int[] fill = CornerSprites.generateFill(radius);

        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                assertEquals(alpha(fill, width, x, y), alpha(stroke, width, x, y),
                        "an outline as thick as the radius must fill the corner");
            }
        }
    }

    @Test
    @DisplayName("the alpha values fade out towards the edge")
    void antiAliasing() {
        int radius = 16;
        int width = CornerSprites.textureWidth(radius);
        int[] pixels = CornerSprites.generateFill(radius);

        // There must be pixels that are only partially covered, otherwise the
        // corner would not be anti-aliased.
        int partial = 0;
        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                int value = alpha(pixels, width, x, y);
                if (value > 0 && value < 255) {
                    partial++;
                }
            }
        }
        assertTrue(partial >= radius, "the corner must have anti-aliased pixels, found " + partial);
    }

    @Test
    @DisplayName("the pixels are white so that they can be tinted")
    void whitePixels() {
        int[] pixels = CornerSprites.generateFill(8);
        for (int pixel : pixels) {
            assertEquals(0x00FFFFFF, pixel & 0x00FFFFFF);
        }
    }
}
