/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.rotacore.client.gui;

import com.rotacore.api.RotaCategory;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.widget.RotaButton;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class RotaModuleCategoriesScreen
extends Screen {
    private final Screen parentScreen;
    private final RotaModule module;
    private final List<RotaCategory> categories;
    private final Runnable onSaveCallback;
    private String hoveredDescription = "";
    public static final int BTN_WIDTH = 210;
    public static final int BTN_HEIGHT = 22;
    public static final int GAP_X = 14;
    public static final int GAP_Y = 6;

    public RotaModuleCategoriesScreen(Screen parentScreen, RotaModule module, List<RotaCategory> categories, Runnable onSaveCallback) {
        super(module.getDisplayName());
        this.parentScreen = parentScreen;
        this.module = module;
        this.categories = categories != null ? categories : List.of();
        this.onSaveCallback = onSaveCallback;
    }

    public static int calculateGridStartY(int screenHeight) {
        return Math.max(46, (screenHeight - 34 - 78) / 2 - 10);
    }

    public static int calculateDescriptionBoxY(int screenHeight, int boxHeight, int numCategories) {
        int availableSpace;
        int numRows;
        int doneY = screenHeight - 34;
        int boxY = doneY - boxHeight - 6;
        int startY = RotaModuleCategoriesScreen.calculateGridStartY(screenHeight);
        int gridBottom = startY + (numRows = (numCategories + 1) / 2) * 22 + Math.max(0, numRows - 1) * 6;
        if (boxY < gridBottom + 4) {
            boxY = gridBottom + 4;
        }
        if ((availableSpace = doneY - gridBottom) > boxHeight && boxY + boxHeight > doneY - 2) {
            boxY = gridBottom + (availableSpace - boxHeight) / 2;
        }
        return boxY;
    }

    protected void init() {
        int totalGridWidth = 434;
        int startX = (this.width - totalGridWidth) / 2;
        int startY = RotaModuleCategoriesScreen.calculateGridStartY(this.height);
        for (int i = 0; i < this.categories.size(); ++i) {
            RotaCategory cat = this.categories.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * 224;
            int y = startY + row * 28;
            RotaButton catButton = new RotaButton(x, y, 210, 22, cat.getTitle(), btn -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(cat.createScreen(this, this.onSaveCallback));
                }
            });
            this.addRenderableWidget((GuiEventListener)catButton);
        }
        int doneWidth = 200;
        int doneX = (this.width - doneWidth) / 2;
        int doneY = this.height - 34;
        this.addRenderableWidget((GuiEventListener)new RotaButton(doneX, doneY, doneWidth, 22, (Component)Component.literal((String)"Conclu\u00eddo"), btn -> this.saveAndClose()));
    }

    private void saveAndClose() {
        if (this.onSaveCallback != null) {
            this.onSaveCallback.run();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, "\u00a76\u00a7l" + this.module.getDisplayName().getString(), this.width / 2, 16, -22016);
        graphics.drawCenteredString(this.font, "\u00a77Menu de Configura\u00e7\u00f5es - Selecione uma \u00e1rea para personalizar", this.width / 2, 29, -5592406);
        this.detectHoveredDescription(mouseX, mouseY);
        if (!this.hoveredDescription.isEmpty()) {
            int maxTextWidth = Math.min(460, this.width - 40);
            List lines = this.font.split((FormattedText)Component.literal((String)("\u00a7e" + this.hoveredDescription)), maxTextWidth);
            int lineHeight = 11;
            int totalTextHeight = lines.size() * lineHeight;
            int paddingX = 10;
            int paddingY = 4;
            int maxLineWidth = 0;
            for (FormattedCharSequence line : lines) {
                maxLineWidth = Math.max(maxLineWidth, this.font.width(line));
            }
            int boxWidth = Math.min(maxLineWidth + paddingX * 2, this.width - 20);
            int boxHeight = totalTextHeight + paddingY * 2;
            int boxX = (this.width - boxWidth) / 2;
            int boxY = RotaModuleCategoriesScreen.calculateDescriptionBoxY(this.height, boxHeight, this.categories.size());
            graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -1728053248);
            graphics.renderOutline(boxX, boxY, boxWidth, boxHeight, 0x44FFFFFF);
            int textY = boxY + paddingY;
            for (FormattedCharSequence line : lines) {
                int lineWidth = this.font.width(line);
                graphics.drawString(this.font, line, (this.width - lineWidth) / 2, textY, 0xFFFFFF, false);
                textY += lineHeight;
            }
        }
    }

    private void detectHoveredDescription(int mouseX, int mouseY) {
        int totalGridWidth = 434;
        int startX = (this.width - totalGridWidth) / 2;
        int startY = RotaModuleCategoriesScreen.calculateGridStartY(this.height);
        for (int i = 0; i < this.categories.size(); ++i) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * 224;
            int y = startY + row * 28;
            if (mouseX < x || mouseX > x + 210 || mouseY < y || mouseY > y + 22) continue;
            this.hoveredDescription = this.categories.get(i).getDescription();
            return;
        }
        this.hoveredDescription = "";
    }

    public void onClose() {
        this.saveAndClose();
    }
}

