/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.zone;

public enum ZoneType {
    CIVIL_KINGDOM("Reino Civil", "Territorio civil colonizado com vilas, estradas conectadas e economia pacifica."),
    WILDERNESS("Territorio Selvagem", "Natureza intocada, relevo acidentado ou florestas densas sem assentamentos urbanos."),
    PILLAGER_DOMAIN("Dominio Hostil", "Regiao militarizada hostil dominada por saqueadores e postos avancados.");

    private final String displayName;
    private final String description;

    private ZoneType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isCivil() {
        return this == CIVIL_KINGDOM;
    }

    public boolean isWild() {
        return this == WILDERNESS;
    }

    public boolean isHostile() {
        return this == PILLAGER_DOMAIN;
    }
}

