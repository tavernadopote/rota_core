/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.common.NeoForge
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
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

@Mod(value="rota_core")
public class RotaCore {
    public static final String MOD_ID = "rota_core";
    public static final String MOD_NAME = "Rota Core";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"Rota Core");
    private static final RotaConfigHandler<RotaCoreConfig> CONFIG_HANDLER = new RotaConfigHandler<RotaCoreConfig>("rota_core", "rota_core.json", RotaCoreConfig.class, RotaCoreConfig::new, RotaCoreConfig::validate);

    public RotaCore(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} inicializado com sucesso no NeoForge.", (Object)MOD_NAME);
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

