package com.rotacore.world.names;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo de dados para pacotes de nomes de vilas e assentamentos.
 * Suporta listas de prefixos, sufixos, nomes exclusivos, vinculação a coordenadas
 * e dicionário multilíngue de traduções (pt_br, en_us, etc.).
 */
public class VillageNamePack {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean replace = false;
    private String culture = "plains";

    private List<String> prefixes = new ArrayList<>();
    private List<String> suffixes = new ArrayList<>();

    @SerializedName("unique_names")
    private List<String> uniqueNames = new ArrayList<>();

    @SerializedName("specific_names")
    private Map<String, String> specificNames = new HashMap<>();

    private Map<String, Map<String, String>> translations = new HashMap<>();

    public VillageNamePack() {
    }

    public VillageNamePack(String culture, boolean replace) {
        this.culture = culture != null ? culture : "plains";
        this.replace = replace;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture != null ? culture : "plains";
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes != null ? new ArrayList<>(prefixes) : new ArrayList<>();
    }

    public List<String> getSuffixes() {
        return suffixes;
    }

    public void setSuffixes(List<String> suffixes) {
        this.suffixes = suffixes != null ? new ArrayList<>(suffixes) : new ArrayList<>();
    }

    public List<String> getUniqueNames() {
        return uniqueNames;
    }

    public void setUniqueNames(List<String> uniqueNames) {
        this.uniqueNames = uniqueNames != null ? new ArrayList<>(uniqueNames) : new ArrayList<>();
    }

    public Map<String, String> getSpecificNames() {
        return specificNames;
    }

    public void setSpecificNames(Map<String, String> specificNames) {
        this.specificNames = specificNames != null ? new HashMap<>(specificNames) : new HashMap<>();
    }

    public Map<String, Map<String, String>> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, Map<String, String>> translations) {
        this.translations = translations != null ? new HashMap<>(translations) : new HashMap<>();
    }

    public void addPrefix(String prefix) {
        if (prefix != null && !prefix.trim().isEmpty() && !prefixes.contains(prefix)) {
            prefixes.add(prefix.trim());
        }
    }

    public void addSuffix(String suffix) {
        if (suffix != null && !suffix.trim().isEmpty() && !suffixes.contains(suffix)) {
            suffixes.add(suffix.trim());
        }
    }

    public void addUniqueName(String uniqueName) {
        if (uniqueName != null && !uniqueName.trim().isEmpty() && !uniqueNames.contains(uniqueName)) {
            uniqueNames.add(uniqueName.trim());
        }
    }

    public void addSpecificName(int x, int z, String name) {
        if (name != null && !name.trim().isEmpty()) {
            specificNames.put(x + "," + z, name.trim());
        }
    }

    public void addTranslation(String locale, String key, String translatedText) {
        if (locale == null || key == null || translatedText == null) return;
        translations.computeIfAbsent(locale.toLowerCase().trim(), k -> new HashMap<>())
                .put(key.trim(), translatedText.trim());
    }

    /**
     * Traduz uma chave ou termo de acordo com a tabela de idiomas do pacote.
     * Possui fallback sequencial para en_us, pt_br e por fim retorna a própria chave.
     */
    public String translate(String key, String locale) {
        if (key == null) return "";
        if (locale == null || locale.isEmpty()) locale = "pt_br";
        String lang = locale.toLowerCase().trim();

        // 1. Tenta tradução para o idioma requisitado
        Map<String, String> langMap = translations.get(lang);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }

        // 2. Se idioma tiver sufixo (ex: pt_br -> pt), tenta formato simplificado
        if (lang.contains("_")) {
            String shortLang = lang.split("_")[0];
            Map<String, String> shortMap = translations.get(shortLang);
            if (shortMap != null && shortMap.containsKey(key)) {
                return shortMap.get(key);
            }
        }

        // 3. Fallback para en_us
        if (!lang.startsWith("en")) {
            Map<String, String> enMap = translations.get("en_us");
            if (enMap != null && enMap.containsKey(key)) {
                return enMap.get(key);
            }
        }

        // 4. Fallback para pt_br
        if (!lang.startsWith("pt")) {
            Map<String, String> ptMap = translations.get("pt_br");
            if (ptMap != null && ptMap.containsKey(key)) {
                return ptMap.get(key);
            }
        }

        // 5. Retorna a própria chave bruta
        return key;
    }

    /**
     * Mescla este pacote com os dados de outro pacote carregado via DataPack.
     * Caso o outro pacote possua replace=true, os dados anteriores são totalmente sobrescritos.
     */
    public void merge(VillageNamePack other) {
        if (other == null) return;

        if (other.isReplace()) {
            this.replace = true;
            this.prefixes = new ArrayList<>(other.prefixes);
            this.suffixes = new ArrayList<>(other.suffixes);
            this.uniqueNames = new ArrayList<>(other.uniqueNames);
            this.specificNames = new HashMap<>(other.specificNames);
            this.translations = new HashMap<>();
            for (Map.Entry<String, Map<String, String>> entry : other.translations.entrySet()) {
                this.translations.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            return;
        }

        // Mescla sem duplicatas
        for (String p : other.prefixes) {
            if (!this.prefixes.contains(p)) this.prefixes.add(p);
        }
        for (String s : other.suffixes) {
            if (!this.suffixes.contains(s)) this.suffixes.add(s);
        }
        for (String u : other.uniqueNames) {
            if (!this.uniqueNames.contains(u)) this.uniqueNames.add(u);
        }

        this.specificNames.putAll(other.specificNames);

        for (Map.Entry<String, Map<String, String>> entry : other.translations.entrySet()) {
            Map<String, String> current = this.translations.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
            current.putAll(entry.getValue());
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static VillageNamePack fromJson(String json) {
        return GSON.fromJson(json, VillageNamePack.class);
    }

    public static VillageNamePack fromJson(JsonObject jsonObject) {
        return GSON.fromJson(jsonObject, VillageNamePack.class);
    }
}
