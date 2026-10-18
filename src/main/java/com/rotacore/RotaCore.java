package com.rotacore;

import com.rotacore.client.RotaCoreClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ponto de entrada do Rota Core.
 * Fornece arquitetura unificada, hub de interface universal e serviços compartilhados
 * para todos os mods do ecossistema Rota Mods.
 */
@Mod(RotaCore.MOD_ID)
public class RotaCore {
    public static final String MOD_ID = "rota_core";
    public static final String MOD_NAME = "Rota Core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public RotaCore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} v1.0.0 inicializado com sucesso no NeoForge.", MOD_NAME);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RotaCoreClient.registerClient(modEventBus, modContainer);
        }
    }
}
