/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.client.gui.hud;

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
        if (name == null) {
            return SQUARE;
        }
        try {
            return RotaViewportShape.valueOf(name.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return SQUARE;
        }
    }
}

