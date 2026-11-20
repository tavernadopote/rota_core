/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.naming;

public enum RouteNamingStyle {
    FANTASY_KINGDOM("Fantasia / Reinos", "Nomes medievais imersivos como Reinos, Baronias e Estradas Reais."),
    NUMERIC_SECTOR("Numerico / Setorial", "Nomenclatura tatica e ordenada, como Zona 01, Rota 04 e Setor A."),
    GEOGRAPHIC("Geografico / Topografico", "Nomes descritivos baseados em relevo, biomas e pontos cardeais."),
    DISABLED("Desativado", "Sem geracao de nomes procedurais para rotas ou provincias.");

    private final String displayName;
    private final String description;

    private RouteNamingStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public RouteNamingStyle next() {
        RouteNamingStyle[] values = RouteNamingStyle.values();
        return values[(this.ordinal() + 1) % values.length];
    }
}

