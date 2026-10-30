package com.rotacore.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configurações globais do Rota Core salvas em config/rota_core.json.
 * Controla a integração de botões na tela oficial de Opções e menu de pausa.
 */
public class RotaCoreConfig {

    /**
     * Exibe o botão Rota Mods no menu de pausa (ESC).
     */
    public boolean showPauseMenuButton = true;

    /**
     * Exibe o botão do Rota Mods Hub na tela oficial de Opções do Minecraft.
     */
    public boolean showHubInOptionsScreen = true;

    /**
     * Mapa de módulos e se seus botões individuais devem aparecer na tela oficial de Opções.
     * Módulos não presentes no mapa são considerados ativados por padrão (true).
     */
    public Map<String, Boolean> moduleOptionsEnabled = new LinkedHashMap<>();

    public boolean isModuleEnabledInOptions(String moduleId) {
        if (moduleId == null) return false;
        return moduleOptionsEnabled.getOrDefault(moduleId, true);
    }

    public void setModuleEnabledInOptions(String moduleId, boolean enabled) {
        if (moduleId != null) {
            moduleOptionsEnabled.put(moduleId, enabled);
        }
    }

    // =========================================================================
    // Zoneamento Regional e Geração de Estruturas Controlada
    // =========================================================================
    /**
     * Habilita a orquestração controlada de posicionamento de vilas e postos no mundo.
     * Desativado por padrão para preservar 100% da integridade da distribuição natural de biomas do Minecraft/CTOV.
     */
    public boolean enableControlledWorldgen = false;

    /**
     * Tamanho da célula regional em chunks (padrão: 38 chunks = ~608 blocos).
     */
    public int regionalCellSizeChunks = 38;

    /**
     * Distância mínima entre vilas/assentamentos em blocos (padrão: 500).
     */
    public int minVillageSeparationBlocks = 500;

    /**
     * Distância mínima entre um posto militar (pillager outpost) e a vila mais próxima em blocos (padrão: 450).
     */
    public int minOutpostFromVillageBlocks = 450;

    /**
     * Se verdadeiro, analisa a variação de relevo e move o ponto da vila para o vale/platô mais plano da região.
     */
    public boolean terrainSuitabilityCheck = true;

    /**
     * Variação máxima de altura tolerada em blocos para aprovar a localização de uma vila (padrão: 8).
     */
    public int maxVillageHeightVariance = 8;

    // =========================================================================
    // Gerenciamento de HUDs e Posicionamento Sandbox
    // =========================================================================

    public static class HudElementConfig {
        public boolean customPosition = false;
        public int customX = 0;
        public int customY = 0;
        public String anchor = "DEFAULT";
        public boolean visible = true;

        public HudElementConfig() {}

        public HudElementConfig(boolean customPosition, int customX, int customY) {
            this.customPosition = customPosition;
            this.customX = customX;
            this.customY = customY;
        }
    }

    public Map<String, HudElementConfig> hudElements = new LinkedHashMap<>();

    public HudElementConfig getHudElementConfig(String id) {
        if (id == null) return new HudElementConfig();
        if (hudElements == null) {
            hudElements = new LinkedHashMap<>();
        }
        return hudElements.computeIfAbsent(id, k -> new HudElementConfig());
    }

    public void setHudElementPosition(String id, int x, int y) {
        if (id == null) return;
        HudElementConfig cfg = getHudElementConfig(id);
        cfg.customPosition = true;
        cfg.customX = x;
        cfg.customY = y;
    }

    public void resetHudElementPosition(String id) {
        if (id == null) return;
        HudElementConfig cfg = getHudElementConfig(id);
        cfg.customPosition = false;
        cfg.customX = 0;
        cfg.customY = 0;
    }

    public void resetAllHudPositions() {
        if (hudElements != null) {
            hudElements.clear();
        }
    }

    public void validate() {
        if (moduleOptionsEnabled == null) {
            moduleOptionsEnabled = new LinkedHashMap<>();
        }
        if (hudElements == null) {
            hudElements = new LinkedHashMap<>();
        }
        for (Map.Entry<String, HudElementConfig> entry : hudElements.entrySet()) {
            if (entry.getValue() == null) {
                entry.setValue(new HudElementConfig());
            }
        }
        if (regionalCellSizeChunks < 24) regionalCellSizeChunks = 24;
        if (regionalCellSizeChunks > 128) regionalCellSizeChunks = 128;
        if (minVillageSeparationBlocks < 300) minVillageSeparationBlocks = 300;
        if (minVillageSeparationBlocks > 2000) minVillageSeparationBlocks = 2000;
        if (minOutpostFromVillageBlocks < 200) minOutpostFromVillageBlocks = 200;
        if (minOutpostFromVillageBlocks > 1000) minOutpostFromVillageBlocks = 1000;
        if (maxVillageHeightVariance < 4) maxVillageHeightVariance = 4;
        if (maxVillageHeightVariance > 24) maxVillageHeightVariance = 24;
    }
}
