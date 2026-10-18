package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.theme.RotaTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Botão de Aba superior da interface Rota Mods.
 * Destaque dourado e linha de base quando selecionado.
 */
public class RotaTabButton extends AbstractButton {

    private boolean selected;
    private final Runnable onClickAction;

    public RotaTabButton(int x, int y, int width, int height, Component title, boolean selected, Runnable onClickAction) {
        super(x, y, width, height, title);
        this.selected = selected;
        this.onClickAction = onClickAction;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress() {
        if (this.onClickAction != null) {
            this.onClickAction.run();
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = this.isHoveredOrFocused();

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        // Fundo da aba
        int bgColor = selected ? 0xFF2A2F3A : (hovered ? 0xFF242933 : 0xAA16191F);
        graphics.fill(x, y, x + w, y + h, bgColor);

        // Borda superior e laterais sutis
        graphics.fill(x, y, x + w, y + 1, selected ? RotaTheme.BORDER_ACCENT : 0xFF353C4B);

        // Linha indicadora inferior
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, RotaTheme.BORDER_ACCENT);
        }

        // Título da aba
        int textColor = selected ? RotaTheme.TEXT_TITLE : (hovered ? 0xFFFFFFFF : RotaTheme.TEXT_MUTED);
        graphics.drawCenteredString(mc.font, this.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
