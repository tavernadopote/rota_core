/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.rotacore.client.gui.hud;

import com.rotacore.RotaCore;
import com.rotacore.client.gui.hud.IRotaHudElement;
import com.rotacore.client.gui.hud.RotaHudAnchor;
import com.rotacore.client.gui.hud.RotaHudEditScreen;
import com.rotacore.client.gui.hud.RotaHudRegistry;
import com.rotacore.config.RotaCoreConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RotaHudLayoutManager {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rota_core/hud_layout");
    public static final int DEFAULT_GAP = 6;
    public static final int DEFAULT_MARGIN = 6;

    private RotaHudLayoutManager() {
    }

    public static Map<String, HudLayoutResult> calculateLayout(int screenWidth, int screenHeight, Collection<IRotaHudElement> elements, Map<String, RotaCoreConfig.HudElementConfig> customConfigs) {
        LinkedHashMap<String, HudLayoutResult> results = new LinkedHashMap<String, HudLayoutResult>();
        if (elements == null || elements.isEmpty()) {
            return results;
        }
        EnumMap anchorGroups = new EnumMap(RotaHudAnchor.class);
        for (RotaHudAnchor anchor : RotaHudAnchor.values()) {
            anchorGroups.put(anchor, new ArrayList());
        }
        for (IRotaHudElement iRotaHudElement : elements) {
            RotaCoreConfig.HudElementConfig cfg;
            String id = iRotaHudElement.getId();
            RotaCoreConfig.HudElementConfig hudElementConfig = cfg = customConfigs != null ? customConfigs.get(id) : null;
            if (cfg != null && cfg.customPosition) {
                int w = Math.max(1, iRotaHudElement.getWidth());
                int h = Math.max(1, iRotaHudElement.getHeight());
                int clampedX = Math.max(0, Math.min(cfg.customX, Math.max(0, screenWidth - w)));
                int clampedY = Math.max(0, Math.min(cfg.customY, Math.max(0, screenHeight - h)));
                results.put(id, new HudLayoutResult(id, clampedX, clampedY, w, h, RotaHudAnchor.CUSTOM));
                continue;
            }
            RotaHudAnchor anchor = iRotaHudElement.getDefaultAnchor();
            if (cfg != null && cfg.anchor != null && !cfg.anchor.equals("DEFAULT") && !cfg.anchor.equals("CUSTOM")) {
                anchor = RotaHudAnchor.fromString(cfg.anchor);
            }
            ((List)anchorGroups.get((Object)anchor)).add(iRotaHudElement);
        }
        for (Map.Entry entry : anchorGroups.entrySet()) {
            int y;
            int x;
            int marginY;
            int marginX;
            int h;
            int w;
            boolean isFirst;
            RotaHudAnchor anchor = (RotaHudAnchor)((Object)entry.getKey());
            List group = (List)entry.getValue();
            if (group.isEmpty()) continue;
            group.sort(Comparator.comparingInt(IRotaHudElement::getPriority).thenComparing(IRotaHudElement::getId));
            if (anchor.isBottomAnchored()) {
                int currentBottomY = screenHeight;
                isFirst = true;
                for (IRotaHudElement elem : group) {
                    w = elem.getWidth();
                    h = elem.getHeight();
                    marginX = elem.getMarginX();
                    marginY = elem.getMarginY();
                    x = anchor.calculateBaseX(screenWidth, w, marginX);
                    if (isFirst) {
                        y = screenHeight - marginY - h;
                        currentBottomY = y - 6;
                        isFirst = false;
                    } else {
                        y = currentBottomY - h;
                        currentBottomY = y - 6;
                    }
                    x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
                    y = Math.max(0, y);
                    results.put(elem.getId(), new HudLayoutResult(elem.getId(), x, y, w, h, anchor));
                }
                continue;
            }
            if (anchor == RotaHudAnchor.MIDDLE_LEFT || anchor == RotaHudAnchor.CENTER || anchor == RotaHudAnchor.MIDDLE_RIGHT) {
                int totalHeight = 0;
                for (int i = 0; i < group.size(); ++i) {
                    totalHeight += ((IRotaHudElement)group.get(i)).getHeight();
                    if (i >= group.size() - 1) continue;
                    totalHeight += 6;
                }
                int currentY = Math.max(0, (screenHeight - totalHeight) / 2);
                for (IRotaHudElement elem : group) {
                    w = elem.getWidth();
                    h = elem.getHeight();
                    marginX = elem.getMarginX();
                    int x2 = anchor.calculateBaseX(screenWidth, w, marginX);
                    int y2 = currentY;
                    currentY += h + 6;
                    x2 = Math.max(0, Math.min(x2, Math.max(0, screenWidth - w)));
                    y2 = Math.max(0, Math.min(y2, Math.max(0, screenHeight - h)));
                    results.put(elem.getId(), new HudLayoutResult(elem.getId(), x2, y2, w, h, anchor));
                }
                continue;
            }
            int currentTopY = 0;
            isFirst = true;
            for (IRotaHudElement elem : group) {
                w = elem.getWidth();
                h = elem.getHeight();
                marginX = elem.getMarginX();
                marginY = elem.getMarginY();
                x = anchor.calculateBaseX(screenWidth, w, marginX);
                if (isFirst) {
                    y = marginY;
                    currentTopY = y + h + 6;
                    isFirst = false;
                } else {
                    y = currentTopY;
                    currentTopY = y + h + 6;
                }
                x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
                y = Math.max(0, Math.min(y, Math.max(0, screenHeight - h)));
                results.put(elem.getId(), new HudLayoutResult(elem.getId(), x, y, w, h, anchor));
            }
        }
        return results;
    }

    public static Map<String, HudLayoutResult> calculateCurrentLayout(int screenWidth, int screenHeight) {
        return RotaHudLayoutManager.calculateLayout(screenWidth, screenHeight, RotaHudRegistry.getVisibleElements(), RotaCore.getConfig().hudElements);
    }

    public static Map<String, HudLayoutResult> calculateAllLayout(int screenWidth, int screenHeight) {
        return RotaHudLayoutManager.calculateLayout(screenWidth, screenHeight, RotaHudRegistry.getAllElements(), RotaCore.getConfig().hudElements);
    }

    public static HudLayoutResult getBounds(String id, int screenWidth, int screenHeight) {
        if (id == null) {
            return null;
        }
        IRotaHudElement elem = RotaHudRegistry.getElement(id);
        if (elem == null) {
            return null;
        }
        Map<String, HudLayoutResult> layout = RotaHudLayoutManager.calculateCurrentLayout(screenWidth, screenHeight);
        return layout.get(id);
    }

    public static void renderAll(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }
        if (mc.screen instanceof RotaHudEditScreen) {
            return;
        }
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        List<IRotaHudElement> visibleElements = RotaHudRegistry.getVisibleElements();
        if (visibleElements.isEmpty()) {
            return;
        }
        Map<String, HudLayoutResult> layout = RotaHudLayoutManager.calculateLayout(screenWidth, screenHeight, visibleElements, RotaCore.getConfig().hudElements);
        for (IRotaHudElement elem : visibleElements) {
            HudLayoutResult bounds = layout.get(elem.getId());
            if (bounds == null) continue;
            try {
                elem.render(graphics, bounds.x(), bounds.y(), bounds.width(), bounds.height(), partialTick);
            }
            catch (Exception e) {
                LOGGER.error("Falha ao renderizar HUD '{}': {}", (Object)elem.getId(), (Object)e.getMessage());
            }
        }
    }

    public record HudLayoutResult(String id, int x, int y, int width, int height, RotaHudAnchor effectiveAnchor) {
        public float cx() {
            return (float)this.x + (float)this.width / 2.0f;
        }

        public float cy() {
            return (float)this.y + (float)this.height / 2.0f;
        }

        public float radius() {
            return (float)Math.min(this.width, this.height) / 2.0f;
        }

        public boolean contains(double px, double py) {
            return px >= (double)this.x && px <= (double)(this.x + this.width) && py >= (double)this.y && py <= (double)(this.y + this.height);
        }
    }
}

