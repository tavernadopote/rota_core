/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui;

import com.rotacore.api.RotaCoreRegistry;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.RotaCoreModulesScreen;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaTabButton;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RotaHubScreen
extends Screen {
    private final Screen parentScreen;
    private int selectedTab = 0;

    public RotaHubScreen(Screen parentScreen) {
        super((Component)Component.literal((String)"Rota Mods Hub"));
        this.parentScreen = parentScreen;
    }

    protected void init() {
        List<RotaModule> modules = RotaCoreRegistry.getAllModules();
        int tabHeight = 22;
        int tabY = 38;
        int tabX = 20;
        int tabWidth = 110;
        this.addRenderableWidget((GuiEventListener)new RotaTabButton(tabX, tabY, tabWidth, tabHeight, (Component)Component.literal((String)"Vis\u00e3o Geral"), this.selectedTab == 0, () -> {
            this.selectedTab = 0;
            this.rebuildWidgets();
        }));
        this.addRenderableWidget((GuiEventListener)new RotaTabButton(tabX += tabWidth + 4, tabY, 95, tabHeight, (Component)Component.literal((String)"Rota Core"), false, () -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen((Screen)new RotaCoreModulesScreen(this));
            }
        }));
        tabX += 99;
        for (int i = 0; i < modules.size(); ++i) {
            RotaModule mod = modules.get(i);
            int tabIndex = i + 1;
            this.addRenderableWidget((GuiEventListener)new RotaTabButton(tabX, tabY, tabWidth + 10, tabHeight, mod.getDisplayName(), this.selectedTab == tabIndex, () -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(mod.createConfigScreen(this));
                }
            }));
            tabX += tabWidth + 14;
        }
        if (this.selectedTab == 0) {
            this.initOverviewTab(modules);
        }
        int doneWidth = 160;
        int doneX = (this.width - doneWidth) / 2;
        int doneY = this.height - 30;
        this.addRenderableWidget((GuiEventListener)new RotaButton(doneX, doneY, doneWidth, 22, (Component)Component.literal((String)"Fechar"), btn -> this.onClose()));
    }

    private void initOverviewTab(List<RotaModule> modules) {
        int cardStartY = 70;
        int cardHeight = 64;
        int cardWidth = Math.min(500, this.width - 40);
        int cardX = (this.width - cardWidth) / 2;
        for (int i = 0; i < modules.size(); ++i) {
            RotaModule mod = modules.get(i);
            int currentY = cardStartY + i * (cardHeight + 10);
            int btnW = 110;
            int btnH = 22;
            int btnX = cardX + cardWidth - btnW - 12;
            int btnY = currentY + (cardHeight - btnH) / 2;
            this.addRenderableWidget((GuiEventListener)new RotaButton(btnX, btnY, btnW, btnH, (Component)Component.literal((String)"Configurar"), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(mod.createConfigScreen(this));
                }
            }).setAccent(true));
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, "\u00a76\u00a7lROTA MODS HUB", this.width / 2, 12, -22016);
        graphics.drawString(this.font, "\u00a78v1.0.0", this.width - 44, 12, -6246720, false);
        graphics.fill(20, 61, this.width - 20, 62, -11905688);
        if (this.selectedTab == 0) {
            this.renderOverviewCards(graphics, mouseX, mouseY);
        }
    }

    private void renderOverviewCards(GuiGraphics graphics, int mouseX, int mouseY) {
        List<RotaModule> modules = RotaCoreRegistry.getAllModules();
        int cardStartY = 70;
        int cardHeight = 64;
        int cardWidth = Math.min(500, this.width - 40);
        int cardX = (this.width - cardWidth) / 2;
        if (modules.isEmpty()) {
            graphics.drawCenteredString(this.font, "\u00a77Nenhum m\u00f3dulo Rota adicional ativo no momento.", this.width / 2, 120, -6246720);
            graphics.drawCenteredString(this.font, "\u00a78Instale 'Rota Roads: Shaper' ou outros mods da su\u00edte para gerenciar suas op\u00e7\u00f5es.", this.width / 2, 136, -6246720);
            return;
        }
        for (int i = 0; i < modules.size(); ++i) {
            RotaModule mod = modules.get(i);
            int currentY = cardStartY + i * (cardHeight + 10);
            boolean hovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= currentY && mouseY <= currentY + cardHeight;
            int bgColor = hovered ? -299225286 : -585161941;
            graphics.fill(cardX, currentY, cardX + cardWidth, currentY + cardHeight, bgColor);
            graphics.renderOutline(cardX, currentY, cardWidth, cardHeight, hovered ? -22016 : -11905688);
            graphics.drawString(this.font, mod.getDisplayName(), cardX + 12, currentY + 10, -22016, false);
            graphics.drawString(this.font, (Component)Component.translatable((String)"gui.rota_core.module_version", (Object[])new Object[]{mod.getVersion()}), cardX + 12, currentY + 24, -6246720, false);
            int maxDescWidth = cardWidth - 145;
            Object descStr = mod.getDescription().getString();
            if (this.font.width((String)descStr) > maxDescWidth) {
                descStr = this.font.plainSubstrByWidth((String)descStr, maxDescWidth - 8) + "...";
            }
            graphics.drawString(this.font, (String)descStr, cardX + 12, currentY + 40, -1906448, false);
        }
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
}

