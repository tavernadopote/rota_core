package com.rotacore.client.gui;

import com.rotacore.api.RotaCategory;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.client.gui.widget.RotaButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Tela de categorias de um módulo Rota específico (ex: Rota Roads: Shaper).
 * Exibe botões em grade de 2 colunas com resumo dinâmico sob o cursor.
 */
public class RotaModuleCategoriesScreen extends Screen {

    private final Screen parentScreen;
    private final RotaModule module;
    private final List<RotaCategory> categories;
    private final Runnable onSaveCallback;
    private String hoveredDescription = "";

    public RotaModuleCategoriesScreen(Screen parentScreen, RotaModule module, List<RotaCategory> categories, Runnable onSaveCallback) {
        super(module.getDisplayName());
        this.parentScreen = parentScreen;
        this.module = module;
        this.categories = categories != null ? categories : List.of();
        this.onSaveCallback = onSaveCallback;
    }

    @Override
    protected void init() {
        int btnWidth = 210;
        int btnHeight = 24;
        int gapX = 14;
        int gapY = 8;

        int totalGridWidth = (btnWidth * 2) + gapX;
        int startX = (this.width - totalGridWidth) / 2;
        int startY = Math.max(50, this.height / 2 - 80);

        for (int i = 0; i < categories.size(); i++) {
            RotaCategory cat = categories.get(i);
            int col = i % 2;
            int row = i / 2;

            int x = startX + col * (btnWidth + gapX);
            int y = startY + row * (btnHeight + gapY);

            RotaButton catButton = new RotaButton(x, y, btnWidth, btnHeight, cat.getTitle(), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(cat.createScreen(this, onSaveCallback));
                }
            });

            this.addRenderableWidget(catButton);
        }

        // Botão "Concluído" no Rodapé
        int doneWidth = 200;
        int doneX = (this.width - doneWidth) / 2;
        int doneY = this.height - 34;

        this.addRenderableWidget(
                new RotaButton(doneX, doneY, doneWidth, 22, Component.literal("Concluído"), btn -> {
                    saveAndClose();
                }).setAccent(true)
        );
    }

    private void saveAndClose() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título e Subtítulo
        graphics.drawCenteredString(this.font, module.getDisplayName(), this.width / 2, 16, RotaTheme.TEXT_TITLE);
        graphics.drawCenteredString(this.font, Component.translatable("gui.rota_core.module_version", module.getVersion()), this.width / 2, 29, RotaTheme.TEXT_MUTED);

        // Atualiza a legenda inferior com base no botão sob o cursor
        detectHoveredDescription(mouseX, mouseY);

        // Barra inferior de status com a descrição da categoria focada
        if (!hoveredDescription.isEmpty()) {
            int barY = this.height - 56;
            graphics.fill(this.width / 2 - 250, barY - 2, this.width / 2 + 250, barY + 12, 0x88000000);
            graphics.drawCenteredString(this.font, "§e" + hoveredDescription, this.width / 2, barY, 0xFFFFFF);
        }
    }

    private void detectHoveredDescription(int mouseX, int mouseY) {
        int btnWidth = 210;
        int btnHeight = 24;
        int gapX = 14;
        int gapY = 8;
        int totalGridWidth = (btnWidth * 2) + gapX;
        int startX = (this.width - totalGridWidth) / 2;
        int startY = Math.max(50, this.height / 2 - 80);

        for (int i = 0; i < categories.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (btnWidth + gapX);
            int y = startY + row * (btnHeight + gapY);

            if (mouseX >= x && mouseX <= x + btnWidth && mouseY >= y && mouseY <= y + btnHeight) {
                this.hoveredDescription = categories.get(i).getDescription();
                return;
            }
        }
        this.hoveredDescription = "";
    }

    @Override
    public void onClose() {
        saveAndClose();
    }
}
