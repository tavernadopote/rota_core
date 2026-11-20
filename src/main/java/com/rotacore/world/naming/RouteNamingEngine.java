/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.naming;

import com.rotacore.world.naming.RouteNamingStyle;
import java.util.Random;

public class RouteNamingEngine {
    private static final String[] FANTASY_TITLES = new String[]{"Reino de", "Baronia de", "Ducado de", "Condado de", "Principado de", "Marca de", "Grao-Ducado de", "Feudo de"};
    private static final String[] FANTASY_ROOTS = new String[]{"Aethelgard", "Solaria", "Valoria", "Florian", "Aldor", "Eldoria", "Brunwald", "Eisenheim", "Ravenspire", "Oakhaven", "Stormhold", "Sunridge", "Riverdell", "Silverpine", "Highgate", "Dawnstar", "Frostford", "Greenshire", "Redcliff", "Falconcrest", "Drakensberg", "Ironwood", "Wolfstone", "Goldcrest", "Shadowfen"};
    private static final String[] FANTASY_ROUTE_PREFIXES = new String[]{"Estrada Real", "Caminho do Rei", "Via Imperial", "Rota dos Mercadores", "Estrada da Alianca", "Trilha dos Pioneiros", "Passo Real", "Caminho das Carruagens"};
    private static final String[] GEOGRAPHIC_CARDINALS = new String[]{"do Norte", "do Sul", "do Leste", "do Oeste", "Central", "Oriental", "Ocidental", "Superior", "Inferior"};
    private static final String[] GEOGRAPHIC_LANDFORMS = new String[]{"Planicies", "Colinas", "Vales", "Terras Altas", "Campinas", "Bosques", "Costas", "Clareiras"};
    private static final String[] GEOGRAPHIC_ROUTE_PREFIXES = new String[]{"Estrada", "Caminho", "Rota", "Passo", "Via", "Trilha"};

    public static String generateZoneName(long seed, String zoneType, String biomeCategory, RouteNamingStyle style, int zoneIndex) {
        if (style == null || style == RouteNamingStyle.DISABLED) {
            return "";
        }
        Random random = new Random(seed ^ 0x9E3779B97F4A7C15L);
        switch (style) {
            case NUMERIC_SECTOR: {
                return String.format("Zona %02d", Math.abs(zoneIndex % 100));
            }
            case GEOGRAPHIC: {
                String landform = GEOGRAPHIC_LANDFORMS[random.nextInt(GEOGRAPHIC_LANDFORMS.length)];
                String cardinal = GEOGRAPHIC_CARDINALS[random.nextInt(GEOGRAPHIC_CARDINALS.length)];
                if (biomeCategory != null && !biomeCategory.isBlank()) {
                    String biomeDesc = RouteNamingEngine.formatBiomeDescriptor(biomeCategory);
                    return landform + " " + biomeDesc + " " + cardinal;
                }
                return landform + " " + cardinal;
            }
        }
        if ("PILLAGER_DOMAIN".equalsIgnoreCase(zoneType)) {
            String root = FANTASY_ROOTS[random.nextInt(FANTASY_ROOTS.length)];
            return "Dominio de " + root;
        }
        if ("WILDERNESS".equalsIgnoreCase(zoneType)) {
            String root = FANTASY_ROOTS[random.nextInt(FANTASY_ROOTS.length)];
            return "Terras Ermas de " + root;
        }
        String title = FANTASY_TITLES[random.nextInt(FANTASY_TITLES.length)];
        String root = FANTASY_ROOTS[random.nextInt(FANTASY_ROOTS.length)];
        return title + " " + root;
    }

    public static String generateRouteName(long seed, String routeTier, String fromZoneName, String toZoneName, RouteNamingStyle style, int routeIndex) {
        if (style == null || style == RouteNamingStyle.DISABLED) {
            return "";
        }
        Random random = new Random(seed ^ 0xBF58476D1CE4E5B9L);
        switch (style) {
            case NUMERIC_SECTOR: {
                return String.format("Rota %02d", Math.abs(routeIndex % 100));
            }
            case GEOGRAPHIC: {
                String geoPrefix = GEOGRAPHIC_ROUTE_PREFIXES[random.nextInt(GEOGRAPHIC_ROUTE_PREFIXES.length)];
                String geoCardinal = GEOGRAPHIC_CARDINALS[random.nextInt(GEOGRAPHIC_CARDINALS.length)];
                return geoPrefix + " " + geoCardinal;
            }
        }
        if (fromZoneName != null && !fromZoneName.isBlank() && toZoneName != null && !toZoneName.isBlank()) {
            String cleanFrom = RouteNamingEngine.stripHonorifics(fromZoneName);
            String cleanTo = RouteNamingEngine.stripHonorifics(toZoneName);
            return "Estrada de " + cleanFrom + " a " + cleanTo;
        }
        String fantasyPrefix = FANTASY_ROUTE_PREFIXES[random.nextInt(FANTASY_ROUTE_PREFIXES.length)];
        String root = FANTASY_ROOTS[random.nextInt(FANTASY_ROOTS.length)];
        return fantasyPrefix + " de " + root;
    }

    private static String stripHonorifics(String name) {
        if (name == null) {
            return "";
        }
        for (String title : FANTASY_TITLES) {
            if (!name.startsWith(title + " ")) continue;
            return name.substring((title + " ").length()).trim();
        }
        if (name.startsWith("Dominio de ")) {
            return name.substring("Dominio de ".length()).trim();
        }
        if (name.startsWith("Terras Ermas de ")) {
            return name.substring("Terras Ermas de ".length()).trim();
        }
        return name;
    }

    private static String formatBiomeDescriptor(String biomeCategory) {
        String lower = biomeCategory.toLowerCase();
        if (lower.contains("plains")) {
            return "dos Campos";
        }
        if (lower.contains("forest")) {
            return "da Floresta";
        }
        if (lower.contains("taiga")) {
            return "dos Pinhais";
        }
        if (lower.contains("mountain") || lower.contains("peak")) {
            return "da Cordilheira";
        }
        if (lower.contains("desert")) {
            return "das Dunas";
        }
        if (lower.contains("savanna")) {
            return "das Savanas";
        }
        if (lower.contains("swamp")) {
            return "do Pantano";
        }
        if (lower.contains("jungle")) {
            return "da Selva";
        }
        if (lower.contains("snow") || lower.contains("ice")) {
            return "das Geleiras";
        }
        if (lower.contains("badlands")) {
            return "dos Canions";
        }
        return "da Regiao";
    }
}

