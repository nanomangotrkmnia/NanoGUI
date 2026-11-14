package dev.nanomango.gui.font;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Loader for the bundled native font renderer.
 *
 * <p>The library is shipped inside the mod jar for every supported platform.
 * At runtime it is extracted into a temporary directory and loaded through
 * {@link System#load(String)}. Users therefore never need a compiler or a
 * toolchain.</p>
 *
 * <p>The loader searches the following locations, in order:</p>
 * <ol>
 *   <li>The directory given by the {@code nanogui.native.path} system property.</li>
 *   <li>The library bundled in the classpath for the current platform.</li>
 *   <li>The directories listed in {@code java.library.path}.</li>
 * </ol>
 */
public final class NativeLibrary {

    /** Base name of the native library, without the platform prefix and suffix. */
    public static final String LIBRARY_NAME = "nanogui_font";

    /** System property that points at a directory containing the native library. */
    public static final String PATH_PROPERTY = "nanogui.native.path";

    private static final Object LOCK = new Object();

    private static boolean loaded;
    private static Throwable failure;

    private NativeLibrary() {
    }

    /**
     * Loads the native library.
     *
     * <p>The method is idempotent: the library is only loaded once. When the
     * library cannot be loaded the failure is recorded and can be inspected
     * with {@link #failure()}.</p>
     *
     * @return {@code true} when the library is available
     */
    public static boolean load() {
        synchronized (LOCK) {
            if (loaded) {
                return true;
            }
            if (failure != null) {
                return false;
            }
            try {
                loadInternal();
                loaded = true;
                return true;
            } catch (Throwable throwable) {
                failure = throwable;
                return false;
            }
        }
    }

    /**
     * True when the native library was loaded successfully.
     */
    public static boolean isLoaded() {
        synchronized (LOCK) {
            return loaded;
        }
    }

    /**
     * The error that prevented the library from loading, or {@code null}.
     */
    public static Throwable failure() {
        synchronized (LOCK) {
            return failure;
        }
    }

    private static void loadInternal() throws IOException {
        Platform platform = Platform.current();

        Path explicit = explicitPath();
        if (explicit != null && Files.exists(explicit)) {
            System.load(explicit.toAbsolutePath().toString());
            return;
        }

        Path extracted = extract(platform);
        if (extracted != null) {
            System.load(extracted.toAbsolutePath().toString());
            return;
        }

        // Last resort: let the JVM search java.library.path.
        System.loadLibrary(LIBRARY_NAME);
    }

    private static Path explicitPath() {
        String value = System.getProperty(PATH_PROPERTY);
        if (value == null || value.isBlank()) {
            return null;
        }
        Path directory = Path.of(value);
        Path direct = directory.resolve(Platform.current().fileName());
        if (Files.exists(direct)) {
            return direct;
        }
        return Files.exists(directory) ? directory : null;
    }

    private static Path extract(Platform platform) throws IOException {
        String resource = platform.resourcePath();
        InputStream stream = NativeLibrary.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            return null;
        }
        Path directory = Files.createTempDirectory("nanogui-native");
        directory.toFile().deleteOnExit();
        Path target = directory.resolve(platform.fileName());
        target.toFile().deleteOnExit();

        try (InputStream input = stream; OutputStream output = Files.newOutputStream(target)) {
            input.transferTo(output);
        }

        // Make the library executable on platforms that require it.
        target.toFile().setExecutable(true, false);
        return target;
    }

    /**
     * Copies the library for the given platform out of the classpath.
     *
     * @return the path of the extracted file or {@code null} when the library
     *         is not bundled for that platform
     */
    public static Path extractFor(NativeLibrary.Platform platform) throws IOException {
        return extract(platform);
    }

    /**
     * The platforms the native renderer is built for.
     */
    public enum Platform {

        MACOS_X86_64("macos", "x86_64", "libnanogui_font.dylib"),
        MACOS_ARM64("macos", "aarch64", "libnanogui_font.dylib"),
        WINDOWS_X86_64("windows", "x86_64", "nanogui_font.dll"),
        WINDOWS_ARM64("windows", "aarch64", "nanogui_font.dll"),
        LINUX_X86_64("linux", "x86_64", "libnanogui_font.so"),
        LINUX_ARM64("linux", "aarch64", "libnanogui_font.so");

        private final String os;
        private final String arch;
        private final String fileName;

        Platform(String os, String arch, String fileName) {
            this.os = os;
            this.arch = arch;
            this.fileName = fileName;
        }

        /**
         * The operating system part of the platform identifier.
         */
        public String os() {
            return os;
        }

        /**
         * The architecture part of the platform identifier.
         */
        public String arch() {
            return arch;
        }

        /**
         * File name of the native library on this platform.
         */
        public String fileName() {
            return fileName;
        }

        /**
         * Path of the library inside the mod jar.
         */
        public String resourcePath() {
            return "dev/nanomango/native/" + os + "-" + arch + "/" + fileName;
        }

        /**
         * Detects the platform the game runs on.
         *
         * @throws IllegalStateException when the platform is not supported
         */
        public static Platform current() {
            String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            String os;
            if (osName.contains("win")) {
                os = "windows";
            } else if (osName.contains("mac") || osName.contains("darwin")) {
                os = "macos";
            } else if (osName.contains("nux") || osName.contains("nix")) {
                os = "linux";
            } else {
                throw new IllegalStateException("Unsupported operating system: " + osName);
            }

            String archName = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
            String arch;
            if (archName.equals("amd64") || archName.equals("x86_64")) {
                arch = "x86_64";
            } else if (archName.equals("aarch64") || archName.equals("arm64")) {
                arch = "aarch64";
            } else {
                throw new IllegalStateException("Unsupported architecture: " + archName);
            }

            for (Platform platform : values()) {
                if (platform.os.equals(os) && platform.arch.equals(arch)) {
                    return platform;
                }
            }
            throw new IllegalStateException("Unsupported platform: " + os + "/" + arch);
        }
    }
}
