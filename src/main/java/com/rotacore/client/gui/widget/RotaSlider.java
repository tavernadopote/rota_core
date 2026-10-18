package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.theme.RotaTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Slider customizado da Rota Mods para ajustes numéricos de precisão.
 */
public class RotaSlider extends AbstractSliderButton {

    private final Function<Double, Component> textFunction;
    private final Consumer<Double> onApply;

    public RotaSlider(
            int x, int y, int width, int height,
            double initialRatio,
            Function<Double, Component> textFunction,
            Consumer<Double> onApply
    ) {
        super(x, y, width, height, textFunction.apply(initialRatio), initialRatio);
        this.textFunction = textFunction;
        this.onApply = onApply;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.textFunction.apply(this.value));
    }

    @Override
    protected void applyValue() {
        if (this.onApply != null) {
            this.onApply.accept(this.value);
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

        // Fundo do trilho (Track)
        graphics.fill(x, y, x + w, y + h, RotaTheme.CONTROL_TRACK);
        graphics.renderOutline(x, y, w, h, hovered ? RotaTheme.BORDER_ACCENT : RotaTheme.BUTTON_BORDER);

        // Barra de progresso preenchida (Fill)
        int fillW = (int) Math.round(this.value * (w - 4));
        if (fillW > 0) {
            graphics.fill(x + 2, y + 2, x + 2 + fillW, y + h - 2, 0x55D4A373);
        }

        // Indicador deslizante (Knob)
        int knobWidth = 6;
        int knobX = x + (int) (this.value * (w - knobWidth));
        int knobColor = hovered ? RotaTheme.TEXT_TITLE : RotaTheme.CONTROL_KNOB;
        graphics.fill(knobX, y + 1, knobX + knobWidth, y + h - 1, knobColor);
        graphics.renderOutline(knobX, y + 1, knobWidth, h - 2, 0xFF111111);

        // Texto centralizado com sombra
        int textColor = hovered ? RotaTheme.TEXT_TITLE : RotaTheme.TEXT_BODY;
        graphics.drawCenteredString(mc.font, this.getMessage(), x + w / 2, y + (h - 8) / 2, textColor);
    }
}
