/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.rotacore.api;

import com.rotacore.api.RotaModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotaCoreRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rota_core/registry");
    private static final Map<String, RotaModule> MODULES = new ConcurrentHashMap<String, RotaModule>();

    public static void register(RotaModule module) {
        if (module == null || module.getId() == null) {
            LOGGER.warn("Tentativa de registrar m\u00f3dulo nulo ou sem ID.");
            return;
        }
        MODULES.put(module.getId(), module);
        LOGGER.info("M\u00f3dulo Rota registrado com sucesso: {} (v{})", (Object)module.getId(), (Object)module.getVersion());
    }

    public static Optional<RotaModule> getModule(String id) {
        return Optional.ofNullable(MODULES.get(id));
    }

    public static List<RotaModule> getAllModules() {
        ArrayList<RotaModule> list = new ArrayList<RotaModule>(MODULES.values());
        list.sort(Comparator.comparingInt(RotaModule::getOrder));
        return Collections.unmodifiableList(list);
    }

    public static boolean hasModules() {
        return !MODULES.isEmpty();
    }
}

