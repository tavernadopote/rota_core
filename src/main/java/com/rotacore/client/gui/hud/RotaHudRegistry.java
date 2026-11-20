/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.rotacore.client.gui.hud;

import com.rotacore.client.gui.hud.IRotaHudElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RotaHudRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rota_core/hud_registry");
    private static final Map<String, IRotaHudElement> ELEMENTS = new ConcurrentHashMap<String, IRotaHudElement>();

    private RotaHudRegistry() {
    }

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
        LOGGER.info("Elemento de HUD registrado com sucesso: {} (\u00c2ncora padr\u00e3o: {})", (Object)element.getId(), (Object)element.getDefaultAnchor());
    }

    public static void unregister(String id) {
        IRotaHudElement removed;
        if (id != null && (removed = ELEMENTS.remove(id)) != null) {
            LOGGER.info("Elemento de HUD removido: {}", (Object)id);
        }
    }

    public static IRotaHudElement getElement(String id) {
        return id != null ? ELEMENTS.get(id) : null;
    }

    public static boolean hasElement(String id) {
        return id != null && ELEMENTS.containsKey(id);
    }

    public static Collection<IRotaHudElement> getAllElements() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    public static List<IRotaHudElement> getVisibleElements() {
        ArrayList<IRotaHudElement> visible = new ArrayList<IRotaHudElement>();
        for (IRotaHudElement element : ELEMENTS.values()) {
            if (!element.isVisible()) continue;
            visible.add(element);
        }
        return visible;
    }

    public static int getCount() {
        return ELEMENTS.size();
    }

    public static void clear() {
        ELEMENTS.clear();
    }
}

