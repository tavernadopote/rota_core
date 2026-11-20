/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class RotaTabButton
extends AbstractButton {
    private boolean selected;
    private final Runnable onClickAction;

    public RotaTabButton(int x, int y, int width, int height, Component title, boolean selected, Runnable onClickAction) {
        super(x, y, width, height, title);
        this.selected = selected;
        this.onClickAction = onClickAction;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void onPress() {
        if (this.onClickAction != null) {
            this.onClickAction.run();
        }
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = this.isHoveredOrFocused();
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        int bgColor = this.selected ? -14012614 : (hovered ? -14407373 : -1441392353);
        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.fill(x, y, x + w, y + 1, this.selected ? -22016 : -13288373);
        if (this.selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, -22016);
        }
        int textColor = this.selected ? -22016 : (hovered ? -1 : -6246720);
        graphics.drawCenteredString(mc.font, this.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }

    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}

