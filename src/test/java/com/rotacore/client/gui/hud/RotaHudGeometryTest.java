package com.rotacore.client.gui.hud;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RotaHudGeometryTest {

    @Test
    public void testIsInsideCircle() {
        double cx = 100.0;
        double cy = 100.0;
        double radius = 50.0;

        // Centro está estritamente dentro
        assertTrue(RotaHudGeometry.isInsideCircle(100.0, 100.0, cx, cy, radius));

        // Ponto interno
        assertTrue(RotaHudGeometry.isInsideCircle(120.0, 110.0, cx, cy, radius));

        // Borda exata
        assertTrue(RotaHudGeometry.isInsideCircle(150.0, 100.0, cx, cy, radius));

        // Ponto fora ao longo do eixo
        assertFalse(RotaHudGeometry.isInsideCircle(151.0, 100.0, cx, cy, radius));

        // Canto do quadrado delimitador (150, 150) -> distância = sqrt(50^2 + 50^2) ≈ 70.7 > 50
        assertFalse(RotaHudGeometry.isInsideCircle(150.0, 150.0, cx, cy, radius));
    }

    @Test
    public void testIsInsideShape() {
        int x = 20;
        int y = 30;
        int size = 100;

        // Teste de quadrado
        assertTrue(RotaHudGeometry.isInsideShape(20, 30, x, y, size, size, RotaViewportShape.SQUARE));
        assertTrue(RotaHudGeometry.isInsideShape(120, 130, x, y, size, size, RotaViewportShape.SQUARE));
        assertFalse(RotaHudGeometry.isInsideShape(121, 130, x, y, size, size, RotaViewportShape.SQUARE));

        // Teste de círculo: quina deve estar fora, centro dentro
        float cx = x + size / 2.0f; // 70
        float cy = y + size / 2.0f; // 80
        assertTrue(RotaHudGeometry.isInsideShape(cx, cy, x, y, size, size, RotaViewportShape.CIRCLE));
        assertFalse(RotaHudGeometry.isInsideShape(20, 30, x, y, size, size, RotaViewportShape.CIRCLE));
    }

    @Test
    public void testCalculateCircularScanlines() {
        int x = 0;
        int y = 0;
        int size = 100;
        float cx = 50.0f;
        float cy = 50.0f;
        float radius = 50.0f;

        List<RotaHudGeometry.Scanline> scanlines = RotaHudGeometry.calculateCircularScanlines(x, y, size, cx, cy, radius, 1);

        assertNotNull(scanlines);
        assertFalse(scanlines.isEmpty());
        // Deve cobrir quase todas as 100 linhas (exceto tangentes limítrofes onde abs(dy) >= radius)
        assertTrue(scanlines.size() >= 98);

        // A linha central (y = 50) deve ter largura próxima a 2 * raio (100)
        RotaHudGeometry.Scanline midLine = null;
        for (RotaHudGeometry.Scanline s : scanlines) {
            if (s.y1() == 50) {
                midLine = s;
                break;
            }
        }
        assertNotNull(midLine);
        assertEquals(50, midLine.y1());
        assertEquals(51, midLine.y2());
        assertTrue(midLine.width() >= 98 && midLine.width() <= 100);

        // Todas as scanlines devem estar estritamente dentro da caixa delimitadora
        for (RotaHudGeometry.Scanline s : scanlines) {
            assertTrue(s.x1() >= 0);
            assertTrue(s.x2() <= 100);
            assertTrue(s.y1() >= 0);
            assertTrue(s.y2() <= 100);
            assertTrue(s.x2() > s.x1());
        }
    }

    @Test
    public void testHudBoundsRecord() {
        RotaHudGeometry.HudBounds bounds = new RotaHudGeometry.HudBounds(10, 20, 100, 80);

        assertEquals(60.0f, bounds.cx(), 0.001f);
        assertEquals(60.0f, bounds.cy(), 0.001f);
        assertEquals(40.0f, bounds.radius(), 0.001f);

        assertTrue(bounds.contains(10, 20));
        assertTrue(bounds.contains(110, 100));
        assertTrue(bounds.contains(50, 50));
        assertFalse(bounds.contains(9, 20));
        assertFalse(bounds.contains(111, 50));
    }
}
