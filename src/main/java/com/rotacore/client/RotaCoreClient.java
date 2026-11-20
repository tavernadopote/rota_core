/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.IExtensionPoint
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.neoforge.client.gui.IConfigScreenFactory
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.rotacore.client;

import com.rotacore.client.RotaClientEvents;
import com.rotacore.client.RotaKeyBindings;
import com.rotacore.client.gui.RotaCoreModulesScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class RotaCoreClient {
    public static void registerClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(RotaKeyBindings::register);
        NeoForge.EVENT_BUS.register(RotaClientEvents.class);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (IExtensionPoint)((IConfigScreenFactory)(container, screen) -> new RotaCoreModulesScreen(screen)));
    }
}

