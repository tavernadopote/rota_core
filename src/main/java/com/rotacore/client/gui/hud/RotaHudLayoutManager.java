package com.rotacore.client.gui.hud;

import com.rotacore.RotaCore;
import com.rotacore.config.RotaCoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Gerenciador universal de posicionamento, empilhamento anti-colisão e renderização centralizada de HUDs.
 */
public final class RotaHudLayoutManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("rota_core/hud_layout");
    public static final int DEFAULT_GAP = 6;
    public static final int DEFAULT_MARGIN = 6;

    private RotaHudLayoutManager() {}

    /**
     * Resultado do cálculo de layout para um elemento individual.
     */
    public record HudLayoutResult(
            String id,
            int x,
            int y,
            int width,
            int height,
            RotaHudAnchor effectiveAnchor
    ) {
        public float cx() {
            return x + width / 2.0f;
        }

        public float cy() {
            return y + height / 2.0f;
        }

        public float radius() {
            return Math.min(width, height) / 2.0f;
        }

        public boolean contains(double px, double py) {
            return px >= x && px <= (x + width) && py >= y && py <= (y + height);
        }
    }

    /**
     * Calcula dinamicamente o layout anti-colisão para uma coleção arbitrária de elementos.
     * Método puro e desacoplado de instâncias gráficas, ideal para testes unitários e telas de edição.
     *
     * @param screenWidth   Largura da tela em pixels de interface.
     * @param screenHeight  Altura da tela em pixels de interface.
     * @param elements      Elementos a serem posicionados.
     * @param customConfigs Mapa de configurações customizadas por ID (pode ser nulo).
     * @return Mapa indexado pelo ID do elemento contendo seu resultado de posicionamento.
     */
    public static Map<String, HudLayoutResult> calculateLayout(
            int screenWidth,
            int screenHeight,
            Collection<IRotaHudElement> elements,
            Map<String, RotaCoreConfig.HudElementConfig> customConfigs
    ) {
        Map<String, HudLayoutResult> results = new LinkedHashMap<>();
        if (elements == null || elements.isEmpty()) {
            return results;
        }

        Map<RotaHudAnchor, List<IRotaHudElement>> anchorGroups = new EnumMap<>(RotaHudAnchor.class);
        for (RotaHudAnchor anchor : RotaHudAnchor.values()) {
            anchorGroups.put(anchor, new ArrayList<>());
        }

        for (IRotaHudElement elem : elements) {
            String id = elem.getId();
            RotaCoreConfig.HudElementConfig cfg = (customConfigs != null) ? customConfigs.get(id) : null;

            if (cfg != null && cfg.customPosition) {
                // Posicionamento Livre (CUSTOM) com clamping na tela
                int w = Math.max(1, elem.getWidth());
                int h = Math.max(1, elem.getHeight());
                int clampedX = Math.max(0, Math.min(cfg.customX, Math.max(0, screenWidth - w)));
                int clampedY = Math.max(0, Math.min(cfg.customY, Math.max(0, screenHeight - h)));

                results.put(id, new HudLayoutResult(id, clampedX, clampedY, w, h, RotaHudAnchor.CUSTOM));
            } else {
                RotaHudAnchor anchor = elem.getDefaultAnchor();
                if (cfg != null && cfg.anchor != null && !cfg.anchor.equals("DEFAULT") && !cfg.anchor.equals("CUSTOM")) {
                    anchor = RotaHudAnchor.fromString(cfg.anchor);
                }
                anchorGroups.get(anchor).add(elem);
            }
        }

        // Processa cada região de ancoragem com empilhamento inteligente anti-colisão
        for (Map.Entry<RotaHudAnchor, List<IRotaHudElement>> entry : anchorGroups.entrySet()) {
            RotaHudAnchor anchor = entry.getKey();
            List<IRotaHudElement> group = entry.getValue();
            if (group.isEmpty()) continue;

            // Ordena determinísticamente: prioridade ascendente, seguido por ID lexicográfico
            group.sort(Comparator.comparingInt(IRotaHudElement::getPriority).thenComparing(IRotaHudElement::getId));

            if (anchor.isBottomAnchored()) {
                // Empilhamento de baixo para cima (Bottom-Up)
                // O primeiro elemento (menor prioridade) ancora na base, os subsequentes sobem
                int currentBottomY = screenHeight;
                boolean isFirst = true;

                for (IRotaHudElement elem : group) {
                    int w = elem.getWidth();
                    int h = elem.getHeight();
                    int marginX = elem.getMarginX();
                    int marginY = elem.getMarginY();

                    int x = anchor.calculateBaseX(screenWidth, w, marginX);
                    int y;

                    if (isFirst) {
                        y = screenHeight - marginY - h;
                        currentBottomY = y - DEFAULT_GAP;
                        isFirst = false;
                    } else {
                        y = currentBottomY - h;
                        currentBottomY = y - DEFAULT_GAP;
                    }

                    // Clamping de segurança
                    x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
                    y = Math.max(0, y);

                    results.put(elem.getId(), new HudLayoutResult(elem.getId(), x, y, w, h, anchor));
                }
            } else if (anchor == RotaHudAnchor.MIDDLE_LEFT || anchor == RotaHudAnchor.CENTER || anchor == RotaHudAnchor.MIDDLE_RIGHT) {
                // Elementos centralizados verticalmente: calcula a altura total do grupo para centralizar o conjunto
                int totalHeight = 0;
                for (int i = 0; i < group.size(); i++) {
                    totalHeight += group.get(i).getHeight();
                    if (i < group.size() - 1) totalHeight += DEFAULT_GAP;
                }

                int currentY = Math.max(0, (screenHeight - totalHeight) / 2);

                for (IRotaHudElement elem : group) {
                    int w = elem.getWidth();
                    int h = elem.getHeight();
                    int marginX = elem.getMarginX();

                    int x = anchor.calculateBaseX(screenWidth, w, marginX);
                    int y = currentY;
                    currentY += h + DEFAULT_GAP;

                    x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
                    y = Math.max(0, Math.min(y, Math.max(0, screenHeight - h)));

                    results.put(elem.getId(), new HudLayoutResult(elem.getId(), x, y, w, h, anchor));
                }
            } else {
                // Empilhamento de cima para baixo (Top-Down) para TOP_LEFT, TOP_CENTER e TOP_RIGHT
                int currentTopY = 0;
                boolean isFirst = true;

                for (IRotaHudElement elem : group) {
                    int w = elem.getWidth();
                    int h = elem.getHeight();
                    int marginX = elem.getMarginX();
                    int marginY = elem.getMarginY();

                    int x = anchor.calculateBaseX(screenWidth, w, marginX);
                    int y;

                    if (isFirst) {
                        y = marginY;
                        currentTopY = y + h + DEFAULT_GAP;
                        isFirst = false;
                    } else {
                        y = currentTopY;
                        currentTopY = y + h + DEFAULT_GAP;
                    }

                    x = Math.max(0, Math.min(x, Math.max(0, screenWidth - w)));
                    y = Math.max(0, Math.min(y, Math.max(0, screenHeight - h)));

                    results.put(elem.getId(), new HudLayoutResult(elem.getId(), x, y, w, h, anchor));
                }
            }
        }

        return results;
    }

    /**
     * Calcula o layout atual apenas para elementos visíveis registrados no RotaHudRegistry.
     */
    public static Map<String, HudLayoutResult> calculateCurrentLayout(int screenWidth, int screenHeight) {
        return calculateLayout(screenWidth, screenHeight, RotaHudRegistry.getVisibleElements(), RotaCore.getConfig().hudElements);
    }

    /**
     * Calcula o layout atual para todos os elementos (visíveis ou não), utilizado para edição no sandbox.
     */
    public static Map<String, HudLayoutResult> calculateAllLayout(int screenWidth, int screenHeight) {
        return calculateLayout(screenWidth, screenHeight, RotaHudRegistry.getAllElements(), RotaCore.getConfig().hudElements);
    }

    /**
     * Obtém as coordenadas calculadas de um elemento específico.
     */
    public static HudLayoutResult getBounds(String id, int screenWidth, int screenHeight) {
        if (id == null) return null;
        IRotaHudElement elem = RotaHudRegistry.getElement(id);
        if (elem == null) return null;
        Map<String, HudLayoutResult> layout = calculateCurrentLayout(screenWidth, screenHeight);
        return layout.get(id);
    }

    /**
     * Executa a renderização centralizada de todos os elementos HUD registrados e visíveis.
     * Chamado pelo ouvinte global de RenderGuiEvent.Post.
     */
    public static void renderAll(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;
        if (mc.screen instanceof RotaHudEditScreen) return;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        List<IRotaHudElement> visibleElements = RotaHudRegistry.getVisibleElements();
        if (visibleElements.isEmpty()) return;

        Map<String, HudLayoutResult> layout = calculateLayout(screenWidth, screenHeight, visibleElements, RotaCore.getConfig().hudElements);

        for (IRotaHudElement elem : visibleElements) {
            HudLayoutResult bounds = layout.get(elem.getId());
            if (bounds != null) {
                try {
                    elem.render(graphics, bounds.x(), bounds.y(), bounds.width(), bounds.height(), partialTick);
                } catch (Exception e) {
                    LOGGER.error("Falha ao renderizar HUD '{}': {}", elem.getId(), e.getMessage());
                }
            }
        }
    }
}
