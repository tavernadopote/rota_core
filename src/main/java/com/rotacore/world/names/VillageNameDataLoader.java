/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 *  net.neoforged.neoforge.event.AddReloadListenerEvent
 *  org.slf4j.Logger
 */
package com.rotacore.world.names;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.rotacore.world.names.VillageNamePack;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

public class VillageNameDataLoader
extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final VillageNameDataLoader INSTANCE = new VillageNameDataLoader();
    private final Map<String, VillageNamePack> packs = new ConcurrentHashMap<String, VillageNamePack>();
    private final VillageNamePack fallbackPack = VillageNameDataLoader.createDefaultPlainsPack();

    public static VillageNameDataLoader getInstance() {
        return INSTANCE;
    }

    public VillageNameDataLoader() {
        super(GSON, "rota_core/village_names");
        this.registerDefaultPacks();
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((PreparableReloadListener)VillageNameDataLoader.getInstance());
    }

    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("[Rota Core] Carregando pacotes de nomes de vilas via DataPacks ({} arquivos encontrados)...", (Object)jsonMap.size());
        this.registerDefaultPacks();
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                if (!element.isJsonObject()) continue;
                VillageNamePack pack = (VillageNamePack)GSON.fromJson(element, VillageNamePack.class);
                if (pack.getCulture() == null || pack.getCulture().trim().isEmpty() || "custom".equalsIgnoreCase(pack.getCulture())) {
                    String path = id.getPath();
                    if (path.contains("/")) {
                        path = path.substring(path.lastIndexOf(47) + 1);
                    }
                    pack.setCulture(path);
                }
                this.registerOrMergePack(pack);
                LOGGER.debug("[Rota Core] Pacote de nomes carregado com sucesso: {} (Cultura: {})", (Object)id, (Object)pack.getCulture());
            }
            catch (Exception e) {
                LOGGER.error("[Rota Core] Falha ao processar arquivo de nomes de vilas {}: {}", (Object)id, (Object)e.getMessage());
            }
        }
    }

    public void registerOrMergePack(VillageNamePack pack) {
        if (pack == null || pack.getCulture() == null) {
            return;
        }
        String culture = pack.getCulture().toLowerCase().trim();
        VillageNamePack existing = this.packs.get(culture);
        if (existing == null || pack.isReplace()) {
            this.packs.put(culture, pack);
        } else {
            existing.merge(pack);
        }
    }

    public VillageNamePack getPack(String culture) {
        if (culture == null) {
            return this.packs.getOrDefault("plains", this.fallbackPack);
        }
        String key = culture.toLowerCase().trim();
        VillageNamePack pack = this.packs.get(key);
        if (pack != null) {
            return pack;
        }
        return this.packs.getOrDefault("plains", this.fallbackPack);
    }

    public String getSpecificName(int x, int z, String locale) {
        String coordKey = x + "," + z;
        for (VillageNamePack pack : this.packs.values()) {
            if (!pack.getSpecificNames().containsKey(coordKey)) continue;
            String raw = pack.getSpecificNames().get(coordKey);
            return pack.translate(raw, locale);
        }
        return null;
    }

    public String translateName(String name, String culture, String locale) {
        VillageNamePack pack = this.getPack(culture);
        return pack.translate(name, locale);
    }

    public void loadFromJson(String culture, String jsonString) {
        VillageNamePack pack = (VillageNamePack)GSON.fromJson(jsonString, VillageNamePack.class);
        if (culture != null && !culture.trim().isEmpty()) {
            pack.setCulture(culture.trim());
        }
        this.registerOrMergePack(pack);
    }

    public void registerDefaultPacks() {
        this.packs.clear();
        this.packs.put("plains", VillageNameDataLoader.createDefaultPlainsPack());
        this.packs.put("desert", VillageNameDataLoader.createDefaultDesertPack());
        this.packs.put("savanna", VillageNameDataLoader.createDefaultSavannaPack());
        this.packs.put("taiga", VillageNameDataLoader.createDefaultTaigaPack());
        this.packs.put("snowy", VillageNameDataLoader.createDefaultSnowyPack());
    }

    private static VillageNamePack createDefaultPlainsPack() {
        VillageNamePack pack = new VillageNamePack("plains", false);
        pack.setPrefixes(List.of("Belo", "Monte", "Vale", "Verde", "Pedra", "Porto", "Alto", "Rio", "Campo", "Nova", "Vila", "Sol"));
        pack.setSuffixes(List.of("val", "monte", "burgo", "vila", "campo", "porto", "Real", "Verde", "Alto", "Novo", "Dourado", "Belo"));
        pack.setUniqueNames(List.of("Alvorada", "Esperan\u00e7a", "Valenorte", "Solaria", "Terranova", "Miravale", "Bravura", "Luzerna"));
        pack.addTranslation("pt_br", "Alvorada", "Alvorada");
        pack.addTranslation("pt_br", "Esperan\u00e7a", "Esperan\u00e7a");
        pack.addTranslation("pt_br", "Valenorte", "Valenorte");
        pack.addTranslation("pt_br", "Solaria", "Sol\u00e1ria");
        pack.addTranslation("pt_br", "Terranova", "Terra Nova");
        pack.addTranslation("pt_br", "Miravale", "Miravale");
        pack.addTranslation("pt_br", "Bravura", "Bravura");
        pack.addTranslation("pt_br", "Luzerna", "Luzerna");
        pack.addTranslation("pt_br", "Belo", "Belo");
        pack.addTranslation("pt_br", "Monte", "Monte");
        pack.addTranslation("pt_br", "Vale", "Vale");
        pack.addTranslation("pt_br", "Verde", "Verde");
        pack.addTranslation("pt_br", "Pedra", "Pedra");
        pack.addTranslation("pt_br", "Porto", "Porto");
        pack.addTranslation("pt_br", "Alto", "Alto");
        pack.addTranslation("pt_br", "Rio", "Rio");
        pack.addTranslation("pt_br", "Campo", "Campo");
        pack.addTranslation("pt_br", "Nova", "Nova");
        pack.addTranslation("pt_br", "Vila", "Vila");
        pack.addTranslation("pt_br", "Sol", "Sol");
        pack.addTranslation("pt_br", "val", "val");
        pack.addTranslation("pt_br", "monte", "monte");
        pack.addTranslation("pt_br", "burgo", "burgo");
        pack.addTranslation("pt_br", "campo", "campo");
        pack.addTranslation("pt_br", "porto", "porto");
        pack.addTranslation("pt_br", "Real", "Real");
        pack.addTranslation("pt_br", "Dourado", "Dourado");
        pack.addTranslation("pt_br", "Novo", "Novo");
        pack.addTranslation("en_us", "Alvorada", "Daybreak");
        pack.addTranslation("en_us", "Esperan\u00e7a", "Hope Valley");
        pack.addTranslation("en_us", "Valenorte", "Northvale");
        pack.addTranslation("en_us", "Solaria", "Solaria");
        pack.addTranslation("en_us", "Terranova", "Newland");
        pack.addTranslation("en_us", "Miravale", "Fairview");
        pack.addTranslation("en_us", "Bravura", "Bravery");
        pack.addTranslation("en_us", "Luzerna", "Lucerne");
        pack.addTranslation("en_us", "Belo", "Fair");
        pack.addTranslation("en_us", "Monte", "Mount");
        pack.addTranslation("en_us", "Vale", "Vale");
        pack.addTranslation("en_us", "Verde", "Green");
        pack.addTranslation("en_us", "Pedra", "Stone");
        pack.addTranslation("en_us", "Porto", "Port");
        pack.addTranslation("en_us", "Alto", "High");
        pack.addTranslation("en_us", "Rio", "River");
        pack.addTranslation("en_us", "Campo", "Field");
        pack.addTranslation("en_us", "Nova", "New");
        pack.addTranslation("en_us", "Vila", "Town");
        pack.addTranslation("en_us", "Sol", "Sun");
        pack.addTranslation("en_us", "val", "dale");
        pack.addTranslation("en_us", "monte", "mount");
        pack.addTranslation("en_us", "burgo", "burgh");
        pack.addTranslation("en_us", "campo", "field");
        pack.addTranslation("en_us", "porto", "port");
        pack.addTranslation("en_us", "Real", "Royal");
        pack.addTranslation("en_us", "Dourado", "Golden");
        pack.addTranslation("en_us", "Novo", "New");
        return pack;
    }

    private static VillageNamePack createDefaultDesertPack() {
        VillageNamePack pack = new VillageNamePack("desert", false);
        pack.setPrefixes(List.of("Al-", "O\u00e1sis", "Duna", "Areia", "Sol", "Miragem", "Qasr", "Nakhla", "Caravana", "Rub"));
        pack.setSuffixes(List.of("-sarai", "-bazar", "-medina", "-duna", "Seca", "Dourada", "-zahra", "do Sol", "Eterna"));
        pack.setUniqueNames(List.of("Qubba", "Zafira", "O\u00e1sis da Lua", "Solarium", "Al-Kharid", "Miragem Alta", "Dourada"));
        pack.addTranslation("pt_br", "O\u00e1sis da Lua", "O\u00e1sis da Lua");
        pack.addTranslation("pt_br", "Miragem Alta", "Miragem Alta");
        pack.addTranslation("pt_br", "Dourada", "Dourada");
        pack.addTranslation("pt_br", "O\u00e1sis", "O\u00e1sis");
        pack.addTranslation("pt_br", "Duna", "Duna");
        pack.addTranslation("pt_br", "Areia", "Areia");
        pack.addTranslation("pt_br", "Sol", "Sol");
        pack.addTranslation("pt_br", "Miragem", "Miragem");
        pack.addTranslation("pt_br", "Seca", "Seca");
        pack.addTranslation("pt_br", "do Sol", "do Sol");
        pack.addTranslation("pt_br", "Eterna", "Eterna");
        pack.addTranslation("en_us", "O\u00e1sis da Lua", "Moon Oasis");
        pack.addTranslation("en_us", "Miragem Alta", "High Mirage");
        pack.addTranslation("en_us", "Dourada", "Golden Sands");
        pack.addTranslation("en_us", "O\u00e1sis", "Oasis");
        pack.addTranslation("en_us", "Duna", "Dune");
        pack.addTranslation("en_us", "Areia", "Sand");
        pack.addTranslation("en_us", "Sol", "Sun");
        pack.addTranslation("en_us", "Miragem", "Mirage");
        pack.addTranslation("en_us", "Seca", "Arid");
        pack.addTranslation("en_us", "do Sol", "of the Sun");
        pack.addTranslation("en_us", "Eterna", "Eternal");
        return pack;
    }

    private static VillageNamePack createDefaultSavannaPack() {
        VillageNamePack pack = new VillageNamePack("savanna", false);
        pack.setPrefixes(List.of("Baob\u00e1", "Savana", "Ac\u00e1cia", "Terra", "Le\u00e3o", "Serengeti", "Kilima", "Marula"));
        pack.setSuffixes(List.of("-kopje", "-veldt", "-boma", "-sanga", "Seca", "Alta", "Dourada", "do Vento"));
        pack.setUniqueNames(List.of("Savana Alta", "Marula", "Zimb\u00e1bue", "Kilimanjaro", "Kopje Dourado", "Baobaburg"));
        pack.addTranslation("pt_br", "Savana Alta", "Savana Alta");
        pack.addTranslation("pt_br", "Kopje Dourado", "Kopje Dourado");
        pack.addTranslation("pt_br", "do Vento", "do Vento");
        pack.addTranslation("pt_br", "Alta", "Alta");
        pack.addTranslation("pt_br", "Seca", "Seca");
        pack.addTranslation("en_us", "Savana Alta", "High Savanna");
        pack.addTranslation("en_us", "Kopje Dourado", "Golden Kopje");
        pack.addTranslation("en_us", "do Vento", "of the Wind");
        pack.addTranslation("en_us", "Alta", "High");
        pack.addTranslation("en_us", "Seca", "Dry");
        return pack;
    }

    private static VillageNamePack createDefaultTaigaPack() {
        VillageNamePack pack = new VillageNamePack("taiga", false);
        pack.setPrefixes(List.of("Pinho", "Bruma", "Bosque", "Frio", "Lobo", "Abeto", "G\u00e9lido", "N\u00f3rdico", "Fjord"));
        pack.setSuffixes(List.of("-gard", "-vik", "-holm", "-boreal", "-fjord", "Frio", "Escuro", "do Norte", "Val"));
        pack.setUniqueNames(List.of("Pinheiral", "Fjordfrio", "Taigaburgo", "Bruma Alta", "Nordgard", "Ulfvik", "Silvavale"));
        pack.addTranslation("pt_br", "Pinheiral", "Pinheiral");
        pack.addTranslation("pt_br", "Fjordfrio", "Fiorde Frio");
        pack.addTranslation("pt_br", "Bruma Alta", "Bruma Alta");
        pack.addTranslation("pt_br", "Pinho", "Pinho");
        pack.addTranslation("pt_br", "Bruma", "Bruma");
        pack.addTranslation("pt_br", "Bosque", "Bosque");
        pack.addTranslation("pt_br", "Frio", "Frio");
        pack.addTranslation("pt_br", "do Norte", "do Norte");
        pack.addTranslation("en_us", "Pinheiral", "Pine Woods");
        pack.addTranslation("en_us", "Fjordfrio", "Cold Fjord");
        pack.addTranslation("en_us", "Bruma Alta", "High Mist");
        pack.addTranslation("en_us", "Pinho", "Pine");
        pack.addTranslation("en_us", "Bruma", "Mist");
        pack.addTranslation("en_us", "Bosque", "Grove");
        pack.addTranslation("en_us", "Frio", "Cold");
        pack.addTranslation("en_us", "do Norte", "of the North");
        return pack;
    }

    private static VillageNamePack createDefaultSnowyPack() {
        VillageNamePack pack = new VillageNamePack("snowy", false);
        pack.setPrefixes(List.of("Neve", "Gelo", "Geada", "Glaciar", "Nevasca", "Inverno", "Boreal", "Cristal"));
        pack.setSuffixes(List.of("-berg", "-tundra", "-gelo", "-vento", "Branco", "Congelado", "do Norte", "Eterno"));
        pack.setUniqueNames(List.of("Ventog\u00e9lido", "Neve Alta", "Glaci\u00e1ria", "Pico Branco", "Frostholm", "Isgard"));
        pack.addTranslation("pt_br", "Ventog\u00e9lido", "Vento G\u00e9lido");
        pack.addTranslation("pt_br", "Neve Alta", "Neve Alta");
        pack.addTranslation("pt_br", "Glaci\u00e1ria", "Glaci\u00e1ria");
        pack.addTranslation("pt_br", "Pico Branco", "Pico Branco");
        pack.addTranslation("pt_br", "Neve", "Neve");
        pack.addTranslation("pt_br", "Gelo", "Gelo");
        pack.addTranslation("pt_br", "Geada", "Geada");
        pack.addTranslation("pt_br", "Glaciar", "Glaciar");
        pack.addTranslation("pt_br", "Branco", "Branco");
        pack.addTranslation("pt_br", "Congelado", "Congelado");
        pack.addTranslation("en_us", "Ventog\u00e9lido", "Frostwind");
        pack.addTranslation("en_us", "Neve Alta", "High Snow");
        pack.addTranslation("en_us", "Glaci\u00e1ria", "Glacier Ridge");
        pack.addTranslation("en_us", "Pico Branco", "White Peak");
        pack.addTranslation("en_us", "Neve", "Snow");
        pack.addTranslation("en_us", "Gelo", "Ice");
        pack.addTranslation("en_us", "Geada", "Frost");
        pack.addTranslation("en_us", "Glaciar", "Glacier");
        pack.addTranslation("en_us", "Branco", "White");
        pack.addTranslation("en_us", "Congelado", "Frozen");
        return pack;
    }
}

