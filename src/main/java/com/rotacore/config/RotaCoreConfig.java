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
     */
    public boolean enableControlledWorldgen = true;

    /**
     * Tamanho da célula regional em chunks (padrão: 48 chunks = 768 blocos).
     */
    public int regionalCellSizeChunks = 48;

    /**
     * Distância mínima entre vilas/assentamentos em blocos (padrão: 700).
     */
    public int minVillageSeparationBlocks = 700;

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

    public void validate() {
        if (moduleOptionsEnabled == null) {
            moduleOptionsEnabled = new LinkedHashMap<>();
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
