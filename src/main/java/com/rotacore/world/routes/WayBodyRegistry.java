package com.rotacore.world.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WayBodyRegistry {
    private static final Map<WayBodyType, WayBodyConfig> CONFIGS = new EnumMap<>(WayBodyType.class);
    private static final Map<String, WayBodyConfig> CUSTOM_CONFIGS = new ConcurrentHashMap<>();

    static {
        resetToDefaults();
    }

    public static void resetToDefaults() {
        CONFIGS.clear();
        CUSTOM_CONFIGS.clear();

        // 1. Estrada (ROAD) - Cinza Asfalto / Rodovia
        CONFIGS.put(WayBodyType.ROAD, new WayBodyConfig(
                WayBodyType.ROAD, 32, 400, 3, 4, 0, true,
                WayBodyType.ROAD.getDefaultMapColor(),
                true, 3, 0.3, 0, false, false,
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.ROAD, true, false)
                )
        ));

        // 2. Rota Maritima (MARITIME) - Azul Nautico / Ciano (Pokemon Style: vão de 12 blocos, balizadores a cada 4)
        CONFIGS.put(WayBodyType.MARITIME, new WayBodyConfig(
                WayBodyType.MARITIME, 32, 500, 12, 0, 4, true,
                WayBodyType.MARITIME.getDefaultMapColor(),
                false, 0, 0.1, 0, false, false,
                List.of("minecraft:ocean", "minecraft:deep_ocean", "minecraft:warm_ocean", "minecraft:lukewarm_ocean",
                        "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:river"),
                Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.MARITIME, true, false),
                        new WayConnectionRule(WayBodyType.BRIDGE, true, true)
                )
        ));

        // 3. Tunel (TUNNEL) - Grafite Escuro / Subterraneo (Massa rochosa minima 6, escava solido e detecta cavernas)
        CONFIGS.put(WayBodyType.TUNNEL, new WayBodyConfig(
                WayBodyType.TUNNEL, 16, 180, 5, 4, 0, true,
                WayBodyType.TUNNEL.getDefaultMapColor(),
                false, 2, 0.2, 6, true, true,
                Collections.emptyList(),
                List.of("minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:beach"),
                List.of(
                        new WayConnectionRule(WayBodyType.TUNNEL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 4. Trilha (TRAIL) - Marrom Rustico / Terra Batida (Subida rustica 1 em 1 bloco, alta curvatura 0.8)
        CONFIGS.put(WayBodyType.TRAIL, new WayBodyConfig(
                WayBodyType.TRAIL, 16, 120, 2, 3, 0, true,
                WayBodyType.TRAIL.getDefaultMapColor(),
                false, 2, 0.8, 0, false, false,
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.TRAIL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 5. Ponte (BRIDGE) - Laranja / Cobre Estrutural (Quase reta 0.05, rampa suave, canCarveSolid desativado por padrao)
        CONFIGS.put(WayBodyType.BRIDGE, new WayBodyConfig(
                WayBodyType.BRIDGE, 10, 80, 3, 0, 0, false,
                WayBodyType.BRIDGE.getDefaultMapColor(),
                true, 4, 0.05, 0, false, true,
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.BRIDGE, true, true),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));
    }

    public static WayBodyConfig getConfig(WayBodyType type) {
        if (type == null) {
            return null;
        }
        return CONFIGS.computeIfAbsent(type, t -> new WayBodyConfig(
                t, t.getDefaultMinLength(), t.getDefaultMaxLength(), t.getDefaultWidth(),
                t.getDefaultClearanceHeight(), t.getDefaultBarrierSpacing(), t.isDefaultAllowBodyMagnetism(),
                t.getDefaultMapColor(), List.of()
        ));
    }

    public static void setConfig(WayBodyType type, WayBodyConfig config) {
        if (type != null && config != null) {
            CONFIGS.put(type, config);
        }
    }

    public static void registerCustomBody(String id, WayBodyConfig config) {
        if (id != null && config != null) {
            CUSTOM_CONFIGS.put(id.toLowerCase(), config);
        }
    }

    public static WayBodyConfig getCustomBody(String id) {
        return id != null ? CUSTOM_CONFIGS.get(id.toLowerCase()) : null;
    }

    public static List<WayBodyConfig> getAllConfigs() {
        List<WayBodyConfig> all = new ArrayList<>(CONFIGS.values());
        all.addAll(CUSTOM_CONFIGS.values());
        return Collections.unmodifiableList(all);
    }
}
