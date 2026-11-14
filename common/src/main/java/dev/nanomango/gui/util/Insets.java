package dev.nanomango.gui.util;

/**
 * Padding and margin values for a single component.
 */
public final class Insets {

    public static final Insets ZERO = new Insets(0.0F, 0.0F, 0.0F, 0.0F);

    private final float top;
    private final float right;
    private final float bottom;
    private final float left;

    public Insets(float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public static Insets all(float value) {
        return new Insets(value, value, value, value);
    }

    public static Insets symmetric(float vertical, float horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }

    public static Insets of(float top, float right, float bottom, float left) {
        return new Insets(top, right, bottom, left);
    }

    public float top() {
        return top;
    }

    public float right() {
        return right;
    }

    public float bottom() {
        return bottom;
    }

    public float left() {
        return left;
    }

    public float horizontal() {
        return left + right;
    }

    public float vertical() {
        return top + bottom;
    }

    public Insets withTop(float value) {
        return new Insets(value, right, bottom, left);
    }

    public Insets withRight(float value) {
        return new Insets(top, value, bottom, left);
    }

    public Insets withBottom(float value) {
        return new Insets(top, right, value, left);
    }

    public Insets withLeft(float value) {
        return new Insets(top, right, bottom, value);
    }

    public Insets add(Insets other) {
        return new Insets(top + other.top, right + other.right, bottom + other.bottom, left + other.left);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Insets other)) {
            return false;
        }
        return Float.compare(top, other.top) == 0
                && Float.compare(right, other.right) == 0
                && Float.compare(bottom, other.bottom) == 0
                && Float.compare(left, other.left) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(top);
        result = 31 * result + Float.hashCode(right);
        result = 31 * result + Float.hashCode(bottom);
        result = 31 * result + Float.hashCode(left);
        return result;
    }
}
