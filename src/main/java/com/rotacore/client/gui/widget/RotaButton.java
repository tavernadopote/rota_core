package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.theme.RotaTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Botão estilizado moderno da Rota Mods.
 * Fundo escuro texturizado, borda dourada/âmbar ao focar e feedback visual suave.
 */
public class RotaButton extends Button {

    private boolean accent = false;

    public RotaButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public RotaButton setAccent(boolean accent) {
        this.accent = accent;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = this.isHoveredOrFocused();

        int bgColor = !this.active ? 0x881A1D24 : (hovered ? RotaTheme.BUTTON_HOVER_BG : RotaTheme.BUTTON_NORMAL_BG);
        int borderColor = !this.active ? 0x55333333 : (hovered || accent ? RotaTheme.BUTTON_HOVER_BORDER : RotaTheme.BUTTON_BORDER);

        // Preenchimento do botão
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
        // Borda suave
        graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

        // Se ativo e focado, linha extra de brilho sutil
        if (hovered && this.active) {
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, 0x33FFAA00);
        }

        // Texto
        int textColor = !this.active ? RotaTheme.TEXT_MUTED : (hovered || accent ? RotaTheme.TEXT_TITLE : RotaTheme.TEXT_BODY);
        int textY = this.getY() + (this.height - 8) / 2;
        graphics.drawCenteredString(mc.font, this.getMessage(), this.getX() + this.width / 2, textY, textColor);
    }
}
