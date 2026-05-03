package ru.fwx.packpulse.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public final class UpdateConfirmScreen extends Screen {
    private static final int PANEL_BACKGROUND = 0xE60B111C;
    private static final int PANEL_BORDER = 0xFF3A8DFF;
    private static final int PANEL_ACCENT = 0xFF66CC99;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFC8D5E8;
    private static final int ROW_HEIGHT = 18;

    private final List<String> files;
    private final Set<String> selectedFiles;
    private final Consumer<List<String>> decisionConsumer;
    private Button selectedButton;
    private int scrollOffset;
    private boolean completed;

    public UpdateConfirmScreen(List<String> files, Consumer<List<String>> decisionConsumer) {
        super(Component.literal("PackPulseMod"));
        this.files = List.copyOf(files);
        this.selectedFiles = new LinkedHashSet<>(files);
        this.decisionConsumer = decisionConsumer;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(430, this.width - 32);
        int panelX = (this.width - panelWidth) / 2;
        int buttonY = Math.min(this.height - 32, this.panelBottom() - 34);
        int buttonWidth = Math.min(126, (panelWidth - 44) / 3);

        this.selectedButton = this.addRenderableWidget(
            Button.builder(Component.literal("Ð¡ÐºÐ°Ñ‡Ð°Ñ‚ÑŒ"), button -> this.complete(true))
                .bounds(panelX + 14, buttonY, buttonWidth, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal("Ð¡ÐºÐ°Ñ‡Ð°Ñ‚ÑŒ Ð²ÑÑ‘"), button -> this.completeAll())
                .bounds(panelX + 20 + buttonWidth, buttonY, buttonWidth, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal("ÐžÑ‚Ð¼ÐµÐ½Ð°"), button -> this.complete(false))
                .bounds(panelX + panelWidth - buttonWidth - 14, buttonY, buttonWidth, 20)
                .build()
        );
        this.updateSelectedButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF07111F, 0xFF101827);

        int panelWidth = Math.min(430, this.width - 32);
        int panelX = (this.width - panelWidth) / 2;
        int panelTop = this.panelTop();
        int panelBottom = this.panelBottom();

        graphics.fill(panelX, panelTop, panelX + panelWidth, panelBottom, PANEL_BACKGROUND);
        graphics.renderOutline(panelX, panelTop, panelWidth, panelBottom - panelTop, PANEL_BORDER);
        graphics.fill(panelX, panelTop, panelX + panelWidth, panelTop + 2, PANEL_ACCENT);

        graphics.drawCenteredString(this.font, Component.literal("PackPulseMod"), this.width / 2, panelTop + 14, TEXT_PRIMARY);
        graphics.drawCenteredString(
            this.font,
            Component.literal("Ð’Ñ‹Ð±Ñ€Ð°Ð½Ð¾: " + this.selectedFiles.size() + " Ð¸Ð· " + this.files.size()),
            this.width / 2,
            panelTop + 30,
            TEXT_SECONDARY
        );

        this.renderFileList(graphics, mouseX, mouseY, panelX + 14, this.listTop(), panelWidth - 28, this.listHeight());

        for (var child : this.children()) {
            if (child instanceof Renderable renderable) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public void onClose() {
        this.complete(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isInsideList(mouseX, mouseY)) {
            int row = ((int) mouseY - this.listTop() - 7 + this.scrollOffset) / ROW_HEIGHT;
            if (row >= 0 && row < this.files.size()) {
                this.toggleFile(this.files.get(row));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.scrollList(mouseX, mouseY, delta);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.scrollList(mouseX, mouseY, scrollY);
    }

    private boolean scrollList(double mouseX, double mouseY, double delta) {
        if (!this.isInsideList(mouseX, mouseY)) {
            return false;
        }

        int maxScroll = Math.max(0, this.files.size() * ROW_HEIGHT + 14 - this.listHeight());
        this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int) Math.signum(delta) * ROW_HEIGHT));
        return true;
    }

    private void renderFileList(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xB80F1724);
        graphics.renderOutline(x, y, width, height, 0xFF24344A);

        if (this.files.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal("ÐÐµÑ‚ Ñ„Ð°Ð¹Ð»Ð¾Ð² Ð´Ð»Ñ ÑƒÑÑ‚Ð°Ð½Ð¾Ð²ÐºÐ¸"), x + width / 2, y + 14, TEXT_SECONDARY);
            return;
        }

        int textWidth = width - 44;
        for (int index = 0; index < this.files.size(); index++) {
            int rowY = y + 7 + index * ROW_HEIGHT - this.scrollOffset;
            if (rowY < y + 4 || rowY + ROW_HEIGHT > y + height - 2) {
                continue;
            }

            String path = this.files.get(index);
            boolean selected = this.selectedFiles.contains(path);
            boolean hovered = mouseX >= x && mouseX <= x + width - 8 && mouseY >= rowY - 3 && mouseY <= rowY + ROW_HEIGHT - 3;

            if (hovered) {
                graphics.fill(x + 4, rowY - 3, x + width - 12, rowY + ROW_HEIGHT - 3, 0x553A8DFF);
            }

            int boxColor = selected ? PANEL_ACCENT : 0xFF6B7B91;
            graphics.renderOutline(x + 8, rowY, 10, 10, boxColor);
            if (selected) {
                graphics.fill(x + 11, rowY + 3, x + 16, rowY + 8, PANEL_ACCENT);
            }

            graphics.drawString(this.font, trimToWidth(displayName(path), textWidth), x + 26, rowY, selected ? TEXT_PRIMARY : 0xFF91A0B6);
        }

        this.renderScrollbar(graphics, x, y, width, height);
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int y, int width, int height) {
        int contentHeight = this.files.size() * ROW_HEIGHT + 14;
        if (contentHeight <= height) {
            return;
        }

        int trackX = x + width - 6;
        int thumbHeight = Math.max(18, height * height / contentHeight);
        int maxScroll = contentHeight - height;
        int thumbY = y + (height - thumbHeight) * this.scrollOffset / Math.max(1, maxScroll);
        graphics.fill(trackX, y + 3, trackX + 2, y + height - 3, 0x663A4658);
        graphics.fill(trackX - 1, thumbY, trackX + 3, thumbY + thumbHeight, PANEL_ACCENT);
    }

    private boolean isInsideList(double mouseX, double mouseY) {
        int panelWidth = Math.min(430, this.width - 32);
        int panelX = (this.width - panelWidth) / 2;
        int listX = panelX + 14;
        int listY = this.listTop();
        return mouseX >= listX && mouseX <= listX + panelWidth - 28 && mouseY >= listY && mouseY <= listY + this.listHeight();
    }

    private void complete(boolean accepted) {
        this.completeSelection(accepted ? new ArrayList<>(this.selectedFiles) : List.of());
    }

    private void completeAll() {
        this.completeSelection(this.files);
    }

    private void completeSelection(List<String> selectedPaths) {
        if (this.completed) {
            return;
        }

        this.completed = true;
        this.decisionConsumer.accept(List.copyOf(selectedPaths));
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void toggleFile(String path) {
        if (!this.selectedFiles.remove(path)) {
            this.selectedFiles.add(path);
        }
        this.updateSelectedButton();
    }

    private void updateSelectedButton() {
        if (this.selectedButton == null) {
            return;
        }
        int selectedCount = this.selectedFiles.size();
        this.selectedButton.active = selectedCount > 0;
        this.selectedButton.setMessage(Component.literal("Ð¡ÐºÐ°Ñ‡Ð°Ñ‚ÑŒ (" + selectedCount + ")"));
    }

    private int panelTop() {
        return Math.max(18, (this.height - Math.min(238, this.height - 28)) / 2);
    }

    private int panelBottom() {
        return Math.min(this.height - 14, this.panelTop() + Math.min(238, this.height - 28));
    }

    private int listTop() {
        return this.panelTop() + 50;
    }

    private int listHeight() {
        return Math.max(44, this.panelBottom() - this.listTop() - 44);
    }

    private String trimToWidth(String value, int width) {
        if (this.font.width(value) <= width) {
            return value;
        }
        return this.font.plainSubstrByWidth(value, Math.max(8, width - this.font.width("..."))) + "...";
    }

    private static String displayName(String path) {
        String normalized = path.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String name = slashIndex >= 0 && slashIndex < normalized.length() - 1
            ? normalized.substring(slashIndex + 1)
            : normalized;

        String lowerName = name.toLowerCase(Locale.ROOT);
        for (String extension : extensions()) {
            if (lowerName.endsWith(extension)) {
                return name.substring(0, name.length() - extension.length());
            }
        }
        return name;
    }

    private static List<String> extensions() {
        List<String> extensions = new ArrayList<>();
        extensions.add(".jar");
        extensions.add(".zip");
        extensions.add(".json");
        extensions.add(".toml");
        extensions.add(".txt");
        extensions.add(".cfg");
        extensions.add(".properties");
        extensions.add(".yml");
        extensions.add(".yaml");
        return extensions;
    }
}

