package dev.nanomango.gui.font;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounded cache for text measurements.
 *
 * <p>A GUI measures the same strings over and over: every frame measures the
 * labels of the visible components. Caching the result removes the shaping
 * cost from the hot path and makes the measurement allocation free after the
 * first frame.</p>
 *
 * <p>The cache uses least recently used eviction and is not thread safe.
 * Every font belongs to a single render thread.</p>
 */
public final class MeasurementCache {

    /** Default number of entries. */
    public static final int DEFAULT_CAPACITY = 256;

    private final Map<String, Float> entries;
    private final int capacity;

    public MeasurementCache() {
        this(DEFAULT_CAPACITY);
    }

    public MeasurementCache(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.entries = new LinkedHashMap<>(16, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Float> eldest) {
                return size() > MeasurementCache.this.capacity;
            }
        };
    }

    /**
     * Returns the cached width of a string.
     *
     * @return the cached width or {@code null} when the string is unknown
     */
    public Float get(String text) {
        return entries.get(text);
    }

    /**
     * Stores the width of a string.
     */
    public void put(String text, float width) {
        entries.put(text, width);
    }

    /**
     * Number of cached entries.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Removes every cached measurement.
     */
    public void clear() {
        entries.clear();
    }
}
