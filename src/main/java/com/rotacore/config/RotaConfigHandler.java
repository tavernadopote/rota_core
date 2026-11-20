/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.neoforged.fml.loading.FMLPaths
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.rotacore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public RotaConfigHandler(String modId, String fileName, Class<T> configClass, Supplier<T> defaultSupplier, Consumer<T> validator) {
        this.modId = modId;
        this.fileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        this.configClass = configClass;
        this.defaultSupplier = defaultSupplier;
        this.validator = validator;
        this.logger = LoggerFactory.getLogger((String)(modId + "/config"));
        this.configPath = FMLPaths.CONFIGDIR.get().resolve(this.fileName);
        this.reload();
    }

    public synchronized T get() {
        if (this.instance == null) {
            this.instance = this.defaultSupplier.get();
        }
        return this.instance;
    }

    public synchronized void reload() {
        if (!Files.exists(this.configPath, new LinkOption[0])) {
            this.logger.info("Arquivo de configura\u00e7\u00e3o {} n\u00e3o encontrado. Gerando com valores padr\u00e3o.", (Object)this.configPath);
            this.instance = this.defaultSupplier.get();
            this.validateAndSave();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(this.configPath);){
            Object loaded = GSON.fromJson((Reader)reader, this.configClass);
            if (loaded != null) {
                this.instance = loaded;
                if (this.validator != null) {
                    this.validator.accept(this.instance);
                }
                this.logger.info("Configura\u00e7\u00e3o carregada com sucesso a partir de {}", (Object)this.configPath);
            } else {
                this.logger.warn("Configura\u00e7\u00e3o em {} estava vazia. Usando padr\u00f5es.", (Object)this.configPath);
                this.instance = this.defaultSupplier.get();
                this.validateAndSave();
            }
        }
        catch (Exception e) {
            this.logger.error("Falha ao ler o arquivo de configura\u00e7\u00e3o {}. Mantendo padr\u00f5es para seguran\u00e7a.", (Object)this.configPath, (Object)e);
            this.instance = this.defaultSupplier.get();
        }
    }

    public synchronized void save() {
        this.validateAndSave();
    }

    private void validateAndSave() {
        if (this.instance == null) {
            this.instance = this.defaultSupplier.get();
        }
        if (this.validator != null) {
            this.validator.accept(this.instance);
        }
        try {
            if (this.configPath.getParent() != null && !Files.exists(this.configPath.getParent(), new LinkOption[0])) {
                Files.createDirectories(this.configPath.getParent(), new FileAttribute[0]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(this.configPath, new OpenOption[0]);){
                GSON.toJson(this.instance, (Appendable)writer);
            }
            this.logger.info("Configura\u00e7\u00e3o salva com sucesso em {}", (Object)this.configPath);
        }
        catch (IOException e) {
            this.logger.error("Erro ao salvar o arquivo de configura\u00e7\u00e3o {}", (Object)this.configPath, (Object)e);
        }
    }

    public synchronized void restoreDefaults() {
        this.instance = this.defaultSupplier.get();
        this.validateAndSave();
        this.logger.info("Valores padr\u00e3o restaurados para {}", (Object)this.configPath);
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public String getModId() {
        return this.modId;
    }
}

