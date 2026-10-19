package com.rotacore.world.zoning;

/**
 * Classificação funcional de zonas e estruturas no ecossistema Rota Mods.
 */
public enum ZoneType {
    /**
     * Assentamento humano / civil (vilas vanilla, cidades, capitais, vilas do CTOV).
     * Posicionado centralmente na célula regional em relevo plano e favorável.
     */
    SETTLEMENT,

    /**
     * Posto avançado militar hostil (pillager outposts, torres de vigia).
     * Posicionado na periferia / fronteira da célula regional, mantendo distância mínima de segurança das vilas.
     */
    OUTPOST,

    /**
     * Marcos de exploração e monumentos (templos do deserto, pirâmides da selva, cabanas de bruxa).
     */
    LANDMARK,

    /**
     * Estruturas marinhas, subterrâneas ou não gerenciadas pelo zoneamento macro.
     */
    UNRESTRICTED
}
