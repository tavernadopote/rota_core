package com.rotacore.client.gui.hud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro centralizado de elementos de HUD da suíte Rota Mods.
 * Permite que qualquer mod registre seus HUDs para posicionamento inteligente e edição visual unificada.
 */
public final class RotaHudRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("rota_core/hud_registry");
    private static final Map<String, IRotaHudElement> ELEMENTS = new ConcurrentHashMap<>();

    private RotaHudRegistry() {}

    /**
     * Registra um novo elemento HUD no sistema.
     */
    public static void register(IRotaHudElement element) {
        if (element == null) {
            LOGGER.warn("Tentativa de registrar elemento de HUD nulo ignorada.");
            return;
        }
        if (element.getId() == null || element.getId().isBlank()) {
            LOGGER.warn("Tentativa de registrar elemento de HUD com ID vazio ignorada.");
            return;
        }
        ELEMENTS.put(element.getId(), element);
        LOGGER.info("Elemento de HUD registrado com sucesso: {} (Âncora padrão: {})", element.getId(), element.getDefaultAnchor());
    }

    /**
     * Remove um elemento HUD registrado pelo seu ID.
     */
    public static void unregister(String id) {
        if (id != null) {
            IRotaHudElement removed = ELEMENTS.remove(id);
            if (removed != null) {
                LOGGER.info("Elemento de HUD removido: {}", id);
            }
        }
    }

    /**
     * Obtém um elemento de HUD pelo seu ID ou null se não registrado.
     */
    public static IRotaHudElement getElement(String id) {
        return id != null ? ELEMENTS.get(id) : null;
    }

    /**
     * Verifica se existe um elemento registrado com o ID fornecido.
     */
    public static boolean hasElement(String id) {
        return id != null && ELEMENTS.containsKey(id);
    }

    /**
     * Retorna uma coleção não modificável de todos os elementos HUD registrados.
     */
    public static Collection<IRotaHudElement> getAllElements() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    /**
     * Retorna a lista de elementos registrados que estão visíveis no momento.
     */
    public static List<IRotaHudElement> getVisibleElements() {
        List<IRotaHudElement> visible = new ArrayList<>();
        for (IRotaHudElement element : ELEMENTS.values()) {
            if (element.isVisible()) {
                visible.add(element);
            }
        }
        return visible;
    }

    /**
     * Retorna a contagem de elementos registrados.
     */
    public static int getCount() {
        return ELEMENTS.size();
    }

    /**
     * Limpa todos os elementos registrados (utilizado primariamente em testes).
     */
    public static void clear() {
        ELEMENTS.clear();
    }
}
