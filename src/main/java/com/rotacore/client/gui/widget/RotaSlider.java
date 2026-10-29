package com.rotacore.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Slider clássico da suíte Rota Mods baseado no slider nativo do Minecraft.
 * Utiliza o trilho e puxador texturizado oficial do jogo.
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
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
