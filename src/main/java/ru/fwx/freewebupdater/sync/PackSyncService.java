package ru.fwx.freewebupdater.sync;

import com.google.gson.Gson;
import ru.fwx.freewebupdater.PackPulseClient;
import ru.fwx.freewebupdater.config.UpdaterConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public final class PackSyncService {
    private static final Gson GSON = new Gson();
    private static final Set<String> ALLOWED_ROOTS = Set.of("mods", "config", "resourcepacks", "shaderpacks");

    private final Path gameDir;
    private final UpdaterConfig config;
    private final HttpClient httpClient;

    public PackSyncService(Path gameDir, UpdaterConfig config) {
        this.gameDir = gameDir;
        this.config = config;
        this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    public PackManifest loadManifest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(parseRemoteUri(config.manifestUrl(), "manifestUrl")).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Manifest request failed with status " + response.statusCode());
        }

        try (Reader reader = new java.io.InputStreamReader(response.body())) {
            PackManifest manifest = GSON.fromJson(reader, PackManifest.class);
            if (manifest == null || manifest.files() == null) {
                throw new IOException("Manifest is empty or invalid");
            }
            return manifest;
        }
    }

    public PackSyncResult sync(PackManifest manifest, Consumer<SyncProgressInfo> progressConsumer) throws IOException, InterruptedException {
        int updated = 0;
        int removed = 0;
        boolean restartRequired = false;
        int current = 0;
        int total = manifest.files().size();

        Set<String> expectedPaths = new HashSet<>();
        for (PackFileEntry file : manifest.files()) {
            current++;
            validateRelativePath(file.path());
            expectedPaths.add(normalizePath(file.path()));
            progressConsumer.accept(new SyncProgressInfo(file.path(), SyncStage.CHECKING, current, total));

            Path destination = gameDir.resolve(file.path().replace("/", java.io.File.separator));
            Files.createDirectories(destination.getParent());

            boolean shouldDownload = true;
            if (Files.exists(destination) && file.sha256() != null && !file.sha256().isBlank()) {
                String currentHash = sha256(destination);
                shouldDownload = !currentHash.equalsIgnoreCase(file.sha256());
            } else if (Files.exists(destination) && (file.sha256() == null || file.sha256().isBlank())) {
                shouldDownload = false;
            }

            if (!shouldDownload) {
                progressConsumer.accept(new SyncProgressInfo(file.path(), SyncStage.UP_TO_DATE, current, total));
                continue;
            }

            progressConsumer.accept(new SyncProgressInfo(file.path(), SyncStage.DOWNLOADING, current, total));
            downloadTo(file.url(), destination);
            updated++;

            if (normalizePath(file.path()).startsWith("mods/")) {
                restartRequired = true;
            }

            progressConsumer.accept(new SyncProgressInfo(file.path(), SyncStage.VERIFYING, current, total));
        }

        if (config.removeFilesMissingFromManifest()) {
            progressConsumer.accept(new SyncProgressInfo("Removing stale files", SyncStage.CLEANING, total, total));
            removed = removeStaleFiles(expectedPaths);
            if (removed > 0) {
                restartRequired = true;
            }
        }

        return new PackSyncResult(updated, removed, restartRequired);
    }

    public UpdatePlan buildUpdatePlan(PackManifest manifest) throws IOException {
        List<String> toDownload = new ArrayList<>();

        for (PackFileEntry file : manifest.files()) {
            validateRelativePath(file.path());
            Path destination = gameDir.resolve(file.path().replace("/", java.io.File.separator));

            boolean needsDownload = true;
            if (Files.exists(destination) && file.sha256() != null && !file.sha256().isBlank()) {
                String currentHash = sha256(destination);
                needsDownload = !currentHash.equalsIgnoreCase(file.sha256());
            } else if (Files.exists(destination) && (file.sha256() == null || file.sha256().isBlank())) {
                needsDownload = false;
            }

            if (needsDownload) {
                toDownload.add(file.path());
            }
        }

        return new UpdatePlan(toDownload);
    }

    private int removeStaleFiles(Set<String> expectedPaths) throws IOException {
        int removed = 0;
        for (String root : ALLOWED_ROOTS) {
            Path rootPath = gameDir.resolve(root);
            if (!Files.isDirectory(rootPath)) {
                continue;
            }

            List<Path> files = new ArrayList<>();
            try (var stream = Files.walk(rootPath)) {
                stream.filter(Files::isRegularFile).forEach(files::add);
            }

            for (Path file : files) {
                String relative = normalizePath(gameDir.relativize(file).toString());
                if (!expectedPaths.contains(relative)) {
                    Files.deleteIfExists(file);
                    removed++;
                    PackPulseClient.LOGGER.info("Removed stale file {}", relative);
                }
            }
        }
        return removed;
    }

    private void downloadTo(String url, Path destination) throws IOException, InterruptedException {
        Path tempFile = destination.resolveSibling(destination.getFileName() + ".download");
        HttpRequest request = HttpRequest.newBuilder(parseRemoteUri(url, "file url")).GET().build();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Download failed for " + url + " with status " + response.statusCode());
        }

        Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        PackPulseClient.LOGGER.info("Updated {}", destination);
    }

    private static void validateRelativePath(String relativePath) {
        String normalized = normalizePath(relativePath);
        boolean allowed = ALLOWED_ROOTS.stream().anyMatch(root -> normalized.startsWith(root + "/"))
            || normalized.equals("options.txt");
        if (!allowed) {
            throw new IllegalArgumentException("Unsupported path in manifest: " + relativePath);
        }
    }

    private static String normalizePath(String path) {
        return path.replace("\\", "/").replaceAll("^/+", "").toLowerCase(Locale.ROOT);
    }

    private static URI parseRemoteUri(String rawUrl, String label) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("Missing " + label);
        }

        URI uri = URI.create(rawUrl.trim());
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URL must include scheme (http/https): " + rawUrl);
        }

        String normalized = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(normalized) && !"https".equals(normalized)) {
            throw new IllegalArgumentException("Only http/https are supported for " + label + ": " + rawUrl);
        }

        return uri;
    }

    private static String sha256(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IOException("Failed to hash file " + file, ex);
        }
    }
}
