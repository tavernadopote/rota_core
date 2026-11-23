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
                WayBodyType.ROAD, WayBodyType.ROAD.getDefaultMinLength(), WayBodyType.ROAD.getDefaultMaxLength(), WayBodyType.ROAD.getDefaultWidth(), WayBodyType.ROAD.getDefaultClearanceHeight(), WayBodyType.ROAD.getDefaultBarrierSpacing(), WayBodyType.ROAD.isDefaultAllowBodyMagnetism(),
                WayBodyType.ROAD.getDefaultMapColor(),
                WayBodyType.ROAD.isDefaultSmoothElevation(), WayBodyType.ROAD.getDefaultMinRunBlocksPerStep(), WayBodyType.ROAD.getDefaultHorizontalCurvatureWeight(), WayBodyType.ROAD.getDefaultMinOverheadRock(), WayBodyType.ROAD.isDefaultCanCarveSolid(), WayBodyType.ROAD.isDefaultDetectCaves(),
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.ROAD, true, false)
                )
        ));

        // 2. Rota Maritima (MARITIME) - Azul Nautico / Ciano (Pokemon Style: vão de 12 blocos, balizadores a cada 4)
        CONFIGS.put(WayBodyType.MARITIME, new WayBodyConfig(
                WayBodyType.MARITIME, WayBodyType.MARITIME.getDefaultMinLength(), WayBodyType.MARITIME.getDefaultMaxLength(), WayBodyType.MARITIME.getDefaultWidth(), WayBodyType.MARITIME.getDefaultClearanceHeight(), WayBodyType.MARITIME.getDefaultBarrierSpacing(), WayBodyType.MARITIME.isDefaultAllowBodyMagnetism(),
                WayBodyType.MARITIME.getDefaultMapColor(),
                WayBodyType.MARITIME.isDefaultSmoothElevation(), WayBodyType.MARITIME.getDefaultMinRunBlocksPerStep(), WayBodyType.MARITIME.getDefaultHorizontalCurvatureWeight(), WayBodyType.MARITIME.getDefaultMinOverheadRock(), WayBodyType.MARITIME.isDefaultCanCarveSolid(), WayBodyType.MARITIME.isDefaultDetectCaves(),
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
                WayBodyType.TUNNEL, WayBodyType.TUNNEL.getDefaultMinLength(), WayBodyType.TUNNEL.getDefaultMaxLength(), WayBodyType.TUNNEL.getDefaultWidth(), WayBodyType.TUNNEL.getDefaultClearanceHeight(), WayBodyType.TUNNEL.getDefaultBarrierSpacing(), WayBodyType.TUNNEL.isDefaultAllowBodyMagnetism(),
                WayBodyType.TUNNEL.getDefaultMapColor(),
                WayBodyType.TUNNEL.isDefaultSmoothElevation(), WayBodyType.TUNNEL.getDefaultMinRunBlocksPerStep(), WayBodyType.TUNNEL.getDefaultHorizontalCurvatureWeight(), WayBodyType.TUNNEL.getDefaultMinOverheadRock(), WayBodyType.TUNNEL.isDefaultCanCarveSolid(), WayBodyType.TUNNEL.isDefaultDetectCaves(),
                Collections.emptyList(),
                List.of("minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:beach"),
                List.of(
                        new WayConnectionRule(WayBodyType.TUNNEL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 4. Trilha (TRAIL) - Marrom Rustico / Terra Batida (Subida rustica 1 em 1 bloco, alta curvatura 0.8)
        CONFIGS.put(WayBodyType.TRAIL, new WayBodyConfig(
                WayBodyType.TRAIL, WayBodyType.TRAIL.getDefaultMinLength(), WayBodyType.TRAIL.getDefaultMaxLength(), WayBodyType.TRAIL.getDefaultWidth(), WayBodyType.TRAIL.getDefaultClearanceHeight(), WayBodyType.TRAIL.getDefaultBarrierSpacing(), WayBodyType.TRAIL.isDefaultAllowBodyMagnetism(),
                WayBodyType.TRAIL.getDefaultMapColor(),
                WayBodyType.TRAIL.isDefaultSmoothElevation(), WayBodyType.TRAIL.getDefaultMinRunBlocksPerStep(), WayBodyType.TRAIL.getDefaultHorizontalCurvatureWeight(), WayBodyType.TRAIL.getDefaultMinOverheadRock(), WayBodyType.TRAIL.isDefaultCanCarveSolid(), WayBodyType.TRAIL.isDefaultDetectCaves(),
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new WayConnectionRule(WayBodyType.TRAIL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 5. Ponte (BRIDGE) - Laranja / Cobre Estrutural (Quase reta 0.05, rampa suave, canCarveSolid desativado por padrao)
        CONFIGS.put(WayBodyType.BRIDGE, new WayBodyConfig(
                WayBodyType.BRIDGE, WayBodyType.BRIDGE.getDefaultMinLength(), WayBodyType.BRIDGE.getDefaultMaxLength(), WayBodyType.BRIDGE.getDefaultWidth(), WayBodyType.BRIDGE.getDefaultClearanceHeight(), WayBodyType.BRIDGE.getDefaultBarrierSpacing(), WayBodyType.BRIDGE.isDefaultAllowBodyMagnetism(),
                WayBodyType.BRIDGE.getDefaultMapColor(),
                WayBodyType.BRIDGE.isDefaultSmoothElevation(), WayBodyType.BRIDGE.getDefaultMinRunBlocksPerStep(), WayBodyType.BRIDGE.getDefaultHorizontalCurvatureWeight(), WayBodyType.BRIDGE.getDefaultMinOverheadRock(), WayBodyType.BRIDGE.isDefaultCanCarveSolid(), WayBodyType.BRIDGE.isDefaultDetectCaves(),
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
