package com.rotacore.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Interruptor booleano clássico da suíte Rota Mods baseado no botão nativo do Minecraft.
 * Exibe §aATIVADO em verde quando ligado e §cDESATIVADO em vermelho quando desligado.
 */
public class RotaSwitch extends Button {

    private boolean value;
    private final Consumer<Boolean> onToggle;

    public RotaSwitch(int x, int y, int width, int height, boolean initialValue, Consumer<Boolean> onToggle) {
        super(x, y, width, height, getStatusText(initialValue), btn -> {}, DEFAULT_NARRATION);
        this.value = initialValue;
        this.onToggle = onToggle;
    }

    public static Component getStatusText(boolean val) {
        return val ? Component.literal("§aATIVADO") : Component.literal("§cDESATIVADO");
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
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
