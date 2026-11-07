package com.rotacore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Modal universal de confirmação de segurança do Rota Core.
 * Renderiza uma janela modal 240x120px centralizada sobreposta com elevação Z=400,
 * mensagem com quebra automática de linha e botão de perigo/alerta em destaque.
 */
public class ConfirmModalWidget extends Screen {

    public static final int PANEL_WIDTH = 240;
    public static final int PANEL_HEIGHT = 120;

    private final Screen parentScreen;
    private final Component message;
    private final Component confirmText;
    private final Runnable onConfirm;

    private RotaButton cancelButton;
    private RotaButton confirmButton;

    public ConfirmModalWidget(Screen parentScreen, Component title, Component message, Component confirmText, Runnable onConfirm) {
        super(title != null ? title : Component.literal("§c§lConfirmar Ação"));
        this.parentScreen = parentScreen;
        this.message = message != null ? message : Component.literal("Tem certeza de que deseja executar esta ação?");
        this.confirmText = confirmText != null ? confirmText : Component.literal("Confirmar");
        this.onConfirm = onConfirm;
    }

    public static void open(Screen parent, Component title, Component message, Component confirmText, Runnable onConfirm) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ConfirmModalWidget(parent, title, message, confirmText, onConfirm));
        }
    }

    public static void open(Screen parent, Component title, Component message, Runnable onConfirm) {
        open(parent, title, message, Component.literal("Confirmar"), onConfirm);
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int btnWidth = 100;
        int btnHeight = 20;
        int btnY = panelY + PANEL_HEIGHT - 28;

        this.cancelButton = new RotaButton(panelX + 14, btnY, btnWidth, btnHeight, Component.literal("Cancelar"), btn -> close());
        this.confirmButton = new RotaButton(panelX + PANEL_WIDTH - btnWidth - 14, btnY, btnWidth, btnHeight, this.confirmText, btn -> {
            if (this.onConfirm != null) {
                this.onConfirm.run();
            }
            close();
        });
        this.confirmButton.setDanger(true);

        this.addRenderableWidget(this.cancelButton);
        this.addRenderableWidget(this.confirmButton);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // NO-OP absoluto: impede que o Minecraft aplique shaders de blur ou sujeira sobre os painéis
    }

    @Override
    protected void renderMenuBackground(GuiGraphics g) {
        // NO-OP absoluto
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Renderiza a tela de trás estática se disponível
        if (this.parentScreen != null) {
            this.parentScreen.render(graphics, -1, -1, partialTick);
        }

        // 2. Elevação Z=400: isola a janela modal para que nenhum componente da tela de fundo sobreponha o modal
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);

        // 3. Fundo escurecido semi-transparente cobrindo a tela inteira
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // 4. Painel central vanilla (240x120px) com borda cinza #C6C6C6
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF222222);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.renderOutline(panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 0xFF3A3A3A);

        // 5. Título de Alerta
        graphics.drawCenteredString(this.font, this.title, panelX + PANEL_WIDTH / 2, panelY + 12, 0xFFFF5555);

        // 6. Mensagem com quebra automática de linha
        int maxTextWidth = PANEL_WIDTH - 28;
        List<FormattedCharSequence> lines = this.font.split(this.message, maxTextWidth);
        int textY = panelY + 32;
        for (FormattedCharSequence line : lines) {
            int lineWidth = this.font.width(line);
            graphics.drawString(this.font, line, panelX + (PANEL_WIDTH - lineWidth) / 2, textY, 0xFFDDDDDD, false);
            textY += 11;
        }

        // 7. Renderiza botões (Cancelar e Confirmar)
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
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
        return parentScreen;
    }

    public Component getMessage() {
        return message;
    }

    public Component getConfirmText() {
        return confirmText;
    }

    public Runnable getOnConfirm() {
        return onConfirm;
    }
}
