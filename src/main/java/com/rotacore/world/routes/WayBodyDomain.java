package com.rotacore.world.routes;

public enum WayBodyDomain {
    SURFACE("Superf\u00edcie (Estrada)"),
    UNDERGROUND("Subterr\u00e2neo (T\u00fanel)"),
    OVER_WATER("Sobre a \u00c1gua (Ponte)"),
    AQUATIC("Aqu\u00e1tico (Rota Mar\u00edtima)");

    private final String displayName;

    WayBodyDomain(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public WayBodyDomain next() {
        WayBodyDomain[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
