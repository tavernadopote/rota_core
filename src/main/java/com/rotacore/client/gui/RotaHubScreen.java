package com.rotacore.client.gui;

import com.rotacore.RotaCore;
import com.rotacore.api.RotaCoreRegistry;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaTabButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Hub Universal da Rota Mods.
 * Ponto central de gerenciamento, configurações e status de todos os mods da suíte Rota.
 */
public class RotaHubScreen extends Screen {

    private final Screen parentScreen;
    private int selectedTab = 0; // 0 = Visão Geral, 1..N = Módulos

    public RotaHubScreen(Screen parentScreen) {
        super(Component.literal("Rota Mods Hub"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        List<RotaModule> modules = RotaCoreRegistry.getAllModules();

        // 1. Barra de Abas Superior
        int tabHeight = 22;
        int tabY = 38;
        int tabX = 20;
        int tabWidth = 110;

        // Aba 0: Visão Geral
        this.addRenderableWidget(new RotaTabButton(
                tabX, tabY, tabWidth, tabHeight,
                Component.literal("Visão Geral"),
                selectedTab == 0,
                () -> {
                    this.selectedTab = 0;
                    this.rebuildWidgets();
                }
        ));
        tabX += tabWidth + 4;

        // Aba 1: Rota Core (Gerenciador de Integrações e Opções)
        this.addRenderableWidget(new RotaTabButton(
                tabX, tabY, 95, tabHeight,
                Component.literal("⚙ Rota Core"),
                false,
                () -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new RotaCoreModulesScreen(this));
                    }
                }
        ));
        tabX += 95 + 4;

        // Abas dos módulos registrados
        for (int i = 0; i < modules.size(); i++) {
            RotaModule mod = modules.get(i);
            int tabIndex = i + 1;
            this.addRenderableWidget(new RotaTabButton(
                    tabX, tabY, tabWidth + 10, tabHeight,
                    mod.getDisplayName(),
                    selectedTab == tabIndex,
                    () -> {
                        // Abre a tela de configuração específica do módulo
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(mod.createConfigScreen(this));
                        }
                    }
            ));
            tabX += tabWidth + 14;
        }

        // 2. Conteúdo da Aba 0 (Cartões dos Módulos Instalados)
        if (selectedTab == 0) {
            initOverviewTab(modules);
        }

        // 3. Rodapé: Botão Fechar
        int doneWidth = 160;
        int doneX = (this.width - doneWidth) / 2;
        int doneY = this.height - 30;

        this.addRenderableWidget(new RotaButton(doneX, doneY, doneWidth, 22, Component.literal("Fechar"), btn -> {
            onClose();
        }));
    }

    private void initOverviewTab(List<RotaModule> modules) {
        int cardStartY = 70;
        int cardHeight = 64;
        int cardWidth = Math.min(500, this.width - 40);
        int cardX = (this.width - cardWidth) / 2;

        for (int i = 0; i < modules.size(); i++) {
            RotaModule mod = modules.get(i);
            int currentY = cardStartY + i * (cardHeight + 10);

            // Botão "Configurar" dentro do cartão
            int btnW = 110;
            int btnH = 22;
            int btnX = cardX + cardWidth - btnW - 12;
            int btnY = currentY + (cardHeight - btnH) / 2;

            this.addRenderableWidget(new RotaButton(btnX, btnY, btnW, btnH, Component.literal("Configurar"), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(mod.createConfigScreen(this));
                }
            }).setAccent(true));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Cabeçalho Principal
        graphics.drawCenteredString(this.font, "§6§lROTA MODS HUB", this.width / 2, 12, RotaTheme.TEXT_TITLE);
        graphics.drawString(this.font, "§8v1.0.0", this.width - 44, 12, RotaTheme.TEXT_MUTED, false);

        // Linha divisória abaixo das abas
        graphics.fill(20, 61, this.width - 20, 62, RotaTheme.BORDER_SECONDARY);

        // Renderização dos cartões no modo Visão Geral
        if (selectedTab == 0) {
            renderOverviewCards(graphics, mouseX, mouseY);
        }
    }

    private void renderOverviewCards(GuiGraphics graphics, int mouseX, int mouseY) {
        List<RotaModule> modules = RotaCoreRegistry.getAllModules();
        int cardStartY = 70;
        int cardHeight = 64;
        int cardWidth = Math.min(500, this.width - 40);
        int cardX = (this.width - cardWidth) / 2;

        if (modules.isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    "§7Nenhum módulo Rota adicional ativo no momento.",
                    this.width / 2,
                    120,
                    RotaTheme.TEXT_MUTED
            );
            graphics.drawCenteredString(
                    this.font,
                    "§8Instale 'Rota Roads: Shaper' ou outros mods da suíte para gerenciar suas opções.",
                    this.width / 2,
                    136,
                    RotaTheme.TEXT_MUTED
            );
            return;
        }

        for (int i = 0; i < modules.size(); i++) {
            RotaModule mod = modules.get(i);
            int currentY = cardStartY + i * (cardHeight + 10);

            boolean hovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= currentY && mouseY <= currentY + cardHeight;

            // Fundo e borda do cartão
            int bgColor = hovered ? RotaTheme.CARD_HOVER_BG : RotaTheme.CARD_BG;
            graphics.fill(cardX, currentY, cardX + cardWidth, currentY + cardHeight, bgColor);
            graphics.renderOutline(cardX, currentY, cardWidth, cardHeight, hovered ? RotaTheme.BORDER_ACCENT : RotaTheme.BORDER_SECONDARY);

            // Informações do Módulo
            graphics.drawString(this.font, mod.getDisplayName(), cardX + 12, currentY + 10, RotaTheme.TEXT_TITLE, false);
            graphics.drawString(this.font, Component.translatable("gui.rota_core.module_version", mod.getVersion()), cardX + 12, currentY + 24, RotaTheme.TEXT_MUTED, false);

            // Descrição (com limite de largura para não sobrepor o botão)
            int maxDescWidth = cardWidth - 145;
            String descStr = mod.getDescription().getString();
            if (this.font.width(descStr) > maxDescWidth) {
                descStr = this.font.plainSubstrByWidth(descStr, maxDescWidth - 8) + "...";
            }
            graphics.drawString(this.font, descStr, cardX + 12, currentY + 40, RotaTheme.TEXT_BODY, false);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
}
