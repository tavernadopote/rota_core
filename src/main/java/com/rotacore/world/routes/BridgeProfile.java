package com.rotacore.world.routes;

public enum BridgeProfile {
    FLAT("Plano (Reto)"),
    ARCH("Em Arco Vertical");

    private final String displayName;

    BridgeProfile(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BridgeProfile next() {
        BridgeProfile[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
