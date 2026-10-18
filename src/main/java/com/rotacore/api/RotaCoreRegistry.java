package com.rotacore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central e thread-safe de módulos da suíte Rota Mods.
 */
public class RotaCoreRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("rota_core/registry");
    private static final Map<String, RotaModule> MODULES = new ConcurrentHashMap<>();

    /**
     * Registra um módulo da Rota Mods no ecossistema central.
     *
     * @param module Módulo implementado.
     */
    public static void register(RotaModule module) {
        if (module == null || module.getId() == null) {
            LOGGER.warn("Tentativa de registrar módulo nulo ou sem ID.");
            return;
        }
        MODULES.put(module.getId(), module);
        LOGGER.info("Módulo Rota registrado com sucesso: {} (v{})", module.getId(), module.getVersion());
    }

    /**
     * Retorna um módulo registrado pelo seu ID.
     */
    public static Optional<RotaModule> getModule(String id) {
        return Optional.ofNullable(MODULES.get(id));
    }

    /**
     * Retorna a lista imutável de todos os módulos registrados, ordenados por prioridade de aba.
     */
    public static List<RotaModule> getAllModules() {
        List<RotaModule> list = new ArrayList<>(MODULES.values());
        list.sort(Comparator.comparingInt(RotaModule::getOrder));
        return Collections.unmodifiableList(list);
    }

    /**
     * Informa se existem módulos registrados no momento.
     */
    public static boolean hasModules() {
        return !MODULES.isEmpty();
    }
}
