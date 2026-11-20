/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.routes;

public enum RoadCurvatureMode {
    DIRECT("Direto / Reta", "Prioriza menor distancia com poucas curvas, aceitando maiores cortes e aterros."),
    BALANCED("Balanceado", "Equilibrio entre distancia e relevo, contornando montanhas altas e desfiladeiros."),
    SERPENTINE("Serpenteado / Organico", "Acompanhamento maximo de curvas de nivel, serpenteando morros com curvas em S.");

    private final String displayName;
    private final String description;

    private RoadCurvatureMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTranslationKey() {
        return "rota_core.config.curvature." + this.name().toLowerCase();
    }

    public String getDescriptionKey() {
        return "rota_core.config.curvature." + this.name().toLowerCase() + ".desc";
    }

    public RoadCurvatureMode next() {
        RoadCurvatureMode[] values = RoadCurvatureMode.values();
        return values[(this.ordinal() + 1) % values.length];
    }
}

