package ru.fwx.freewebupdater.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public record UpdaterConfig(
    String manifestUrl,
    boolean removeFilesMissingFromManifest,
    boolean updateOnStartup,
    boolean showProgressWindow,
    boolean autoRestartAfterModUpdates,
    String restartExecutable
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_MANIFEST_URL = "https://example.com/packpulse/manifest.json";

    public static UpdaterConfig load(Path gameDir) {
        try {
            Path configDir = gameDir.resolve("config");
            Files.createDirectories(configDir);

            Path configPath = configDir.resolve("packpulse.json");
            if (!Files.exists(configPath)) {
                UpdaterConfig config = defaults();
                save(configPath, config);
                return config;
            }

            try (Reader reader = Files.newBufferedReader(configPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                UpdaterConfig defaults = defaults();
                UpdaterConfig config = new UpdaterConfig(
                    getString(json, "manifestUrl", defaults.manifestUrl()),
                    getBoolean(json, "removeFilesMissingFromManifest", defaults.removeFilesMissingFromManifest()),
                    getBoolean(json, "updateOnStartup", defaults.updateOnStartup()),
                    getBoolean(json, "showProgressWindow", defaults.showProgressWindow()),
                    getBoolean(json, "autoRestartAfterModUpdates", defaults.autoRestartAfterModUpdates()),
                    getString(json, "restartExecutable", defaults.restartExecutable())
                );

                save(configPath, config);
                return config;
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load updater config", ex);
        }
    }

    public static UpdaterConfig defaults() {
        return new UpdaterConfig(DEFAULT_MANIFEST_URL, false, true, true, false, "");
    }

    private static void save(Path configPath, UpdaterConfig config) throws IOException {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        }
    }

    private static String getString(JsonObject json, String key, String fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        String value = json.get(key).getAsString();
        return value == null || value.isBlank() ? fallback : value;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        return json.get(key).getAsBoolean();
    }
}
