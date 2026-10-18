package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.theme.RotaTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Interruptor / Switch moderno (Ligado/Desligado) da Rota Mods.
 */
public class RotaSwitch extends AbstractButton {

    private boolean value;
    private final Consumer<Boolean> onToggle;

    public RotaSwitch(int x, int y, int width, int height, boolean initialValue, Consumer<Boolean> onToggle) {
        super(x, y, width, height, getStatusText(initialValue));
        this.value = initialValue;
        this.onToggle = onToggle;
    }

    private static Component getStatusText(boolean val) {
        return val ? Component.literal("§aLIGADO") : Component.literal("§cDESLIGADO");
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
        this.setMessage(getStatusText(value));
    }

    @Override
    public void onPress() {
        this.value = !this.value;
        this.setMessage(getStatusText(this.value));
        if (this.onToggle != null) {
            this.onToggle.accept(this.value);
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

        // Fundo do interruptor
        int bgColor = this.value ? 0x4448BB78 : 0x44E53E3E;
        int borderColor = hovered ? RotaTheme.BORDER_ACCENT : (this.value ? RotaTheme.SWITCH_ON : RotaTheme.SWITCH_OFF);

        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.renderOutline(x, y, w, h, borderColor);

        // Indicador deslizante lateral
        int knobWidth = 14;
        int knobX = this.value ? (x + w - knobWidth - 2) : (x + 2);
        int knobColor = this.value ? RotaTheme.SWITCH_ON : RotaTheme.SWITCH_OFF;

        graphics.fill(knobX, y + 2, knobX + knobWidth, y + h - 2, knobColor);

        // Texto de status centralizado
        graphics.drawCenteredString(mc.font, this.getMessage(), x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
