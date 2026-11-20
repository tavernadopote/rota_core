/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 */
package com.rotacore.client.gui.widget;

import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

public class RotaButton
extends Button {
    private boolean accent = false;
    private boolean danger = false;
    private Supplier<ItemStack> iconSupplier = null;

    public RotaButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public RotaButton setAccent(boolean accent) {
        this.accent = accent;
        return this;
    }

    public boolean isDanger() {
        return this.danger;
    }

    public RotaButton setDanger(boolean danger) {
        this.danger = danger;
        return this;
    }

    public Supplier<ItemStack> getIconSupplier() {
        return this.iconSupplier;
    }

    public RotaButton setIconSupplier(Supplier<ItemStack> iconSupplier) {
        this.iconSupplier = iconSupplier;
        return this;
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack stack;
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.danger) {
            int outlineColor = this.isHoveredOrFocused() ? -48060 : -5627358;
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, outlineColor);
        } else if (this.accent) {
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, -22016);
        }
        if (this.iconSupplier != null && (stack = this.iconSupplier.get()) != null && !stack.isEmpty()) {
            graphics.renderFakeItem(stack, this.getX() + 4, this.getY() + (this.getHeight() - 16) / 2);
        }
    }

    public void renderString(GuiGraphics graphics, Font font, int color) {
        ItemStack stack;
        int textColor = color;
        if (this.danger) {
            textColor = !this.active ? -5614251 : (this.isHoveredOrFocused() ? -34953 : -43691);
        }
        ItemStack itemStack = stack = this.iconSupplier != null ? this.iconSupplier.get() : null;
        if (stack != null && !stack.isEmpty()) {
            int iconOffset = 22;
            int textLeft = this.getX() + iconOffset;
            int textRight = this.getX() + this.width - 4;
            int textY = this.getY() + (this.height - 8) / 2;
            int availableWidth = Math.max(10, textRight - textLeft);
            Component msg = this.getMessage();
            int stringWidth = font.width((FormattedText)msg);
            if (stringWidth <= availableWidth) {
                int centerX = textLeft + availableWidth / 2;
                graphics.drawCenteredString(font, msg, centerX, textY, textColor);
            } else {
                graphics.drawString(font, font.plainSubstrByWidth(msg.getString(), availableWidth - 6) + "...", textLeft, textY, textColor, false);
            }
        } else {
            super.renderString(graphics, font, textColor);
        }
    }
}

