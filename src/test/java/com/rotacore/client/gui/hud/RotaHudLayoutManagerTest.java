package com.rotacore.client.gui.hud;

import com.rotacore.config.RotaCoreConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RotaHudLayoutManagerTest {

    private static class MockElement implements IRotaHudElement {
        private final String id;
        private final RotaHudAnchor anchor;
        private final int width;
        private final int height;
        private final int priority;

        MockElement(String id, RotaHudAnchor anchor, int width, int height, int priority) {
            this.id = id;
            this.anchor = anchor;
            this.width = width;
            this.height = height;
            this.priority = priority;
        }

        @Override public String getId() { return id; }
        @Override public Component getDisplayName() { return Component.literal(id); }
        @Override public RotaHudAnchor getDefaultAnchor() { return anchor; }
        @Override public RotaViewportShape getShape() { return RotaViewportShape.SQUARE; }
        @Override public int getWidth() { return width; }
        @Override public int getHeight() { return height; }
        @Override public boolean isVisible() { return true; }
        @Override public int getPriority() { return priority; }
        @Override public void render(GuiGraphics graphics, int x, int y, int width, int height, float partialTick) {}
    }

    @Test
    public void testBottomLeftAntiCollisionStacking() {
        int screenW = 800;
        int screenH = 600;

        // 3 elementos na mesma âncora BOTTOM_LEFT
        MockElement elem1 = new MockElement("elem1", RotaHudAnchor.BOTTOM_LEFT, 100, 80, 0);
        MockElement elem2 = new MockElement("elem2", RotaHudAnchor.BOTTOM_LEFT, 120, 50, 1);
        MockElement elem3 = new MockElement("elem3", RotaHudAnchor.BOTTOM_LEFT, 90, 40, 2);

        List<IRotaHudElement> elements = List.of(elem1, elem2, elem3);
        Map<String, RotaHudLayoutManager.HudLayoutResult> layout = RotaHudLayoutManager.calculateLayout(
                screenW, screenH, elements, null
        );

        RotaHudLayoutManager.HudLayoutResult r1 = layout.get("elem1");
        RotaHudLayoutManager.HudLayoutResult r2 = layout.get("elem2");
        RotaHudLayoutManager.HudLayoutResult r3 = layout.get("elem3");

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);

        // elem1 é prioridade 0, deve estar na base do canto inferior esquerdo
        // y1 = 600 - 6 (margin) - 80 = 514
        assertEquals(elem1.getMarginX(), r1.x());
        assertEquals(screenH - elem1.getMarginY() - elem1.getHeight(), r1.y());

        // elem2 deve estar empilhado ACIMA de elem1 com gap de 6 pixels
        // y2 = y1 - gap (6) - 50 = 514 - 6 - 50 = 458
        assertEquals(elem2.getMarginX(), r2.x());
        assertEquals(r1.y() - RotaHudLayoutManager.DEFAULT_GAP - elem2.getHeight(), r2.y());

        // elem3 deve estar empilhado ACIMA de elem2 com gap de 6 pixels
        // y3 = y2 - gap (6) - 40 = 458 - 6 - 40 = 412
        assertEquals(elem3.getMarginX(), r3.x());
        assertEquals(r2.y() - RotaHudLayoutManager.DEFAULT_GAP - elem3.getHeight(), r3.y());

        // Verificação matemática rigorosa de anti-colisão (nenhum retângulo se sobrepõe)
        assertFalse(rectanglesOverlap(r1, r2));
        assertFalse(rectanglesOverlap(r1, r3));
        assertFalse(rectanglesOverlap(r2, r3));
    }

    @Test
    public void testTopRightAntiCollisionStacking() {
        int screenW = 800;
        int screenH = 600;

        // 2 elementos na mesma âncora TOP_RIGHT (ex: Minimapa e Contador de Velocidade)
        MockElement minimap = new MockElement("rota_map:minimap", RotaHudAnchor.TOP_RIGHT, 120, 120, 0);
        MockElement speedo = new MockElement("rota_roads:speedometer", RotaHudAnchor.TOP_RIGHT, 100, 30, 1);

        Map<String, RotaHudLayoutManager.HudLayoutResult> layout = RotaHudLayoutManager.calculateLayout(
                screenW, screenH, List.of(minimap, speedo), null
        );

        RotaHudLayoutManager.HudLayoutResult rMap = layout.get("rota_map:minimap");
        RotaHudLayoutManager.HudLayoutResult rSpeed = layout.get("rota_roads:speedometer");

        assertNotNull(rMap);
        assertNotNull(rSpeed);

        // Minimapa fica no topo direito: x = 800 - 6 - 120 = 674, y = 6
        assertEquals(screenW - minimap.getMarginX() - minimap.getWidth(), rMap.x());
        assertEquals(minimap.getMarginY(), rMap.y());

        // Velocímetro deve empilhar ABAIXO do minimapa: y = 6 + 120 + 6 = 132
        assertEquals(screenW - speedo.getMarginX() - speedo.getWidth(), rSpeed.x());
        assertEquals(rMap.y() + rMap.height() + RotaHudLayoutManager.DEFAULT_GAP, rSpeed.y());

        assertFalse(rectanglesOverlap(rMap, rSpeed));
    }

    @Test
    public void testCustomCoordinatesAndClamping() {
        int screenW = 800;
        int screenH = 600;

        MockElement customElem = new MockElement("custom_hud", RotaHudAnchor.TOP_LEFT, 100, 50, 0);

        Map<String, RotaCoreConfig.HudElementConfig> customMap = new HashMap<>();
        customMap.put("custom_hud", new RotaCoreConfig.HudElementConfig(true, 250, 350));

        Map<String, RotaHudLayoutManager.HudLayoutResult> layout = RotaHudLayoutManager.calculateLayout(
                screenW, screenH, List.of(customElem), customMap
        );

        RotaHudLayoutManager.HudLayoutResult result = layout.get("custom_hud");
        assertNotNull(result);
        assertEquals(250, result.x());
        assertEquals(350, result.y());
        assertEquals(RotaHudAnchor.CUSTOM, result.effectiveAnchor());

        // Teste de clamping: coordenadas além dos limites da tela são fixadas nas bordas
        customMap.put("custom_hud", new RotaCoreConfig.HudElementConfig(true, 950, 750));
        layout = RotaHudLayoutManager.calculateLayout(screenW, screenH, List.of(customElem), customMap);
        result = layout.get("custom_hud");

        assertEquals(screenW - customElem.getWidth(), result.x());
        assertEquals(screenH - customElem.getHeight(), result.y());
    }

    private boolean rectanglesOverlap(RotaHudLayoutManager.HudLayoutResult a, RotaHudLayoutManager.HudLayoutResult b) {
        return a.x() < b.x() + b.width() &&
                a.x() + a.width() > b.x() &&
                a.y() < b.y() + b.height() &&
                a.y() + a.height() > b.y();
    }
}
