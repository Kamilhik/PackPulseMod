package ru.fwx.packpulse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fwx.packpulse.config.UpdaterConfig;
import ru.fwx.packpulse.sync.PackManifest;
import ru.fwx.packpulse.sync.PackSyncResult;
import ru.fwx.packpulse.sync.PackSyncService;
import ru.fwx.packpulse.sync.SyncProgressInfo;
import ru.fwx.packpulse.sync.UpdatePlan;
import ru.fwx.packpulse.ui.RestartRequiredScreen;
import ru.fwx.packpulse.ui.SyncProgressScreen;
import ru.fwx.packpulse.ui.UpdateConfirmScreen;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PackPulseRuntime {
    public static final String MOD_ID = "packpulse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final long CLIENT_READY_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private PackPulseRuntime() {
    }

    public static void start(Path gameDir) {
        UpdaterConfig config = UpdaterConfig.load(gameDir);
        LOGGER.info("PackPulseMod initialized. Manifest: {}", config.manifestUrl());

        if (!config.updateOnStartup()) {
            LOGGER.info("Startup sync is disabled in the config.");
            return;
        }

        CompletableFuture.runAsync(() -> runSync(gameDir, config));
    }

    private static void runSync(Path gameDir, UpdaterConfig config) {
        Minecraft client = Minecraft.getInstance();
        SyncProgressScreen progressScreen = null;

        try {
            waitForClientReady();

            PackSyncService syncService = new PackSyncService(gameDir, config);
            PackManifest manifest = syncService.loadManifest();
            UpdatePlan plan = syncService.buildUpdatePlan(manifest);
            LOGGER.info("Manifest loaded: {} files, {} files require download.", manifest.files().size(), plan.filesToDownload().size());
            Set<String> selectedFilePaths = null;

            if (!plan.isEmpty()) {
                List<String> selectedFiles = promptDownloadInGame(plan);
                if (selectedFiles.isEmpty()) {
                    notifyPlayer(
                        Component.literal("Update canceled"),
                        Component.literal("You canceled the download.")
                    );
                    return;
                }
                selectedFilePaths = new HashSet<>(selectedFiles);
            }

            if (config.showProgressWindow()) {
                progressScreen = new SyncProgressScreen();
                SyncProgressScreen finalProgressScreen = progressScreen;
                if (client != null) {
                    client.execute(() -> client.setScreen(finalProgressScreen));
                }
            }

            SyncProgressScreen finalProgressScreenForCallback = progressScreen;
            PackSyncResult result = selectedFilePaths == null
                ? syncService.sync(manifest, info -> reportProgress(client, finalProgressScreenForCallback, info))
                : syncService.sync(manifest, selectedFilePaths, info -> reportProgress(client, finalProgressScreenForCallback, info));

            closeProgressScreen(client, progressScreen);

            if (result.updatedFiles() == 0 && result.removedFiles() == 0) {
                notifyPlayer(
                    Component.literal("Pack is up to date"),
                    Component.literal("No new files were required.")
                );
                return;
            }

            if (result.restartRequired()) {
                handleRestart(result);
            } else {
                notifyPlayer(
                    Component.literal("Pack updated"),
                    Component.literal("Updated files: " + result.updatedFiles() + ". No restart is required.")
                );
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to sync pack", ex);
            closeProgressScreen(client, progressScreen);
            notifyPlayer(
                Component.literal("Update error"),
                Component.literal(String.valueOf(ex.getMessage()))
            );
        }
    }

    private static void waitForClientReady() throws InterruptedException {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }

        long deadline = System.currentTimeMillis() + CLIENT_READY_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            if (client.screen instanceof TitleScreen || client.player != null) {
                LOGGER.info("Minecraft client is ready, starting pack sync.");
                return;
            }

            Thread.sleep(250L);
        }

        LOGGER.warn("Minecraft client readiness timeout reached, starting pack sync anyway.");
    }

    private static List<String> promptDownloadInGame(UpdatePlan plan) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return plan.filesToDownload();
        }

        CompletableFuture<List<String>> decision = new CompletableFuture<>();

        client.execute(() -> client.setScreen(new UpdateConfirmScreen(plan.filesToDownload(), decision::complete)));

        try {
            return decision.get(30, TimeUnit.MINUTES);
        } catch (Exception ex) {
            LOGGER.error("Failed to get update confirmation", ex);
            return List.of();
        }
    }

    private static void reportProgress(Minecraft client, SyncProgressScreen progressScreen, SyncProgressInfo info) {
        LOGGER.info("{} {}", info.stage(), info.filePath());
        if (client != null && progressScreen != null) {
            client.execute(() -> progressScreen.updateProgress(info));
        }
    }

    private static void closeProgressScreen(Minecraft client, SyncProgressScreen progressScreen) {
        if (client == null || progressScreen == null) {
            return;
        }
        client.execute(() -> {
            if (client.screen == progressScreen) {
                client.setScreen(null);
            }
        });
    }

    private static void handleRestart(PackSyncResult result) {
        notifyPlayer(
            Component.literal("Pack updated"),
            Component.literal("Updated files: " + result.updatedFiles() + ". Restart Minecraft to apply mod updates.")
        );

        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.execute(() -> client.setScreen(new RestartRequiredScreen(result.updatedFiles())));
        } else {
            notifyPlayer(
                Component.literal("Restart required"),
                Component.literal("Close Minecraft manually to apply mod updates.")
            );
        }
    }

    private static void notifyPlayer(Component title, Component message) {
        LOGGER.info("{}: {}", title.getString(), message.getString());

        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }

        client.execute(() -> {
            if (client.player != null) {
                client.player.displayClientMessage(
                    Component.literal("[PackPulseMod] ").append(title).append(Component.literal(": ")).append(message),
                    false
                );
            }
        });
    }
}

