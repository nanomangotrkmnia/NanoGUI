package dev.nanomango.gui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent JSON backed configuration store.
 *
 * <p>The store keeps the parsed document in memory and only touches the disk
 * when something actually changed. Writes happen through {@link #flush()}
 * which the host calls when the game shuts down or on a slow timer.</p>
 */
public final class ConfigManager {

    private final Path file;
    private final Gson gson;
    private final Map<String, JsonElement> values = new LinkedHashMap<>();
    private boolean dirty;
    private boolean loaded;

    public ConfigManager(Path file) {
        this.file = file;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    public Path file() {
        return file;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Reads the configuration from disk. Missing or corrupted files are
     * handled gracefully: the configuration simply starts empty.
     */
    public void load() {
        values.clear();
        dirty = false;
        loaded = true;
        if (file == null || !Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException | RuntimeException exception) {
            // A corrupted configuration must never prevent the client from
            // starting. The broken file is kept as a backup.
            values.clear();
            backupCorruptedFile();
        }
    }

    private void backupCorruptedFile() {
        if (file == null || !Files.exists(file)) {
            return;
        }
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".broken");
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Best effort only.
        }
    }

    /**
     * Writes the configuration to disk when it changed.
     *
     * @return {@code true} when a file was written
     */
    public boolean flush() {
        if (!dirty || file == null) {
            return false;
        }
        dirty = false;
        JsonObject object = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            object.add(entry.getKey(), entry.getValue());
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                gson.toJson(object, writer);
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    // ------------------------------------------------------------------
    // Typed accessors
    // ------------------------------------------------------------------

    public void set(String key, String value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void set(String key, int value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void set(String key, long value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void set(String key, float value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void set(String key, double value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void set(String key, boolean value) {
        values.put(key, gson.toJsonTree(value));
        dirty = true;
    }

    public void setRaw(String key, JsonElement value) {
        values.put(key, value);
        dirty = true;
    }

    public String getString(String key, String fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        return element.getAsString();
    }

    public int getInt(String key, int fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public long getLong(String key, long fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            return element.getAsLong();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public float getFloat(String key, float fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            return element.getAsFloat();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        try {
            return element.getAsBoolean();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public JsonElement getRaw(String key) {
        return values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }

    public void remove(String key) {
        if (values.remove(key) != null) {
            dirty = true;
        }
    }

    public Map<String, JsonElement> entries() {
        return values;
    }

    /**
     * Stores a value under a namespaced key, for example
     * {@code put("module.foo.enabled", true)}.
     */
    public void put(String key, Object value) {
        if (value instanceof String string) {
            set(key, string);
        } else if (value instanceof Integer number) {
            set(key, number);
        } else if (value instanceof Long number) {
            set(key, number);
        } else if (value instanceof Float number) {
            set(key, number);
        } else if (value instanceof Double number) {
            set(key, number);
        } else if (value instanceof Boolean flag) {
            set(key, flag);
        } else if (value != null) {
            setRaw(key, gson.toJsonTree(value));
        }
    }

    /**
     * Reads a namespaced value.
     */
    public Object get(String key, Object fallback) {
        JsonElement element = values.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return fallback;
        }
        if (element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }
        if (element.getAsJsonPrimitive().isNumber()) {
            return element.getAsDouble();
        }
        return element.getAsString();
    }
}
