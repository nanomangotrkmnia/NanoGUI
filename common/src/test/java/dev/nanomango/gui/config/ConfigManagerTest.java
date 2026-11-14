package dev.nanomango.gui.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    @TempDir
    Path directory;

    @Test
    @DisplayName("a missing file starts with an empty configuration")
    void missingFile() {
        ConfigManager config = new ConfigManager(directory.resolve("missing.json"));
        config.load();
        assertTrue(config.isLoaded());
        assertFalse(config.isDirty());
        assertEquals("fallback", config.getString("key", "fallback"));
    }

    @Test
    @DisplayName("values survive a save and load cycle")
    void roundTrip() {
        Path file = directory.resolve("config.json");

        ConfigManager writer = new ConfigManager(file);
        writer.load();
        writer.set("theme", "dark");
        writer.set("scale", 3);
        writer.set("volume", 0.75F);
        writer.set("enabled", true);
        assertTrue(writer.flush());
        assertFalse(writer.isDirty());

        ConfigManager reader = new ConfigManager(file);
        reader.load();
        assertEquals("dark", reader.getString("theme", "light"));
        assertEquals(3, reader.getInt("scale", 1));
        assertEquals(0.75F, reader.getFloat("volume", 0.0F));
        assertTrue(reader.getBoolean("enabled", false));
    }

    @Test
    @DisplayName("flushing without changes does not touch the disk")
    void flushWithoutChanges() {
        Path file = directory.resolve("config.json");
        ConfigManager config = new ConfigManager(file);
        config.load();
        assertFalse(config.flush());
        assertFalse(Files.exists(file));
    }

    @Test
    @DisplayName("a corrupted file is backed up and the configuration starts empty")
    void corruptedFile() throws IOException {
        Path file = directory.resolve("broken.json");
        Files.writeString(file, "{ this is not valid json", StandardCharsets.UTF_8);

        ConfigManager config = new ConfigManager(file);
        config.load();

        assertTrue(config.isLoaded());
        assertEquals("fallback", config.getString("theme", "fallback"));
        assertTrue(Files.exists(directory.resolve("broken.json.broken")),
                "the corrupted file must be kept as a backup");
        assertFalse(Files.exists(file), "the corrupted file must not stay in place");
    }

    @Test
    @DisplayName("a file with the wrong shape is handled gracefully")
    void wrongShape() throws IOException {
        Path file = directory.resolve("array.json");
        Files.writeString(file, "[1, 2, 3]", StandardCharsets.UTF_8);

        ConfigManager config = new ConfigManager(file);
        config.load();
        assertEquals("fallback", config.getString("key", "fallback"));
    }

    @Test
    @DisplayName("values of the wrong type fall back to the default")
    void wrongType() throws IOException {
        Path file = directory.resolve("types.json");
        Files.writeString(file, "{\"number\": \"not a number\"}", StandardCharsets.UTF_8);

        ConfigManager config = new ConfigManager(file);
        config.load();
        assertEquals(42, config.getInt("number", 42));
        assertEquals(7.5F, config.getFloat("number", 7.5F));
    }

    @Test
    @DisplayName("the written file is valid JSON")
    void writtenFileIsValidJson() throws IOException {
        Path file = directory.resolve("valid.json");
        ConfigManager config = new ConfigManager(file);
        config.load();
        config.set("name", "NanoGUI");
        config.set("count", 5);
        config.flush();

        JsonObject parsed = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertEquals("NanoGUI", parsed.get("name").getAsString());
        assertEquals(5, parsed.get("count").getAsInt());
    }

    @Test
    @DisplayName("removing a key marks the configuration as dirty")
    void remove() {
        ConfigManager config = new ConfigManager(directory.resolve("remove.json"));
        config.load();
        config.set("key", "value");
        config.flush();
        assertFalse(config.isDirty());

        config.remove("key");
        assertTrue(config.isDirty());
        assertFalse(config.contains("key"));
    }

    @Test
    @DisplayName("namespaced accessors read and write values")
    void namespaced() {
        ConfigManager config = new ConfigManager(directory.resolve("namespaced.json"));
        config.load();
        config.put("module.example.enabled", true);
        assertEquals(Boolean.TRUE, config.get("module.example.enabled", false));
        assertEquals("fallback", config.get("module.example.missing", "fallback"));
    }

    @Test
    @DisplayName("the parent directory is created when it does not exist")
    void createsParentDirectory() {
        Path file = directory.resolve("nested/deeper/config.json");
        ConfigManager config = new ConfigManager(file);
        config.load();
        config.set("key", "value");
        assertTrue(config.flush());
        assertTrue(Files.exists(file));
    }
}
