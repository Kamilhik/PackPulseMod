package ru.fwx.packpulse.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.fwx.packpulse.sync.SyncProgressInfo;
import ru.fwx.packpulse.sync.SyncStage;

public final class SyncProgressScreen extends Screen {
    private SyncProgressInfo current = new SyncProgressInfo(
        "Preparing...",
        SyncStage.CHECKING,
        0,
        1
    );

    public SyncProgressScreen() {
        super(Component.literal("PackPulseMod Update"));
    }

    public void updateProgress(SyncProgressInfo info) {
        this.current = info;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF0F1724);
        int centerX = width / 2;
        int top = height / 3;

        graphics.drawCenteredString(
            font,
            Component.literal("Updating pack"),
            centerX,
            top,
            0xFFFFFF
        );
        graphics.drawCenteredString(
            font,
            Component.literal(stageText(current.stage()) + " " + current.currentFile() + "/" + current.totalFiles()),
            centerX,
            top + 22,
            0xA7C7E7
        );
        graphics.drawCenteredString(
            font,
            Component.literal("File: " + displayName(current.filePath())),
            centerX,
            top + 44,
            0xDCEAF8
        );

        int barWidth = Math.min(360, width - 40);
        int barHeight = 14;
        int barX = centerX - barWidth / 2;
        int barY = top + 72;

        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1C2A3A);
        int progressWidth = (int) ((Math.min(current.currentFile(), Math.max(1, current.totalFiles())) / (float) Math.max(1, current.totalFiles())) * barWidth);
        graphics.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF66CC99);
    }

    private static String stageText(SyncStage stage) {
        return switch (stage) {
            case CHECKING -> "Checking";
            case DOWNLOADING -> "Downloading";
            case VERIFYING -> "Verifying";
            case UPDATED -> "Updated";
            case UP_TO_DATE -> "Up to date";
            case CLEANING -> "Cleaning";
        };
    }

    private static String displayName(String path) {
        String normalized = path.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < normalized.length() - 1) {
            return normalized.substring(slashIndex + 1);
        }
        return normalized;
    }
}

