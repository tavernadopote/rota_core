/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.client.gui.hud;

public enum RotaHudAnchor {
    TOP_LEFT("gui.rota_core.anchor.top_left", false, true),
    TOP_CENTER("gui.rota_core.anchor.top_center", false, true),
    TOP_RIGHT("gui.rota_core.anchor.top_right", false, true),
    MIDDLE_LEFT("gui.rota_core.anchor.middle_left", false, false),
    CENTER("gui.rota_core.anchor.center", false, false),
    MIDDLE_RIGHT("gui.rota_core.anchor.middle_right", false, false),
    BOTTOM_LEFT("gui.rota_core.anchor.bottom_left", true, false),
    BOTTOM_CENTER("gui.rota_core.anchor.bottom_center", true, false),
    BOTTOM_RIGHT("gui.rota_core.anchor.bottom_right", true, false),
    CUSTOM("gui.rota_core.anchor.custom", false, false);

    private final String translationKey;
    private final boolean bottomAnchored;
    private final boolean topAnchored;

    private RotaHudAnchor(String translationKey, boolean bottomAnchored, boolean topAnchored) {
        this.translationKey = translationKey;
        this.bottomAnchored = bottomAnchored;
        this.topAnchored = topAnchored;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }

    public boolean isBottomAnchored() {
        return this.bottomAnchored;
    }

    public boolean isTopAnchored() {
        return this.topAnchored;
    }

    public int calculateBaseX(int screenWidth, int elementWidth, int marginX) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 3, 6 -> marginX;
            case 1, 4, 7 -> Math.max(0, (screenWidth - elementWidth) / 2);
            case 2, 5, 8 -> Math.max(0, screenWidth - marginX - elementWidth);
            case 9 -> marginX;
        };
    }

    public int calculateBaseY(int screenHeight, int elementHeight, int marginY) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1, 2 -> marginY;
            case 3, 4, 5 -> Math.max(0, (screenHeight - elementHeight) / 2);
            case 6, 7, 8 -> Math.max(0, screenHeight - marginY - elementHeight);
            case 9 -> marginY;
        };
    }

    public static RotaHudAnchor fromString(String name) {
        if (name == null) {
            return TOP_RIGHT;
        }
        try {
            return RotaHudAnchor.valueOf(name.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return TOP_RIGHT;
        }
    }
}

