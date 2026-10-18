package com.rotacore.api;

import com.rotacore.client.gui.RotaModuleCategoriesScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Interface que todo mod da suíte Rota Mods (como Rota Roads e Rota Quests)
 * implementa para se registrar no Rota Hub e compartilhar recursos universais.
 */
public interface RotaModule {

    /**
     * Identificador único do módulo (ex: "rota_roads", "rota_quests").
     */
    String getId();

    /**
     * Nome legível do módulo exibido nas abas e títulos da interface.
     */
    Component getDisplayName();

    /**
     * Versão instalada do módulo.
     */
    String getVersion();

    /**
     * Descrição resumida da funcionalidade do módulo.
     */
    Component getDescription();

    /**
     * Lista de categorias de configuração do módulo.
     */
    default List<RotaCategory> getCategories() {
        return List.of();
    }

    /**
     * Cria e retorna a tela de configurações do módulo (sem depender de mods terceiros como Cloth Config).
     * Por padrão, se o módulo definir getCategories(), cria automaticamente uma tela de grade de categorias.
     *
     * @param parent Tela pai para onde retornar ao fechar.
     * @return Tela de configuração nativa.
     */
    default Screen createConfigScreen(Screen parent) {
        return new RotaModuleCategoriesScreen(parent, this, getCategories(), null);
    }

    /**
     * Renderização personalizada do cartão ou painel de status deste módulo no painel principal do Hub.
     */
    default void renderDashboard(GuiGraphics graphics, int x, int y, int width, int height, Font font) {
        graphics.drawString(font, getDisplayName(), x + 10, y + 10, 0xFFFFAA00);
        graphics.drawString(font, Component.translatable("gui.rota_core.module_version", getVersion()), x + 10, y + 25, 0xFFAAAAAA);
        graphics.drawString(font, getDescription(), x + 10, y + 40, 0xFFDDDDDD);
    }

    /**
     * Ordem de exibição da aba (menor valor = mais à esquerda).
     */
    default int getOrder() {
        return 100;
    }
}
