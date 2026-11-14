package dev.nanomango.gui.util;

/**
 * Helpers for the packed 0xAARRGGBB colour representation used throughout the
 * framework.
 */
public final class Colors {

    public static final int TRANSPARENT = 0x00000000;
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;

    private Colors() {
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clampByte(r) << 16) | (clampByte(g) << 8) | clampByte(b);
    }

    public static int argb(int a, int r, int g, int b) {
        return (clampByte(a) << 24) | (clampByte(r) << 16) | (clampByte(g) << 8) | clampByte(b);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (clampByte(alpha) << 24);
    }

    public static int withAlphaFactor(int color, float factor) {
        int alpha = (int) Math.round(((color >>> 24) & 0xFF) * MathUtil.saturate(factor));
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int red(int color) {
        return (color >>> 16) & 0xFF;
    }

    public static int green(int color) {
        return (color >>> 8) & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static float alphaFraction(int color) {
        return ((color >>> 24) & 0xFF) / 255.0F;
    }

    public static float redFraction(int color) {
        return ((color >>> 16) & 0xFF) / 255.0F;
    }

    public static float greenFraction(int color) {
        return ((color >>> 8) & 0xFF) / 255.0F;
    }

    public static float blueFraction(int color) {
        return (color & 0xFF) / 255.0F;
    }

    /**
     * Multiplies the alpha channel of {@code color} by {@code factor} and
     * returns the result. Used for hover and press feedback.
     */
    public static int fade(int color, float factor) {
        return withAlphaFactor(color, factor);
    }

    public static int mix(int from, int to, float t) {
        return MathUtil.lerpColor(from, to, MathUtil.saturate(t));
    }

    /**
     * Converts a hue/saturation/value triple into a packed colour.
     *
     * @param hue        hue in degrees, normalised to {@code [0, 360)}
     * @param saturation saturation in {@code [0, 1]}
     * @param value     brightness in {@code [0, 1]}
     * @param alpha      alpha in {@code [0, 1]}
     */
    public static int hsv(float hue, float saturation, float value, float alpha) {
        float h = (hue % 360.0F + 360.0F) % 360.0F / 60.0F;
        float s = MathUtil.saturate(saturation);
        float v = MathUtil.saturate(value);
        int sector = (int) h;
        float f = h - sector;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * f);
        float t = v * (1.0F - s * (1.0F - f));
        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            default -> {
                r = v;
                g = p;
                b = q;
            }
        }
        return argb((int) Math.round(MathUtil.saturate(alpha) * 255.0F),
                (int) Math.round(r * 255.0F), (int) Math.round(g * 255.0F), (int) Math.round(b * 255.0F));
    }

    /**
     * Converts a packed colour into hue, saturation and value.
     *
     * @param hsv array of length three that receives hue in {@code [0, 1)},
     *             saturation in {@code [0, 1]} and value in {@code [0, 1]}
     */
    public static void rgbToHsv(int color, float[] hsv) {
        float r = redFraction(color);
        float g = greenFraction(color);
        float b = blueFraction(color);
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == r) {
            hue = 60.0F * (((g - b) / delta) % 6.0F);
        } else if (max == g) {
            hue = 60.0F * (((b - r) / delta) + 2.0F);
        } else {
            hue = 60.0F * (((r - g) / delta) + 4.0F);
        }
        if (hue < 0.0F) {
            hue += 360.0F;
        }

        hsv[0] = hue / 360.0F;
        hsv[1] = max <= 0.0F ? 0.0F : delta / max;
        hsv[2] = max;
    }

    private static int clampByte(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }
}
