package com.rotacore.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Botão clássico da suíte Rota Mods baseado no botão nativo do Minecraft.
 * Renderiza relevo 3D, cantos biselados e textura metálica oficial do jogo.
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
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.accent) {
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFFFFAA00);
        }
    }
}
