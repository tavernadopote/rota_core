package com.rotacore.client.gui.hud;

/**
 * Formatos geométricos de viewport suportados para renderização de HUDs.
 */
public enum RotaViewportShape {
    CIRCLE,
    SQUARE;

    public boolean isCircle() {
        return this == CIRCLE;
    }

    public boolean isSquare() {
        return this == SQUARE;
    }

    public static RotaViewportShape fromString(String name) {
        if (name == null) return SQUARE;
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SQUARE;
        }
    }
}
