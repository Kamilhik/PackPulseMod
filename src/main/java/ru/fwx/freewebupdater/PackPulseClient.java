package ru.fwx.freewebupdater;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fwx.freewebupdater.config.UpdaterConfig;
import ru.fwx.freewebupdater.sync.PackManifest;
import ru.fwx.freewebupdater.sync.PackSyncResult;
import ru.fwx.freewebupdater.sync.PackSyncService;
import ru.fwx.freewebupdater.sync.SyncProgressInfo;
import ru.fwx.freewebupdater.sync.UpdatePlan;
import ru.fwx.freewebupdater.ui.SyncProgressScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PackPulseClient implements ClientModInitializer {
    public static final String MOD_ID = "packpulse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final int MAX_ITEMS_PER_CATEGORY = 6;

    @Override
    public void onInitializeClient() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        UpdaterConfig config = UpdaterConfig.load(gameDir);

        LOGGER.info("PackPulse initialized. Manifest: {}", config.manifestUrl());
        if (!config.updateOnStartup()) {
            LOGGER.info("Startup sync is disabled in the config.");
            return;
        }

        CompletableFuture.runAsync(() -> runSync(gameDir, config));
    }

    private void runSync(Path gameDir, UpdaterConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        SyncProgressScreen progressScreen = null;

        try {
            PackSyncService syncService = new PackSyncService(gameDir, config);
            PackManifest manifest = syncService.loadManifest();
            UpdatePlan plan = syncService.buildUpdatePlan(manifest);

            if (!plan.isEmpty()) {
                boolean accepted = promptDownloadInGame(plan);
                if (!accepted) {
                    notifyPlayer(
                        Text.literal("Update canceled"),
                        Text.literal("You canceled the download.")
                    );
                    return;
                }
            }

            if (config.showProgressWindow()) {
                progressScreen = new SyncProgressScreen();
                SyncProgressScreen finalProgressScreen = progressScreen;
                if (client != null) {
                    client.execute(() -> client.setScreen(finalProgressScreen));
                }
            }

            SyncProgressScreen finalProgressScreenForCallback = progressScreen;
            PackSyncResult result = syncService.sync(manifest, info -> reportProgress(client, finalProgressScreenForCallback, info));

            closeProgressScreen(client, progressScreen);

            if (result.updatedFiles() == 0 && result.removedFiles() == 0) {
                notifyPlayer(
                    Text.literal("Pack is up to date"),
                    Text.literal("No new files were required.")
                );
                return;
            }

            if (result.restartRequired()) {
                handleRestart(result);
            } else {
                notifyPlayer(
                    Text.literal("Pack updated"),
                    Text.literal("Updated files: " + result.updatedFiles() + ". No restart is required.")
                );
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to sync pack", ex);
            closeProgressScreen(client, progressScreen);
            notifyPlayer(
                Text.literal("Update error"),
                Text.literal(String.valueOf(ex.getMessage()))
            );
        }
    }

    private boolean promptDownloadInGame(UpdatePlan plan) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return true;
        }

        CompletableFuture<Boolean> decision = new CompletableFuture<>();

        client.execute(() -> {
            String message = buildPlanMessage(plan);
            ConfirmScreen screen = new ConfirmScreen(
                accepted -> {
                    decision.complete(accepted);
                    client.setScreen(null);
                },
                Text.literal("PackPulse update (" + plan.filesToDownload().size() + " files)"),
                Text.literal(message),
                Text.literal("Download"),
                Text.literal("Cancel")
            );
            client.setScreen(screen);
        });

        try {
            return decision.get(30, TimeUnit.MINUTES);
        } catch (Exception ex) {
            LOGGER.error("Failed to get update confirmation", ex);
            return false;
        }
    }

    private String buildPlanMessage(UpdatePlan plan) {
        Map<PlanCategory, List<String>> grouped = groupByCategory(plan.filesToDownload());
        StringBuilder builder = new StringBuilder();
        builder.append("Need to download ").append(plan.filesToDownload().size()).append(" files.").append('\n');

        for (PlanCategory category : PlanCategory.values()) {
            List<String> files = grouped.get(category);
            if (files == null || files.isEmpty()) {
                continue;
            }

            builder.append('\n')
                .append(category.title())
                .append(" (")
                .append(files.size())
                .append(")")
                .append('\n');

            int show = Math.min(MAX_ITEMS_PER_CATEGORY, files.size());
            for (int i = 0; i < show; i++) {
                builder.append(" - ").append(files.get(i)).append('\n');
            }

            if (files.size() > show) {
                builder.append("   ")
                    .append("... and ")
                    .append(files.size() - show)
                    .append(" more")
                    .append('\n');
            }
        }

        builder.append('\n').append("Start download?");
        return builder.toString();
    }

    private Map<PlanCategory, List<String>> groupByCategory(List<String> files) {
        Map<PlanCategory, List<String>> grouped = new EnumMap<>(PlanCategory.class);
        for (PlanCategory category : PlanCategory.values()) {
            grouped.put(category, new ArrayList<>());
        }

        for (String path : files) {
            grouped.get(resolveCategory(path)).add(displayName(path));
        }

        return grouped;
    }

    private PlanCategory resolveCategory(String path) {
        String normalized = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (normalized.startsWith("mods/")) {
            return PlanCategory.MODS;
        }
        if (normalized.startsWith("resourcepacks/")) {
            return PlanCategory.RESOURCEPACKS;
        }
        if (normalized.startsWith("shaderpacks/")) {
            return PlanCategory.SHADERPACKS;
        }
        if (normalized.startsWith("config/")) {
            return PlanCategory.CONFIG;
        }
        return PlanCategory.OTHER;
    }

    private String displayName(String path) {
        String normalized = path.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < normalized.length() - 1) {
            return normalized.substring(slashIndex + 1);
        }
        return normalized;
    }

    private void reportProgress(MinecraftClient client, SyncProgressScreen progressScreen, SyncProgressInfo info) {
        LOGGER.info("{} {}", info.stage(), info.filePath());
        if (client != null && progressScreen != null) {
            client.execute(() -> progressScreen.updateProgress(info));
        }
    }

    private void closeProgressScreen(MinecraftClient client, SyncProgressScreen progressScreen) {
        if (client == null || progressScreen == null) {
            return;
        }
        client.execute(() -> {
            if (client.currentScreen == progressScreen) {
                client.setScreen(null);
            }
        });
    }

    private void handleRestart(PackSyncResult result) {
        notifyPlayer(
            Text.literal("Pack updated"),
            Text.literal("Updated files: " + result.updatedFiles() + ". Minecraft will close to apply mod updates.")
        );

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(client::scheduleStop);
        } else {
            notifyPlayer(
                Text.literal("Restart required"),
                Text.literal("Close Minecraft manually to apply mod updates.")
            );
        }
    }

    private void notifyPlayer(Text title, Text message) {
        LOGGER.info("{}: {}", title.getString(), message.getString());

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        client.execute(() -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("[PackPulse] ").append(title).append(Text.literal(": ")).append(message), false);
            }
        });
    }

    private enum PlanCategory {
        MODS("Mods"),
        RESOURCEPACKS("Resource Packs"),
        SHADERPACKS("Shader Packs"),
        CONFIG("Config"),
        OTHER("Other");

        private final String title;

        PlanCategory(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }
}
