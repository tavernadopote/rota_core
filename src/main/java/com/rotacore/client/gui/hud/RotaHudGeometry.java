/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.rotacore.client.gui.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rotacore.client.gui.hud.RotaViewportShape;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

public final class RotaHudGeometry {
    public static final int CIRCLE_SLICES = 64;
    public static final float[] CIRCLE_COS = new float[65];
    public static final float[] CIRCLE_SIN = new float[65];

    private RotaHudGeometry() {
    }

    public static List<Scanline> calculateCircularScanlines(int x, int y, int size, float cx, float cy, float radius, int step) {
        if (step <= 0) {
            step = 1;
        }
        ArrayList<Scanline> scanlines = new ArrayList<Scanline>();
        for (int sy = y; sy < y + size; sy += step) {
            float dy = (float)sy + (float)step / 2.0f - cy;
            if (Math.abs(dy) >= radius) continue;
            float halfW = (float)Math.sqrt(radius * radius - dy * dy);
            int x1 = Math.round(cx - halfW);
            int x2 = Math.round(cx + halfW);
            int y2 = Math.min(y + size, sy + step);
            if (x2 <= x1 || y2 <= sy) continue;
            scanlines.add(new Scanline(x1, sy, x2, y2));
        }
        return scanlines;
    }

    public static List<Scanline> calculateCircularScanlines(int x, int y, int size, int step) {
        float cx = (float)x + (float)size / 2.0f;
        float cy = (float)y + (float)size / 2.0f;
        float radius = (float)size / 2.0f;
        return RotaHudGeometry.calculateCircularScanlines(x, y, size, cx, cy, radius, step);
    }

    public static boolean isInsideCircle(double px, double py, double cx, double cy, double radius) {
        double dx = px - cx;
        double dy = py - cy;
        return dx * dx + dy * dy <= radius * radius;
    }

    public static boolean isInsideShape(double px, double py, int x, int y, int width, int height, RotaViewportShape shape) {
        if (shape == RotaViewportShape.CIRCLE) {
            float cx = (float)x + (float)width / 2.0f;
            float cy = (float)y + (float)height / 2.0f;
            float radius = (float)Math.min(width, height) / 2.0f;
            return RotaHudGeometry.isInsideCircle(px, py, cx, cy, radius);
        }
        return px >= (double)x && px <= (double)(x + width) && py >= (double)y && py <= (double)(y + height);
    }

    public static void renderMaskedBackground(GuiGraphics graphics, int x, int y, int width, int height, RotaViewportShape shape, int bgColor) {
        if (shape == RotaViewportShape.CIRCLE) {
            int size = Math.min(width, height);
            float cx = (float)x + (float)width / 2.0f;
            float cy = (float)y + (float)height / 2.0f;
            float radius = (float)size / 2.0f;
            List<Scanline> scanlines = RotaHudGeometry.calculateCircularScanlines(x, y, size, cx, cy, radius, 1);
            for (Scanline s : scanlines) {
                graphics.fill(s.x1(), s.y1(), s.x2(), s.y2(), bgColor);
            }
        } else {
            graphics.fill(x, y, x + width, y + height, bgColor);
        }
    }

    public static void renderFrame(GuiGraphics graphics, int x, int y, int width, int height, RotaViewportShape shape, int borderColor, float thickness) {
        if (shape == RotaViewportShape.CIRCLE) {
            int size = Math.min(width, height);
            float cx = (float)x + (float)width / 2.0f;
            float cy = (float)y + (float)height / 2.0f;
            float radius = (float)size / 2.0f;
            for (int i = 0; i < 64; ++i) {
                float x1 = cx + CIRCLE_COS[i] * radius;
                float y1 = cy + CIRCLE_SIN[i] * radius;
                float x2 = cx + CIRCLE_COS[i + 1] * radius;
                float y2 = cy + CIRCLE_SIN[i + 1] * radius;
                RotaHudGeometry.drawLine(graphics, x1, y1, x2, y2, thickness, borderColor);
            }
        } else {
            int t = Math.max(1, Math.round(thickness));
            if (t <= 1) {
                graphics.renderOutline(x, y, width, height, borderColor);
            } else {
                for (int i = 0; i < t; ++i) {
                    graphics.renderOutline(x + i, y + i, width - 2 * i, height - 2 * i, borderColor);
                }
            }
        }
    }

    public static void renderDottedFrame(GuiGraphics graphics, int x, int y, int width, int height, int color, int dashLength, int gapLength) {
        int py;
        int segEnd;
        int px;
        if (dashLength <= 0) {
            dashLength = 4;
        }
        if (gapLength <= 0) {
            gapLength = 3;
        }
        int step = dashLength + gapLength;
        for (px = x; px < x + width; px += step) {
            segEnd = Math.min(x + width, px + dashLength);
            graphics.fill(px, y, segEnd, y + 1, color);
        }
        for (px = x; px < x + width; px += step) {
            segEnd = Math.min(x + width, px + dashLength);
            graphics.fill(px, y + height - 1, segEnd, y + height, color);
        }
        for (py = y; py < y + height; py += step) {
            segEnd = Math.min(y + height, py + dashLength);
            graphics.fill(x, py, x + 1, segEnd, color);
        }
        for (py = y; py < y + height; py += step) {
            segEnd = Math.min(y + height, py + dashLength);
            graphics.fill(x + width - 1, py, x + width, segEnd, color);
        }
    }

    public static void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (Math.abs(dy) < 0.01f) {
            float minX = Math.min(x1, x2);
            float maxX = Math.max(x1, x2);
            int xA = Math.round(minX);
            int xB = Math.round(maxX);
            if (xB <= xA) {
                return;
            }
            int y = Math.round(y1);
            if (thickness <= 1.0f) {
                graphics.fill(xA, y, xB + 1, y + 1, color);
            } else {
                int halfT = Math.max(1, Math.round(thickness / 2.0f));
                graphics.fill(xA, y - halfT, xB + 1, y + halfT, color);
            }
            return;
        }
        if (Math.abs(dx) < 0.01f) {
            float minY = Math.min(y1, y2);
            float maxY = Math.max(y1, y2);
            int yA = Math.round(minY);
            int yB = Math.round(maxY);
            if (yB <= yA) {
                return;
            }
            int x = Math.round(x1);
            if (thickness <= 1.0f) {
                graphics.fill(x, yA, x + 1, yB + 1, color);
            } else {
                int halfT = Math.max(1, Math.round(thickness / 2.0f));
                graphics.fill(x - halfT, yA, x + halfT, yB + 1, color);
            }
            return;
        }
        float len = (float)Math.hypot(dx, dy);
        if (len < 0.5f) {
            return;
        }
        float angle = (float)Math.toDegrees(Math.atan2(dy, dx));
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1, y1, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        int l = (int)Math.ceil(len);
        if (thickness <= 1.0f) {
            graphics.fill(0, 0, l, 1, color);
        } else {
            int halfT = Math.max(1, Math.round(thickness / 2.0f));
            graphics.fill(0, -halfT, l, halfT, color);
        }
        pose.popPose();
    }

    static {
        for (int i = 0; i <= 64; ++i) {
            double angle = Math.PI * 2 * (double)i / 64.0;
            RotaHudGeometry.CIRCLE_COS[i] = (float)Math.cos(angle);
            RotaHudGeometry.CIRCLE_SIN[i] = (float)Math.sin(angle);
        }
    }

    public record Scanline(int x1, int y1, int x2, int y2) {
        public int width() {
            return this.x2 - this.x1;
        }

        public int height() {
            return this.y2 - this.y1;
        }
    }

    public record HudBounds(int x, int y, int width, int height) {
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

