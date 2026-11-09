package com.rotacore.client.gui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Contrato universal para elementos de HUD registrados no ecossistema Rota Mods.
 * Define dimensões, geometria de viewport, ancoragem e método oficial de renderização cooperativa.
 */
public interface IRotaHudElement {

    /**
     * Identificador único deste HUD (ex: "rota_map:minimap").
     */
    String getId();

    /**
     * Nome amigável para exibição no sandbox e menus de configuração.
     */
    Component getDisplayName();

    /**
     * Ponto de ancoragem padrão sugerido pelo mod.
     */
    RotaHudAnchor getDefaultAnchor();

    /**
     * Formato geométrico da viewport (CIRCLE ou SQUARE).
     */
    RotaViewportShape getShape();

    /**
     * Largura atual da caixa delimitadora do HUD em pixels de interface.
     */
    int getWidth();

    /**
     * Altura atual da caixa delimitadora do HUD em pixels de interface.
     */
    int getHeight();

    /**
     * Determina se o elemento deve ser desenhado no frame atual.
     */
    boolean isVisible();

    /**
     * Renderiza o conteúdo do HUD nas coordenadas absolutas calculadas pelo layout manager.
     *
     * @param graphics    Contexto gráfico de renderização do Minecraft.
     * @param x           Coordenada X superior esquerda.
     * @param y           Coordenada Y superior esquerda.
     * @param width       Largura calculada da viewport.
     * @param height      Altura calculada da viewport.
     * @param partialTick Interpolação parcial de tick.
     */
    void render(GuiGraphics graphics, int x, int y, int width, int height, float partialTick);

    /**
     * Margem horizontal padrão (em pixels) em relação às bordas da tela.
     */
    default int getMarginX() {
        return 6;
    }

    /**
     * Margem vertical padrão (em pixels) em relação às bordas da tela.
     */
    default int getMarginY() {
        return 6;
    }

    /**
     * Prioridade de ordenação quando múltiplos elementos compartilham a mesma âncora (menores números vêm antes).
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Define se o usuário tem permissão para arrastar este HUD no sandbox visual.
     */
    default boolean isMovable() {
        return true;
    }

    /**
     * Determina se o elemento está em modo de pré-visualização (ex: durante edição no RotaHudEditScreen).
     * Permite que elementos condicionais (como o HUD da luneta) exibam dados simulados durante o reposicionamento.
     */
    default boolean isPreviewMode() {
        return false;
    }

    default void setPreviewMode(boolean preview) {
    }
}
