package com.rotacore;

import com.rotacore.client.RotaCoreClient;
import com.rotacore.config.RotaConfigHandler;
import com.rotacore.config.RotaCoreConfig;
import com.rotacore.world.names.VillageNameDataLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
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

    private static final RotaConfigHandler<RotaCoreConfig> CONFIG_HANDLER = new RotaConfigHandler<>(
            MOD_ID,
            "rota_core.json",
            RotaCoreConfig.class,
            RotaCoreConfig::new,
            RotaCoreConfig::validate
    );

    public RotaCore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} inicializado com sucesso no NeoForge.", MOD_NAME);

        // Registra o recarregador de DataPacks de nomes de vilas no barramento de eventos do NeoForge
        NeoForge.EVENT_BUS.addListener(VillageNameDataLoader::onAddReloadListeners);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RotaCoreClient.registerClient(modEventBus, modContainer);
        }
    }

    public static RotaCoreConfig getConfig() {
        return CONFIG_HANDLER.get();
    }

    public static void saveConfig() {
        CONFIG_HANDLER.save();
    }
}
