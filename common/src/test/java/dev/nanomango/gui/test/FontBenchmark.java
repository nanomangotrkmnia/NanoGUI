package dev.nanomango.gui.test;

import dev.nanomango.gui.font.AwtFont;
import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.FontLoader;
import dev.nanomango.gui.font.NativeFont;

import java.io.IOException;

/**
 * Measures the throughput of the native font renderer against the Java2D
 * fallback.
 *
 * <p>Both implementations use the same measurement cache, so the benchmark
 * compares the cost of a cold measurement (the first time a string is seen)
 * with the cost of a cached measurement (the steady state of a running GUI).</p>
 *
 * <p>Run it with {@code ./gradlew :common:benchmark}.</p>
 */
public final class FontBenchmark {

    private FontBenchmark() {
    }

    /**
     * Entry point of the benchmark.
     */
    public static void main(String[] args) throws Exception {
        byte[] data = FontLoader.read(FontLoader.INTER_REGULAR);
        String text = "NanoArray performance probe 1234567890";

        Font nativeFont = new NativeFont(0, "Inter (native)", data, 16.0F);
        Font fallback = new AwtFont(1, "Inter (Java2D)", data, 16.0F, java.awt.Font.PLAIN);

        System.out.println();
        System.out.println("NanoArray font benchmark");
        System.out.println("========================");
        System.out.println("text length      : " + text.length() + " characters");
        System.out.println("measured width   : native=" + nativeFont.width(text)
                + " java=" + fallback.width(text));
        System.out.println();

        // Cold measurement: a fresh cache for every single string.
        long nativeCold = measureCold(() -> new NativeFont(0, "native", data, 16.0F), text, 200);
        long javaCold = measureCold(
                () -> new AwtFont(1, "java", data, 16.0F, java.awt.Font.PLAIN), text, 200);
        System.out.printf("cold measurement : native=%8d ns  java=%8d ns  speedup=%.2fx%n",
                nativeCold, javaCold, ratio(javaCold, nativeCold));

        // Warm measurement: the cache is hit on every call.
        long nativeWarm = measureWarm(nativeFont, text, 1_000_000);
        long javaWarm = measureWarm(fallback, text, 1_000_000);
        System.out.printf("warm measurement : native=%8d ns  java=%8d ns  speedup=%.2fx%n",
                nativeWarm, javaWarm, ratio(javaWarm, nativeWarm));

        System.out.println();
    }

    private static double ratio(long reference, long value) {
        if (value <= 0) {
            return Double.NaN;
        }
        return (double) reference / (double) value;
    }

    private static long measureCold(FontFactory factory, String text, int iterations) {
        long total = 0L;
        for (int i = 0; i < iterations; i++) {
            Font font;
            try {
                font = factory.create();
            } catch (Exception exception) {
                throw new IllegalStateException("The benchmark font could not be created", exception);
            }
            long start = System.nanoTime();
            font.width(text + i);
            total += System.nanoTime() - start;
            if (font instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                    // Nothing to do, the font is only used for the measurement.
                }
            }
        }
        return total / iterations;
    }

    /**
     * Creates a font for the benchmark. The factory may throw a checked
     * exception because the Java2D font can reject the font data.
     */
    @FunctionalInterface
    private interface FontFactory {
        Font create() throws Exception;
    }
    private static long measureWarm(Font font, String text, int iterations) {
        for (int i = 0; i < 1000; i++) {
            font.width(text);
        }
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            font.width(text);
        }
        return (System.nanoTime() - start) / iterations;
    }
}
