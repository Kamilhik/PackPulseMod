package ru.fwx.freewebupdater.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.fwx.freewebupdater.sync.SyncProgressInfo;
import ru.fwx.freewebupdater.sync.SyncStage;

public final class SyncProgressScreen extends Screen {
    private SyncProgressInfo current = new SyncProgressInfo(
        "Preparing...",
        SyncStage.CHECKING,
        0,
        1
    );

    public SyncProgressScreen() {
        super(Text.literal("PackPulse Update"));
    }

    public void updateProgress(SyncProgressInfo info) {
        this.current = info;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF0F1724);
        int centerX = width / 2;
        int top = height / 3;

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Updating pack"),
            centerX,
            top,
            0xFFFFFF
        );
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal(stageText(current.stage()) + " " + current.currentFile() + "/" + current.totalFiles()),
            centerX,
            top + 22,
            0xA7C7E7
        );
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("File: " + displayName(current.filePath())),
            centerX,
            top + 44,
            0xDCEAF8
        );

        int barWidth = 360;
        int barHeight = 14;
        int barX = centerX - barWidth / 2;
        int barY = top + 72;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1C2A3A);
        int progressWidth = (int) ((Math.min(current.currentFile(), Math.max(1, current.totalFiles())) / (float) Math.max(1, current.totalFiles())) * barWidth);
        context.fill(barX, barY, barX + progressWidth, barY + barHeight, 0xFF66CC99);

        super.render(context, mouseX, mouseY, delta);
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
