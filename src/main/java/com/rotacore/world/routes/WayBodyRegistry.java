package com.rotacore.world.routes;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WayBodyRegistry {
    private static final Map<WayBodyType, WayBodyConfig> CONFIGS = new EnumMap<>(WayBodyType.class);

    static {
        resetToDefaults();
    }

    public static void resetToDefaults() {
        CONFIGS.clear();

        // 1. Estrada (ROAD) - Cinza Asfalto / Rodovia
        CONFIGS.put(WayBodyType.ROAD, new WayBodyConfig(
                WayBodyType.ROAD, 32, 400, 3, 4, 0, true,
                WayBodyType.ROAD.getDefaultMapColor(),
                List.of(
                        new WayConnectionRule(WayBodyType.ROAD, true, false)
                )
        ));

        // 2. Rota Maritima (MARITIME) - Azul Nautico / Ciano (Pokemon Style: vão de 12 blocos, balizadores a cada 4)
        CONFIGS.put(WayBodyType.MARITIME, new WayBodyConfig(
                WayBodyType.MARITIME, 32, 500, 12, 0, 4, true,
                WayBodyType.MARITIME.getDefaultMapColor(),
                List.of(
                        new WayConnectionRule(WayBodyType.MARITIME, true, false),
                        new WayConnectionRule(WayBodyType.BRIDGE, true, true)
                )
        ));

        // 3. Tunel (TUNNEL) - Grafite Escuro / Subterraneo
        CONFIGS.put(WayBodyType.TUNNEL, new WayBodyConfig(
                WayBodyType.TUNNEL, 16, 180, 5, 4, 0, true,
                WayBodyType.TUNNEL.getDefaultMapColor(),
                List.of(
                        new WayConnectionRule(WayBodyType.TUNNEL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 4. Trilha (TRAIL) - Marrom Rustico / Terra Batida
        CONFIGS.put(WayBodyType.TRAIL, new WayBodyConfig(
                WayBodyType.TRAIL, 16, 120, 2, 3, 0, true,
                WayBodyType.TRAIL.getDefaultMapColor(),
                List.of(
                        new WayConnectionRule(WayBodyType.TRAIL, true, false),
                        new WayConnectionRule(WayBodyType.MARITIME, true, true)
                )
        ));

        // 5. Ponte (BRIDGE) - Laranja / Cobre Estrutural
        CONFIGS.put(WayBodyType.BRIDGE, new WayBodyConfig(
                WayBodyType.BRIDGE, 10, 80, 3, 0, 0, false,
                WayBodyType.BRIDGE.getDefaultMapColor(),
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
}
