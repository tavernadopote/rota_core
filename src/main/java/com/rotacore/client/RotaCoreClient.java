package com.rotacore.client;

import com.rotacore.client.gui.RotaHubScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Ponto de entrada das funcionalidades exclusivas do lado do cliente do Rota Core.
 */
public class RotaCoreClient {

    public static void registerClient(IEventBus modEventBus, ModContainer modContainer) {
        // Registra teclas de atalho no barramento do mod
        modEventBus.addListener(RotaKeyBindings::register);

        // Registra eventos de interface e tick no barramento do NeoForge
        NeoForge.EVENT_BUS.register(RotaClientEvents.class);

        // Registra o gerenciador de módulos como tela de configuração no menu de Mods do NeoForge
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, screen) -> new com.rotacore.client.gui.RotaCoreModulesScreen(screen)
        );
    }
}
