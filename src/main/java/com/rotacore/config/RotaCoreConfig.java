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

    public void validate() {
        if (moduleOptionsEnabled == null) {
            moduleOptionsEnabled = new LinkedHashMap<>();
        }
    }
}
