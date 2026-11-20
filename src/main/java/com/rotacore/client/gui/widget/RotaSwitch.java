/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RotaSwitch
extends Button {
    private boolean value;
    private final Consumer<Boolean> onToggle;

    public RotaSwitch(int x, int y, int width, int height, boolean initialValue, Consumer<Boolean> onToggle) {
        super(x, y, width, height, RotaSwitch.getStatusText(initialValue), btn -> {}, DEFAULT_NARRATION);
        this.value = initialValue;
        this.onToggle = onToggle;
    }

    public static Component getStatusText(boolean val) {
        return val ? Component.literal((String)"\u00a7aATIVADO") : Component.literal((String)"\u00a7cDESATIVADO");
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
        this.setMessage(RotaSwitch.getStatusText(value));
    }

    public void onPress() {
        this.value = !this.value;
        this.setMessage(RotaSwitch.getStatusText(this.value));
        if (this.onToggle != null) {
            this.onToggle.accept(this.value);
        }
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}

