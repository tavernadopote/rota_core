package com.rotacore.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Botão clássico da suíte Rota Mods baseado no botão nativo do Minecraft.
 * Renderiza relevo 3D, cantos biselados e textura metálica oficial do jogo.
 */
public class RotaButton extends Button {

    private boolean accent = false;
    private boolean danger = false;
    private Supplier<ItemStack> iconSupplier = null;

    public RotaButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public RotaButton setAccent(boolean accent) {
        this.accent = accent;
        return this;
    }

    public boolean isDanger() {
        return danger;
    }

    public RotaButton setDanger(boolean danger) {
        this.danger = danger;
        return this;
    }

    public Supplier<ItemStack> getIconSupplier() {
        return iconSupplier;
    }

    public RotaButton setIconSupplier(Supplier<ItemStack> iconSupplier) {
        this.iconSupplier = iconSupplier;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.danger) {
            int outlineColor = this.isHoveredOrFocused() ? 0xFFFF4444 : 0xFFAA2222;
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, outlineColor);
        } else if (this.accent) {
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFFFFAA00);
        }
        if (this.iconSupplier != null) {
            ItemStack stack = this.iconSupplier.get();
            if (stack != null && !stack.isEmpty()) {
                graphics.renderFakeItem(stack, this.getX() + 4, this.getY() + (this.getHeight() - 16) / 2);
            }
        }
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int color) {
        int textColor = color;
        if (this.danger) {
            textColor = !this.active ? 0xFFAA5555 : (this.isHoveredOrFocused() ? 0xFFFF7777 : 0xFFFF5555);
        }

        ItemStack stack = this.iconSupplier != null ? this.iconSupplier.get() : null;
        if (stack != null && !stack.isEmpty()) {
            int iconOffset = 22;
            int textLeft = this.getX() + iconOffset;
            int textRight = this.getX() + this.width - 4;
            int textY = this.getY() + (this.height - 8) / 2;
            int availableWidth = Math.max(10, textRight - textLeft);

            Component msg = this.getMessage();
            int stringWidth = font.width(msg);
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
