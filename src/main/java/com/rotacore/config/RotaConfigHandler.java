package com.rotacore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gerenciador universal e leve de configurações JSON para a Rota Mods.
 * Elimina totalmente dependências de mods externos (como Cloth Config),
 * mantendo persistência robusta, formatação limpa e validação automática.
 *
 * @param <T> Classe POJO da configuração do mod.
 */
public class RotaConfigHandler<T> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String modId;
    private final String fileName;
    private final Class<T> configClass;
    private final Supplier<T> defaultSupplier;
    private final Consumer<T> validator;
    private final Logger logger;

    private T instance;
    private final Path configPath;

    public RotaConfigHandler(
            String modId,
            String fileName,
            Class<T> configClass,
            Supplier<T> defaultSupplier,
            Consumer<T> validator
    ) {
        this.modId = modId;
        this.fileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        this.configClass = configClass;
        this.defaultSupplier = defaultSupplier;
        this.validator = validator;
        this.logger = LoggerFactory.getLogger(modId + "/config");
        this.configPath = FMLPaths.CONFIGDIR.get().resolve(this.fileName);

        // Carrega imediatamente ao instanciar
        this.reload();
    }

    public synchronized T get() {
        if (instance == null) {
            instance = defaultSupplier.get();
        }
        return instance;
    }

    public synchronized void reload() {
        if (!Files.exists(configPath)) {
            logger.info("Arquivo de configuração {} não encontrado. Gerando com valores padrão.", configPath);
            instance = defaultSupplier.get();
            validateAndSave();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            T loaded = GSON.fromJson(reader, configClass);
            if (loaded != null) {
                instance = loaded;
                if (validator != null) {
                    validator.accept(instance);
                }
                logger.info("Configuração carregada com sucesso a partir de {}", configPath);
            } else {
                logger.warn("Configuração em {} estava vazia. Usando padrões.", configPath);
                instance = defaultSupplier.get();
                validateAndSave();
            }
        } catch (Exception e) {
            logger.error("Falha ao ler o arquivo de configuração {}. Mantendo padrões para segurança.", configPath, e);
            instance = defaultSupplier.get();
        }
    }

    public synchronized void save() {
        validateAndSave();
    }

    private void validateAndSave() {
        if (instance == null) {
            instance = defaultSupplier.get();
        }
        if (validator != null) {
            validator.accept(instance);
        }

        try {
            if (configPath.getParent() != null && !Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(instance, writer);
            }
            logger.info("Configuração salva com sucesso em {}", configPath);
        } catch (IOException e) {
            logger.error("Erro ao salvar o arquivo de configuração {}", configPath, e);
        }
    }

    public synchronized void restoreDefaults() {
        instance = defaultSupplier.get();
        validateAndSave();
        logger.info("Valores padrão restaurados para {}", configPath);
    }

    public Path getConfigPath() {
        return configPath;
    }

    public String getModId() {
        return modId;
    }
}
