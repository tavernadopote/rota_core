/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui.hud;

import com.rotacore.RotaCore;
import com.rotacore.client.gui.hud.IRotaHudElement;
import com.rotacore.client.gui.hud.RotaHudGeometry;
import com.rotacore.client.gui.hud.RotaHudLayoutManager;
import com.rotacore.client.gui.hud.RotaHudRegistry;
import com.rotacore.client.gui.hud.RotaViewportShape;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.config.RotaCoreConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RotaHudEditScreen
extends Screen {
    private final Screen parentScreen;
    private final List<HudEditItem> items = new ArrayList<HudEditItem>();
    private HudEditItem activeDragged = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private static final int SNAP_DISTANCE = 10;
    private static final int MARGIN = 6;

    public RotaHudEditScreen(Screen parentScreen) {
        super((Component)Component.literal((String)"Editor de HUDs"));
        this.parentScreen = parentScreen;
    }

    protected void init() {
        this.items.clear();
        this.activeDragged = null;
        for (IRotaHudElement elem : RotaHudRegistry.getAllElements()) {
            elem.setPreviewMode(true);
        }
        RotaCoreConfig config = RotaCore.getConfig();
        Map<String, RotaHudLayoutManager.HudLayoutResult> layout = RotaHudLayoutManager.calculateAllLayout(this.width, this.height);
        for (IRotaHudElement elem : RotaHudRegistry.getAllElements()) {
            RotaHudLayoutManager.HudLayoutResult result = layout.get(elem.getId());
            RotaCoreConfig.HudElementConfig elemCfg = config.hudElements.get(elem.getId());
            boolean isCustom = elemCfg != null && elemCfg.customPosition;
            int posX = result != null ? result.x() : 6;
            int posY = result != null ? result.y() : 6;
            int w = elem.getWidth();
            int h = elem.getHeight();
            this.items.add(new HudEditItem(elem, posX, posY, w, h, isCustom));
        }
        int btnW = 120;
        int btnH = 20;
        int gap = 8;
        int totalW = btnW * 4 + gap * 3;
        int startX = (this.width - totalW) / 2;
        int bottomY = this.height - 28;
        this.addRenderableWidget((GuiEventListener)new RotaButton(startX, bottomY, btnW, btnH, (Component)Component.literal((String)"Restaurar Padr\u00f5es"), btn -> this.restoreDefaults()));
        this.addRenderableWidget((GuiEventListener)new RotaButton(startX + btnW + gap, bottomY, btnW, btnH, (Component)Component.literal((String)"Alinhar \u00c2ncoras"), btn -> this.autoAlign()));
        this.addRenderableWidget((GuiEventListener)new RotaButton(startX + (btnW + gap) * 2, bottomY, btnW, btnH, (Component)Component.literal((String)"Salvar e Fechar"), btn -> this.saveAndClose()).setAccent(true));
        this.addRenderableWidget((GuiEventListener)new RotaButton(startX + (btnW + gap) * 3, bottomY, btnW, btnH, (Component)Component.literal((String)"Cancelar"), btn -> this.onClose()));
    }

    private void restoreDefaults() {
        RotaCore.getConfig().resetAllHudPositions();
        this.autoAlign();
    }

    private void autoAlign() {
        Map<String, RotaHudLayoutManager.HudLayoutResult> defaultLayout = RotaHudLayoutManager.calculateLayout(this.width, this.height, RotaHudRegistry.getAllElements(), null);
        for (HudEditItem item : this.items) {
            RotaHudLayoutManager.HudLayoutResult res = defaultLayout.get(item.element.getId());
            if (res == null) continue;
            item.x = res.x();
            item.y = res.y();
            item.isCustom = false;
        }
    }

    private void saveAndClose() {
        RotaCoreConfig config = RotaCore.getConfig();
        for (HudEditItem item : this.items) {
            if (item.isCustom) {
                config.setHudElementPosition(item.element.getId(), item.x, item.y);
                continue;
            }
            config.resetHudElementPosition(item.element.getId());
        }
        RotaCore.saveConfig();
        this.onClose();
    }

    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderMenuBackground(GuiGraphics graphics) {
    }

    protected void renderMenuBackground(GuiGraphics graphics, int x, int y, int width, int height) {
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(partialTick);
        graphics.fill(0, 0, this.width, this.height, 0x77000000);
        if (this.activeDragged != null) {
            this.renderMagneticGuides(graphics);
        }
        for (HudEditItem item : this.items) {
            int bgColor;
            boolean isHovered;
            boolean isDragged = item == this.activeDragged;
            boolean bl = isHovered = !isDragged && item.contains(mouseX, mouseY);
            int n = isDragged ? 1442818560 : (bgColor = isHovered ? 1157605888 : 857086239);
            int frameColor = isDragged ? -22016 : (isHovered ? -1 : -7829368);
            RotaHudGeometry.renderMaskedBackground(graphics, item.x, item.y, item.width, item.height, item.element.getShape(), bgColor);
            if (item.element.getShape() == RotaViewportShape.CIRCLE) {
                RotaHudGeometry.renderFrame(graphics, item.x, item.y, item.width, item.height, RotaViewportShape.CIRCLE, frameColor, 1.5f);
            } else {
                RotaHudGeometry.renderDottedFrame(graphics, item.x, item.y, item.width, item.height, frameColor, 4, 3);
            }
            int titleY = item.y + item.height / 2 - 8;
            graphics.drawCenteredString(this.font, item.element.getDisplayName(), item.x + item.width / 2, titleY, -1);
            String statusStr = item.isCustom ? String.format("\u00a7ePos: %d, %d", item.x, item.y) : String.format("\u00a77\u00c2ncora: %s", item.element.getDefaultAnchor().name());
            graphics.drawCenteredString(this.font, statusStr, item.x + item.width / 2, titleY + 11, -6246720);
        }
        graphics.drawCenteredString(this.font, "\u00a76\u00a7lEDITOR DE POSICIONAMENTO DE HUDS", this.width / 2, 12, -22016);
        graphics.drawCenteredString(this.font, "\u00a77Clique e arraste as caixas com o mouse. Encaixe magn\u00e9tico ativo nos cantos e centros.", this.width / 2, 24, -6246720);
        if (this.items.isEmpty()) {
            graphics.drawCenteredString(this.font, "\u00a78Nenhum elemento HUD registrado no momento.", this.width / 2, this.height / 2, -6246720);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMagneticGuides(GuiGraphics graphics) {
        int guideColor = 0x22FFFFFF;
        graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, guideColor);
        graphics.fill(0, this.height / 2, this.width, this.height / 2 + 1, guideColor);
        graphics.fill(6, 0, 7, this.height, guideColor);
        graphics.fill(this.width - 6, 0, this.width - 6 + 1, this.height, guideColor);
        graphics.fill(0, 6, this.width, 7, guideColor);
        graphics.fill(0, this.height - 6, this.width, this.height - 6 + 1, guideColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = this.items.size() - 1; i >= 0; --i) {
                HudEditItem item = this.items.get(i);
                if (!item.element.isMovable() || !item.contains(mouseX, mouseY)) continue;
                this.activeDragged = item;
                this.dragOffsetX = (int)(mouseX - (double)item.x);
                this.dragOffsetY = (int)(mouseY - (double)item.y);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.activeDragged != null) {
            int targetX = (int)(mouseX - (double)this.dragOffsetX);
            int targetY = (int)(mouseY - (double)this.dragOffsetY);
            int snapLeft = 6;
            int snapRight = this.width - 6 - this.activeDragged.width;
            int snapCenterX = (this.width - this.activeDragged.width) / 2;
            if (Math.abs(targetX - snapLeft) <= 10) {
                targetX = snapLeft;
            } else if (Math.abs(targetX - snapRight) <= 10) {
                targetX = snapRight;
            } else if (Math.abs(targetX - snapCenterX) <= 10) {
                targetX = snapCenterX;
            }
            int snapTop = 6;
            int snapBottom = this.height - 6 - this.activeDragged.height;
            int snapCenterY = (this.height - this.activeDragged.height) / 2;
            if (Math.abs(targetY - snapTop) <= 10) {
                targetY = snapTop;
            } else if (Math.abs(targetY - snapBottom) <= 10) {
                targetY = snapBottom;
            } else if (Math.abs(targetY - snapCenterY) <= 10) {
                targetY = snapCenterY;
            }
            targetX = Math.max(0, Math.min(targetX, Math.max(0, this.width - this.activeDragged.width)));
            targetY = Math.max(0, Math.min(targetY, Math.max(0, this.height - this.activeDragged.height)));
            this.activeDragged.x = targetX;
            this.activeDragged.y = targetY;
            this.activeDragged.isCustom = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.activeDragged != null) {
            this.activeDragged = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void removed() {
        super.removed();
        for (IRotaHudElement elem : RotaHudRegistry.getAllElements()) {
            elem.setPreviewMode(false);
        }
    }

    public void onClose() {
        for (IRotaHudElement elem : RotaHudRegistry.getAllElements()) {
            elem.setPreviewMode(false);
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
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
            return px >= (double)this.x && px <= (double)(this.x + this.width) && py >= (double)this.y && py <= (double)(this.y + this.height);
        }
    }
}

