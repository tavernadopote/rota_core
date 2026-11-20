/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class RotaSlider
extends AbstractSliderButton {
    private final Function<Double, Component> textFunction;
    private final Consumer<Double> onApply;

    public RotaSlider(int x, int y, int width, int height, double initialRatio, Function<Double, Component> textFunction, Consumer<Double> onApply) {
        super(x, y, width, height, textFunction.apply(initialRatio), initialRatio);
        this.textFunction = textFunction;
        this.onApply = onApply;
    }

    protected void updateMessage() {
        this.setMessage(this.textFunction.apply(this.value));
    }

    protected void applyValue() {
        if (this.onApply != null) {
            this.onApply.accept(this.value);
        }
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}

