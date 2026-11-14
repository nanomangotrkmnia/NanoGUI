package dev.nanomango.gui.util;

/**
 * Small allocation free math helpers used across the GUI framework.
 *
 * <p>All methods are static and free of side effects so they can be used from
 * hot rendering paths.</p>
 */
public final class MathUtil {

    private MathUtil() {
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    public static int lerpColor(int from, int to, float t) {
        int a = lerpInt((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, t);
        int r = lerpInt((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, t);
        int g = lerpInt((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, t);
        int b = lerpInt(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lerpInt(int from, int to, float t) {
        return (int) Math.round(from + (to - from) * t);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float saturate(float value) {
        return clamp(value, 0.0F, 1.0F);
    }

    public static boolean approximately(float a, float b, float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    /**
     * Moves a value a fraction of the way towards a target.
     *
     * @param value    the current value
     * @param target   the value to move towards
     * @param fraction the fraction of the remaining distance that is covered,
     *                 clamped to {@code [0, 1]}
     */
    public static float approach(float value, float target, float fraction) {
        return value + (target - value) * saturate(fraction);
    }

    /**
     * Maps {@code value} from one range onto another. The target range may be
     * inverted, which is how progress bars and sliders handle reversed axes.
     */
    public static float mapRange(float value, float fromMin, float fromMax, float toMin, float toMax) {
        if (fromMax == fromMin) {
            return toMin;
        }
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    /**
     * Snaps {@code value} to the closest multiple of {@code step}. A step of
     * zero returns the value unchanged.
     */
    public static float snap(float value, float step) {
        if (step <= 0.0F) {
            return value;
        }
        return Math.round(value / step) * step;
    }

    /**
     * Converts a delta time in seconds into a frame rate independent smoothing
     * factor. {@code smoothing} is the fraction of the remaining distance that
     * is covered per second.
     */
    public static float damping(float smoothing, float deltaSeconds) {
        return 1.0F - (float) Math.pow(1.0F - saturate(smoothing), deltaSeconds * 60.0F);
    }

    public static boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static boolean contains(float x, float y, float px, float py, float width, float height) {
        return px <= x && x < px + width && py <= y && y < py + height;
    }

    public static float roundToPixel(float value, float scale) {
        return Math.round(value * scale) / scale;
    }
}
