package com.rotacore.client.gui.hud;

import com.rotacore.RotaCore;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.config.RotaCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tela de sandbox visual para edição e reposicionamento livre de HUDs com o mouse.
 * Permite arrastar os elementos pela tela, aplicar encaixe magnético aos cantos e centros,
 * restaurar padrões e persistir as coordenadas no arquivo de configuração global.
 */
public class RotaHudEditScreen extends Screen {

    private final Screen parentScreen;
    private final List<HudEditItem> items = new ArrayList<>();
    private HudEditItem activeDragged = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private static final int SNAP_DISTANCE = 10;
    private static final int MARGIN = 6;

    public RotaHudEditScreen(Screen parentScreen) {
        super(Component.literal("Editor de HUDs"));
        this.parentScreen = parentScreen;
    }

    public static class HudEditItem {
        public final IRotaHudElement element;
        public int x;
        public int y;
        public int width;
        public int height;
        public boolean isCustom;

        public HudEditItem(IRotaHudElement element, int x, int y, int width, int height, boolean isCustom) {
            this.element = element;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isCustom = isCustom;
        }

        public boolean contains(double px, double py) {
            return px >= x && px <= (x + width) && py >= y && py <= (y + height);
        }
    }

    @Override
    protected void init() {
        this.items.clear();
        this.activeDragged = null;

        RotaCoreConfig config = RotaCore.getConfig();
        Map<String, RotaHudLayoutManager.HudLayoutResult> layout = RotaHudLayoutManager.calculateAllLayout(this.width, this.height);

        for (IRotaHudElement elem : RotaHudRegistry.getAllElements()) {
            RotaHudLayoutManager.HudLayoutResult result = layout.get(elem.getId());
            RotaCoreConfig.HudElementConfig elemCfg = config.hudElements.get(elem.getId());
            boolean isCustom = elemCfg != null && elemCfg.customPosition;

            int posX = (result != null) ? result.x() : MARGIN;
            int posY = (result != null) ? result.y() : MARGIN;
            int w = elem.getWidth();
            int h = elem.getHeight();

            items.add(new HudEditItem(elem, posX, posY, w, h, isCustom));
        }

        // Barra inferior de botões de ação
        int btnW = 120;
        int btnH = 20;
        int gap = 8;
        int totalW = (btnW * 4) + (gap * 3);
        int startX = (this.width - totalW) / 2;
        int bottomY = this.height - 28;

        // 1. Restaurar Padrões
        this.addRenderableWidget(new RotaButton(
                startX, bottomY, btnW, btnH,
                Component.literal("Restaurar Padrões"),
                btn -> restoreDefaults()
        ));

        // 2. Alinhar Automaticamente
        this.addRenderableWidget(new RotaButton(
                startX + btnW + gap, bottomY, btnW, btnH,
                Component.literal("Alinhar Âncoras"),
                btn -> autoAlign()
        ));

        // 3. Salvar e Fechar
        this.addRenderableWidget(new RotaButton(
                startX + (btnW + gap) * 2, bottomY, btnW, btnH,
                Component.literal("Salvar e Fechar"),
                btn -> saveAndClose()
        ).setAccent(true));

        // 4. Cancelar
        this.addRenderableWidget(new RotaButton(
                startX + (btnW + gap) * 3, bottomY, btnW, btnH,
                Component.literal("Cancelar"),
                btn -> onClose()
        ));
    }

    private void restoreDefaults() {
        RotaCore.getConfig().resetAllHudPositions();
        autoAlign();
    }

    private void autoAlign() {
        Map<String, RotaHudLayoutManager.HudLayoutResult> defaultLayout = RotaHudLayoutManager.calculateLayout(
                this.width, this.height, RotaHudRegistry.getAllElements(), null
        );

        for (HudEditItem item : items) {
            RotaHudLayoutManager.HudLayoutResult res = defaultLayout.get(item.element.getId());
            if (res != null) {
                item.x = res.x();
                item.y = res.y();
                item.isCustom = false;
            }
        }
    }

    private void saveAndClose() {
        RotaCoreConfig config = RotaCore.getConfig();
        for (HudEditItem item : items) {
            if (item.isCustom) {
                config.setHudElementPosition(item.element.getId(), item.x, item.y);
            } else {
                config.resetHudElementPosition(item.element.getId());
            }
        }
        RotaCore.saveConfig();
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No-op para evitar o shader de blur do Minecraft cobrindo o editor
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // No-op
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        // No-op
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        // No-op
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Fundo escurecido semi-transparente limpo sem blur
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        // 2. Guias magnéticas sutis ao arrastar
        if (activeDragged != null) {
            renderMagneticGuides(graphics);
        }

        // 3. Caixas delimitadoras de cada HUD
        for (HudEditItem item : items) {
            boolean isDragged = (item == activeDragged);
            boolean isHovered = !isDragged && item.contains(mouseX, mouseY);

            int bgColor = isDragged ? 0x55FFAA00 : (isHovered ? 0x44FFAA00 : 0x3316191F);
            int frameColor = isDragged ? 0xFFFFAA00 : (isHovered ? 0xFFFFFFFF : 0xFF888888);

            // Fundo mascarado da geometria
            RotaHudGeometry.renderMaskedBackground(graphics, item.x, item.y, item.width, item.height, item.element.getShape(), bgColor);

            // Moldura externa
            if (item.element.getShape() == RotaViewportShape.CIRCLE) {
                RotaHudGeometry.renderFrame(graphics, item.x, item.y, item.width, item.height, RotaViewportShape.CIRCLE, frameColor, 1.5f);
            } else {
                RotaHudGeometry.renderDottedFrame(graphics, item.x, item.y, item.width, item.height, frameColor, 4, 3);
            }

            // Título e identificação do elemento
            int titleY = item.y + (item.height / 2) - 8;
            graphics.drawCenteredString(this.font, item.element.getDisplayName(), item.x + item.width / 2, titleY, 0xFFFFFFFF);

            String statusStr = item.isCustom
                    ? String.format("§ePos: %d, %d", item.x, item.y)
                    : String.format("§7Âncora: %s", item.element.getDefaultAnchor().name());
            graphics.drawCenteredString(this.font, statusStr, item.x + item.width / 2, titleY + 11, RotaTheme.TEXT_MUTED);
        }

        // 4. Cabeçalho explicativo
        graphics.drawCenteredString(this.font, "§6§lEDITOR DE POSICIONAMENTO DE HUDS", this.width / 2, 12, RotaTheme.TEXT_TITLE);
        graphics.drawCenteredString(this.font, "§7Clique e arraste as caixas com o mouse. Encaixe magnético ativo nos cantos e centros.", this.width / 2, 24, RotaTheme.TEXT_MUTED);

        if (items.isEmpty()) {
            graphics.drawCenteredString(this.font, "§8Nenhum elemento HUD registrado no momento.", this.width / 2, this.height / 2, RotaTheme.TEXT_MUTED);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMagneticGuides(GuiGraphics graphics) {
        int guideColor = 0x22FFFFFF;
        // Centro horizontal e vertical
        graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, guideColor);
        graphics.fill(0, this.height / 2, this.width, this.height / 2 + 1, guideColor);

        // Margens externas
        graphics.fill(MARGIN, 0, MARGIN + 1, this.height, guideColor);
        graphics.fill(this.width - MARGIN, 0, this.width - MARGIN + 1, this.height, guideColor);
        graphics.fill(0, MARGIN, this.width, MARGIN + 1, guideColor);
        graphics.fill(0, this.height - MARGIN, this.width, this.height - MARGIN + 1, guideColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Botão esquerdo do mouse
            // Itera em ordem reversa para selecionar o elemento no topo
            for (int i = items.size() - 1; i >= 0; i--) {
                HudEditItem item = items.get(i);
                if (item.element.isMovable() && item.contains(mouseX, mouseY)) {
                    this.activeDragged = item;
                    this.dragOffsetX = (int) (mouseX - item.x);
                    this.dragOffsetY = (int) (mouseY - item.y);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeDragged != null) {
            int targetX = (int) (mouseX - dragOffsetX);
            int targetY = (int) (mouseY - dragOffsetY);

            // Encaixe magnético horizontal
            int snapLeft = MARGIN;
            int snapRight = this.width - MARGIN - activeDragged.width;
            int snapCenterX = (this.width - activeDragged.width) / 2;

            if (Math.abs(targetX - snapLeft) <= SNAP_DISTANCE) targetX = snapLeft;
            else if (Math.abs(targetX - snapRight) <= SNAP_DISTANCE) targetX = snapRight;
            else if (Math.abs(targetX - snapCenterX) <= SNAP_DISTANCE) targetX = snapCenterX;

            // Encaixe magnético vertical
            int snapTop = MARGIN;
            int snapBottom = this.height - MARGIN - activeDragged.height;
            int snapCenterY = (this.height - activeDragged.height) / 2;

            if (Math.abs(targetY - snapTop) <= SNAP_DISTANCE) targetY = snapTop;
            else if (Math.abs(targetY - snapBottom) <= SNAP_DISTANCE) targetY = snapBottom;
            else if (Math.abs(targetY - snapCenterY) <= SNAP_DISTANCE) targetY = snapCenterY;

            // Clamping para não sair da tela
            targetX = Math.max(0, Math.min(targetX, Math.max(0, this.width - activeDragged.width)));
            targetY = Math.max(0, Math.min(targetY, Math.max(0, this.height - activeDragged.height)));

            activeDragged.x = targetX;
            activeDragged.y = targetY;
            activeDragged.isCustom = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeDragged != null) {
            this.activeDragged = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
}
