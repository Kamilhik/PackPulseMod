package ru.fwx.packpulse.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RestartRequiredScreen extends Screen {
    private final int updatedFiles;

    public RestartRequiredScreen(int updatedFiles) {
        super(Component.literal("PackPulseMod"));
        this.updatedFiles = updatedFiles;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(170, this.width - 40);
        int centerX = this.width / 2;
        int buttonY = this.height / 2 + 42;

        this.addRenderableWidget(
            Button.builder(Component.literal("Закрыть игру"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.stop();
                    }
                })
                .bounds(centerX - buttonWidth - 6, buttonY, buttonWidth, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Перезапущу позже"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(null);
                    }
                })
                .bounds(centerX + 6, buttonY, buttonWidth, 20)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF07111F, 0xFF101827);

        int panelWidth = Math.min(440, this.width - 32);
        int panelHeight = 150;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE60B111C);
        renderBorder(graphics, panelX, panelY, panelWidth, panelHeight, 0xFF3A8DFF);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF66CC99);

        graphics.drawCenteredString(this.font, Component.literal("Моды обновлены"), this.width / 2, panelY + 18, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Обновлено файлов: " + this.updatedFiles), this.width / 2, panelY + 42, 0xFFC8D5E8);
        graphics.drawCenteredString(this.font, Component.literal("Чтобы моды применились, нужно перезапустить игру."), this.width / 2, panelY + 64, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static void renderBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
