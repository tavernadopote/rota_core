package com.rotacore.world.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WayBodyConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WayBodyConfigManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load(Path configFile) {
        if (configFile == null || !Files.exists(configFile)) {
            return;
        }
        try {
            String content = Files.readString(configFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            if (root.has("way_bodies") && root.get("way_bodies").isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray("way_bodies")) {
                    if (el.isJsonObject()) {
                        WayBodyConfig cfg = WayBodyConfig.fromJson(el.getAsJsonObject());
                        if (cfg != null) {
                            WayBodyRegistry.setConfig(cfg.getBodyType(), cfg);
                        }
                    }
                }
            }
            LOGGER.info("Configuracao de Corpos Viarios carregada com sucesso de {}", configFile.getFileName());
        } catch (Exception e) {
            LOGGER.error("Falha ao carregar configuracoes de corpos viarios de {}", configFile, e);
        }
    }

    public static void save(Path configFile) {
        if (configFile == null) {
            return;
        }
        try {
            if (configFile.getParent() != null && !Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (WayBodyConfig cfg : WayBodyRegistry.getAllConfigs()) {
                arr.add(cfg.toJson());
            }
            root.add("way_bodies", arr);

            Path tempFile = configFile.resolveSibling(configFile.getFileName().toString() + ".tmp");
            Files.writeString(tempFile, GSON.toJson(root));
            Files.move(tempFile, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            LOGGER.info("Configuracao de Corpos Viarios salva com sucesso em {}", configFile.getFileName());
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar configuracoes de corpos viarios em {}", configFile, e);
        }
    }
}
