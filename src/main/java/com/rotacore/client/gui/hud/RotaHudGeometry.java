package com.rotacore.client.gui.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitários matemáticos e de renderização geométrica para viewports e HUDs.
 * Fornece cálculo de scanlines circulares, teste euclidiano de contenção,
 * desenho vetorial e molduras técnicas (sólidas e pontilhadas).
 */
public final class RotaHudGeometry {

    public static final int CIRCLE_SLICES = 64;
    public static final float[] CIRCLE_COS = new float[CIRCLE_SLICES + 1];
    public static final float[] CIRCLE_SIN = new float[CIRCLE_SLICES + 1];

    static {
        for (int i = 0; i <= CIRCLE_SLICES; i++) {
            double angle = (2.0 * Math.PI * i) / CIRCLE_SLICES;
            CIRCLE_COS[i] = (float) Math.cos(angle);
            CIRCLE_SIN[i] = (float) Math.sin(angle);
        }
    }

    private RotaHudGeometry() {}

    /**
     * Segmento horizontal de varredura (scanline) delimitador.
     */
    public record Scanline(int x1, int y1, int x2, int y2) {
        public int width() {
            return x2 - x1;
        }

        public int height() {
            return y2 - y1;
        }
    }

    /**
     * Caixa delimitadora retangular com utilitários de centro e raio.
     */
    public record HudBounds(int x, int y, int width, int height) {
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
     * Calcula as scanlines para mascaramento circular com passo customizado.
     */
    public static List<Scanline> calculateCircularScanlines(int x, int y, int size, float cx, float cy, float radius, int step) {
        if (step <= 0) step = 1;
        List<Scanline> scanlines = new ArrayList<>();
        for (int sy = y; sy < y + size; sy += step) {
            float dy = (sy + step / 2.0f) - cy;
            if (Math.abs(dy) >= radius) continue;
            float halfW = (float) Math.sqrt(radius * radius - dy * dy);
            int x1 = (int) Math.round(cx - halfW);
            int x2 = (int) Math.round(cx + halfW);
            int y2 = Math.min(y + size, sy + step);
            if (x2 > x1 && y2 > sy) {
                scanlines.add(new Scanline(x1, sy, x2, y2));
            }
        }
        return scanlines;
    }

    /**
     * Sobrecarga facilitadora para cálculo de scanlines circulares a partir da caixa delimitadora.
     */
    public static List<Scanline> calculateCircularScanlines(int x, int y, int size, int step) {
        float cx = x + size / 2.0f;
        float cy = y + size / 2.0f;
        float radius = size / 2.0f;
        return calculateCircularScanlines(x, y, size, cx, cy, radius, step);
    }

    /**
     * Verifica se o ponto (px, py) está contido no círculo euclidiano delimitado por (cx, cy) e raio.
     */
    public static boolean isInsideCircle(double px, double py, double cx, double cy, double radius) {
        double dx = px - cx;
        double dy = py - cy;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    /**
     * Verifica se o ponto (px, py) está contido na geometria da viewport especificada.
     */
    public static boolean isInsideShape(double px, double py, int x, int y, int width, int height, RotaViewportShape shape) {
        if (shape == RotaViewportShape.CIRCLE) {
            float cx = x + width / 2.0f;
            float cy = y + height / 2.0f;
            float radius = Math.min(width, height) / 2.0f;
            return isInsideCircle(px, py, cx, cy, radius);
        } else {
            return px >= x && px <= (x + width) && py >= y && py <= (y + height);
        }
    }

    /**
     * Renderiza o fundo mascarado da viewport respeitando o formato especificado.
     */
    public static void renderMaskedBackground(GuiGraphics graphics, int x, int y, int width, int height, RotaViewportShape shape, int bgColor) {
        if (shape == RotaViewportShape.CIRCLE) {
            int size = Math.min(width, height);
            float cx = x + width / 2.0f;
            float cy = y + height / 2.0f;
            float radius = size / 2.0f;
            List<Scanline> scanlines = calculateCircularScanlines(x, y, size, cx, cy, radius, 1);
            for (Scanline s : scanlines) {
                graphics.fill(s.x1(), s.y1(), s.x2(), s.y2(), bgColor);
            }
        } else {
            graphics.fill(x, y, x + width, y + height, bgColor);
        }
    }

    /**
     * Renderiza a moldura de contorno da viewport de acordo com o formato (CIRCLE ou SQUARE).
     */
    public static void renderFrame(GuiGraphics graphics, int x, int y, int width, int height, RotaViewportShape shape, int borderColor, float thickness) {
        if (shape == RotaViewportShape.CIRCLE) {
            int size = Math.min(width, height);
            float cx = x + width / 2.0f;
            float cy = y + height / 2.0f;
            float radius = size / 2.0f;
            for (int i = 0; i < CIRCLE_SLICES; i++) {
                float x1 = cx + CIRCLE_COS[i] * radius;
                float y1 = cy + CIRCLE_SIN[i] * radius;
                float x2 = cx + CIRCLE_COS[i + 1] * radius;
                float y2 = cy + CIRCLE_SIN[i + 1] * radius;
                drawLine(graphics, x1, y1, x2, y2, thickness, borderColor);
            }
        } else {
            int t = Math.max(1, Math.round(thickness));
            if (t <= 1) {
                graphics.renderOutline(x, y, width, height, borderColor);
            } else {
                for (int i = 0; i < t; i++) {
                    graphics.renderOutline(x + i, y + i, width - 2 * i, height - 2 * i, borderColor);
                }
            }
        }
    }

    /**
     * Renderiza uma moldura com efeito tracejado/pontilhado em volta do retângulo.
     * Ideal para estados de edição e seleção no sandbox de HUD.
     */
    public static void renderDottedFrame(GuiGraphics graphics, int x, int y, int width, int height, int color, int dashLength, int gapLength) {
        if (dashLength <= 0) dashLength = 4;
        if (gapLength <= 0) gapLength = 3;
        int step = dashLength + gapLength;

        // Borda superior
        for (int px = x; px < x + width; px += step) {
            int segEnd = Math.min(x + width, px + dashLength);
            graphics.fill(px, y, segEnd, y + 1, color);
        }
        // Borda inferior
        for (int px = x; px < x + width; px += step) {
            int segEnd = Math.min(x + width, px + dashLength);
            graphics.fill(px, y + height - 1, segEnd, y + height, color);
        }
        // Borda esquerda
        for (int py = y; py < y + height; py += step) {
            int segEnd = Math.min(y + height, py + dashLength);
            graphics.fill(x, py, x + 1, segEnd, color);
        }
        // Borda direita
        for (int py = y; py < y + height; py += step) {
            int segEnd = Math.min(y + height, py + dashLength);
            graphics.fill(x + width - 1, py, x + width, segEnd, color);
        }
    }

    /**
     * Desenha uma linha vetorial 2D arbitrária com espessura e cor.
     */
    public static void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Otimização para linhas horizontais
        if (Math.abs(dy) < 0.01f) {
            float minX = Math.min(x1, x2);
            float maxX = Math.max(x1, x2);
            int xA = Math.round(minX);
            int xB = Math.round(maxX);
            if (xB <= xA) return;

            int y = Math.round(y1);
            if (thickness <= 1.0f) {
                graphics.fill(xA, y, xB + 1, y + 1, color);
            } else {
                int halfT = Math.max(1, Math.round(thickness / 2.0f));
                graphics.fill(xA, y - halfT, xB + 1, y + halfT, color);
            }
            return;
        }

        // Otimização para linhas verticais
        if (Math.abs(dx) < 0.01f) {
            float minY = Math.min(y1, y2);
            float maxY = Math.max(y1, y2);
            int yA = Math.round(minY);
            int yB = Math.round(maxY);
            if (yB <= yA) return;

            int x = Math.round(x1);
            if (thickness <= 1.0f) {
                graphics.fill(x, yA, x + 1, yB + 1, color);
            } else {
                int halfT = Math.max(1, Math.round(thickness / 2.0f));
                graphics.fill(x - halfT, yA, x + halfT, yB + 1, color);
            }
            return;
        }

        // Linha angular com rotação de PoseStack
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.5f) return;

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1, y1, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));

        int l = (int) Math.ceil(len);
        if (thickness <= 1.0f) {
            graphics.fill(0, 0, l, 1, color);
        } else {
            int halfT = Math.max(1, Math.round(thickness / 2.0f));
            graphics.fill(0, -halfT, l, halfT, color);
        }
        pose.popPose();
    }
}
