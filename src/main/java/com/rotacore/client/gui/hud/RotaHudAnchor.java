package com.rotacore.client.gui.hud;

/**
 * Âncoras regionais de tela e regras de posicionamento para elementos HUD.
 * Fornece coordenadas base de alinhamento para as 9 regiões clássicas e suporte a coordenadas personalizadas.
 */
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

    RotaHudAnchor(String translationKey, boolean bottomAnchored, boolean topAnchored) {
        this.translationKey = translationKey;
        this.bottomAnchored = bottomAnchored;
        this.topAnchored = topAnchored;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }

    public boolean isBottomAnchored() {
        return bottomAnchored;
    }

    public boolean isTopAnchored() {
        return topAnchored;
    }

    /**
     * Calcula a coordenada X base deste ponto de ancoragem para um elemento com a largura especificada.
     */
    public int calculateBaseX(int screenWidth, int elementWidth, int marginX) {
        return switch (this) {
            case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> marginX;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> Math.max(0, (screenWidth - elementWidth) / 2);
            case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> Math.max(0, screenWidth - marginX - elementWidth);
            case CUSTOM -> marginX;
        };
    }

    /**
     * Calcula a coordenada Y base deste ponto de ancoragem para um elemento com a altura especificada.
     */
    public int calculateBaseY(int screenHeight, int elementHeight, int marginY) {
        return switch (this) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> marginY;
            case MIDDLE_LEFT, CENTER, MIDDLE_RIGHT -> Math.max(0, (screenHeight - elementHeight) / 2);
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> Math.max(0, screenHeight - marginY - elementHeight);
            case CUSTOM -> marginY;
        };
    }

    public static RotaHudAnchor fromString(String name) {
        if (name == null) return TOP_RIGHT;
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TOP_RIGHT;
        }
    }
}
