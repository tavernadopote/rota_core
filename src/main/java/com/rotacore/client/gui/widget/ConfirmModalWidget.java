/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.widget.RotaButton;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class ConfirmModalWidget
extends Screen {
    public static final int PANEL_WIDTH = 240;
    public static final int PANEL_HEIGHT = 120;
    private final Screen parentScreen;
    private final Component message;
    private final Component confirmText;
    private final Runnable onConfirm;
    private RotaButton cancelButton;
    private RotaButton confirmButton;

    public ConfirmModalWidget(Screen parentScreen, Component title, Component message, Component confirmText, Runnable onConfirm) {
        super((Component)(title != null ? title : Component.literal((String)"\u00a7c\u00a7lConfirmar A\u00e7\u00e3o")));
        this.parentScreen = parentScreen;
        this.message = message != null ? message : Component.literal((String)"Tem certeza de que deseja executar esta a\u00e7\u00e3o?");
        this.confirmText = confirmText != null ? confirmText : Component.literal((String)"Confirmar");
        this.onConfirm = onConfirm;
    }

    public static void open(Screen parent, Component title, Component message, Component confirmText, Runnable onConfirm) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen((Screen)new ConfirmModalWidget(parent, title, message, confirmText, onConfirm));
        }
    }

    public static void open(Screen parent, Component title, Component message, Runnable onConfirm) {
        ConfirmModalWidget.open(parent, title, message, (Component)Component.literal((String)"Confirmar"), onConfirm);
    }

    protected void init() {
        int panelX = (this.width - 240) / 2;
        int panelY = (this.height - 120) / 2;
        int btnWidth = 100;
        int btnHeight = 20;
        int btnY = panelY + 120 - 28;
        this.cancelButton = new RotaButton(panelX + 14, btnY, btnWidth, btnHeight, (Component)Component.literal((String)"Cancelar"), btn -> this.close());
        this.confirmButton = new RotaButton(panelX + 240 - btnWidth - 14, btnY, btnWidth, btnHeight, this.confirmText, btn -> {
            if (this.onConfirm != null) {
                this.onConfirm.run();
            }
            this.close();
        });
        this.confirmButton.setDanger(true);
        this.addRenderableWidget((GuiEventListener)this.cancelButton);
        this.addRenderableWidget((GuiEventListener)this.confirmButton);
    }

    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderMenuBackground(GuiGraphics g) {
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.parentScreen != null) {
            this.parentScreen.render(graphics, -1, -1, partialTick);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);
        graphics.fill(0, 0, this.width, this.height, -2013265920);
        int panelX = (this.width - 240) / 2;
        int panelY = (this.height - 120) / 2;
        graphics.fill(panelX, panelY, panelX + 240, panelY + 120, -14540254);
        graphics.renderOutline(panelX, panelY, 240, 120, -3750202);
        graphics.renderOutline(panelX + 1, panelY + 1, 238, 118, -12961222);
        graphics.drawCenteredString(this.font, this.title, panelX + 120, panelY + 12, -43691);
        int maxTextWidth = 212;
        List lines = this.font.split((FormattedText)this.message, maxTextWidth);
        int textY = panelY + 32;
        for (FormattedCharSequence line : lines) {
            int lineWidth = this.font.width(line);
            graphics.drawString(this.font, line, panelX + (240 - lineWidth) / 2, textY, -2236963, false);
            textY += 11;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public Screen getParentScreen() {
        return this.parentScreen;
    }

    public Component getMessage() {
        return this.message;
    }

    public Component getConfirmText() {
        return this.confirmText;
    }

    public Runnable getOnConfirm() {
        return this.onConfirm;
    }
}

