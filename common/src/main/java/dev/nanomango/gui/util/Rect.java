package dev.nanomango.gui.util;

/**
 * An immutable axis aligned rectangle in GUI space.
 *
 * <p>The rectangle is stored as origin plus size because that is the form the
 * layout engine and the render backends work with most often.</p>
 */
public final class Rect {

    public static final Rect EMPTY = new Rect(0.0F, 0.0F, 0.0F, 0.0F);

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Rect of(float x, float y, float width, float height) {
        return new Rect(x, y, width, height);
    }

    public static Rect between(float x1, float y1, float x2, float y2) {
        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public float centerX() {
        return x + width * 0.5F;
    }

    public float centerY() {
        return y + height * 0.5F;
    }

    public boolean isEmpty() {
        return width <= 0.0F || height <= 0.0F;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < right() && py >= y && py < bottom();
    }

    public boolean intersects(Rect other) {
        return x < other.right() && right() > other.x && y < other.bottom() && bottom() > other.y;
    }

    public boolean contains(Rect other) {
        return other.x >= x && other.y >= y && other.right() <= right() && other.bottom() <= bottom();
    }

    public Rect translate(float dx, float dy) {
        return new Rect(x + dx, y + dy, width, height);
    }

    public Rect inset(float amount) {
        return new Rect(x + amount, y + amount, width - amount * 2.0F, height - amount * 2.0F);
    }

    public Rect inset(Insets insets) {
        return new Rect(x + insets.left(), y + insets.top(),
                width - insets.left() - insets.right(),
                height - insets.top() - insets.bottom());
    }

    public Rect expand(float amount) {
        return inset(-amount);
    }

    public Rect intersect(Rect other) {
        float nx = Math.max(x, other.x);
        float ny = Math.max(y, other.y);
        float nr = Math.min(right(), other.right());
        float nb = Math.min(bottom(), other.bottom());
        if (nr <= nx || nb <= ny) {
            return EMPTY;
        }
        return new Rect(nx, ny, nr - nx, nb - ny);
    }

    /**
     * Returns the largest radius that still fits inside the rectangle while
     * producing a visually correct rounded shape.
     */
    public float clampRadius(float radius) {
        return Math.min(radius, Math.min(width, height) * 0.5F);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rect other)) {
            return false;
        }
        return Float.compare(x, other.x) == 0
                && Float.compare(y, other.y) == 0
                && Float.compare(width, other.width) == 0
                && Float.compare(height, other.height) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(x);
        result = 31 * result + Float.hashCode(y);
        result = 31 * result + Float.hashCode(width);
        result = 31 * result + Float.hashCode(height);
        return result;
    }

    @Override
    public String toString() {
        return "Rect[" + x + ", " + y + ", " + width + "x" + height + "]";
    }
}
