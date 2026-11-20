/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.annotations.SerializedName
 */
package com.rotacore.world.names;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillageNamePack {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private boolean replace = false;
    private String culture = "plains";
    private List<String> prefixes = new ArrayList<String>();
    private List<String> suffixes = new ArrayList<String>();
    @SerializedName(value="unique_names")
    private List<String> uniqueNames = new ArrayList<String>();
    @SerializedName(value="specific_names")
    private Map<String, String> specificNames = new HashMap<String, String>();
    private Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

    public VillageNamePack() {
    }

    public VillageNamePack(String culture, boolean replace) {
        this.culture = culture != null ? culture : "plains";
        this.replace = replace;
    }

    public boolean isReplace() {
        return this.replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public String getCulture() {
        return this.culture;
    }

    public void setCulture(String culture) {
        this.culture = culture != null ? culture : "plains";
    }

    public List<String> getPrefixes() {
        return this.prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes != null ? new ArrayList<String>(prefixes) : new ArrayList();
    }

    public List<String> getSuffixes() {
        return this.suffixes;
    }

    public void setSuffixes(List<String> suffixes) {
        this.suffixes = suffixes != null ? new ArrayList<String>(suffixes) : new ArrayList();
    }

    public List<String> getUniqueNames() {
        return this.uniqueNames;
    }

    public void setUniqueNames(List<String> uniqueNames) {
        this.uniqueNames = uniqueNames != null ? new ArrayList<String>(uniqueNames) : new ArrayList();
    }

    public Map<String, String> getSpecificNames() {
        return this.specificNames;
    }

    public void setSpecificNames(Map<String, String> specificNames) {
        this.specificNames = specificNames != null ? new HashMap<String, String>(specificNames) : new HashMap();
    }

    public Map<String, Map<String, String>> getTranslations() {
        return this.translations;
    }

    public void setTranslations(Map<String, Map<String, String>> translations) {
        this.translations = translations != null ? new HashMap<String, Map<String, String>>(translations) : new HashMap();
    }

    public void addPrefix(String prefix) {
        if (prefix != null && !prefix.trim().isEmpty() && !this.prefixes.contains(prefix)) {
            this.prefixes.add(prefix.trim());
        }
    }

    public void addSuffix(String suffix) {
        if (suffix != null && !suffix.trim().isEmpty() && !this.suffixes.contains(suffix)) {
            this.suffixes.add(suffix.trim());
        }
    }

    public void addUniqueName(String uniqueName) {
        if (uniqueName != null && !uniqueName.trim().isEmpty() && !this.uniqueNames.contains(uniqueName)) {
            this.uniqueNames.add(uniqueName.trim());
        }
    }

    public void addSpecificName(int x, int z, String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.specificNames.put(x + "," + z, name.trim());
        }
    }

    public void addTranslation(String locale, String key, String translatedText) {
        if (locale == null || key == null || translatedText == null) {
            return;
        }
        this.translations.computeIfAbsent(locale.toLowerCase().trim(), k -> new HashMap()).put(key.trim(), translatedText.trim());
    }

    public String translate(String key, String locale) {
        Map<String, String> ptMap;
        Map<String, String> enMap;
        String shortLang;
        Map<String, String> shortMap;
        String lang;
        Map<String, String> langMap;
        if (key == null) {
            return "";
        }
        if (locale == null || locale.isEmpty()) {
            locale = "pt_br";
        }
        if ((langMap = this.translations.get(lang = locale.toLowerCase().trim())) != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        if (lang.contains("_") && (shortMap = this.translations.get(shortLang = lang.split("_")[0])) != null && shortMap.containsKey(key)) {
            return shortMap.get(key);
        }
        if (!lang.startsWith("en") && (enMap = this.translations.get("en_us")) != null && enMap.containsKey(key)) {
            return enMap.get(key);
        }
        if (!lang.startsWith("pt") && (ptMap = this.translations.get("pt_br")) != null && ptMap.containsKey(key)) {
            return ptMap.get(key);
        }
        return key;
    }

    public void merge(VillageNamePack other) {
        if (other == null) {
            return;
        }
        if (other.isReplace()) {
            this.replace = true;
            this.prefixes = new ArrayList<String>(other.prefixes);
            this.suffixes = new ArrayList<String>(other.suffixes);
            this.uniqueNames = new ArrayList<String>(other.uniqueNames);
            this.specificNames = new HashMap<String, String>(other.specificNames);
            this.translations = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, Map<String, String>> entry : other.translations.entrySet()) {
                this.translations.put(entry.getKey(), new HashMap<String, String>(entry.getValue()));
            }
            return;
        }
        for (String string : other.prefixes) {
            if (this.prefixes.contains(string)) continue;
            this.prefixes.add(string);
        }
        for (String string : other.suffixes) {
            if (this.suffixes.contains(string)) continue;
            this.suffixes.add(string);
        }
        for (String string : other.uniqueNames) {
            if (this.uniqueNames.contains(string)) continue;
            this.uniqueNames.add(string);
        }
        this.specificNames.putAll(other.specificNames);
        for (Map.Entry entry : other.translations.entrySet()) {
            Map current = this.translations.computeIfAbsent((String)entry.getKey(), k -> new HashMap());
            current.putAll((Map)entry.getValue());
        }
    }

    public String toJson() {
        return GSON.toJson((Object)this);
    }

    public static VillageNamePack fromJson(String json) {
        return (VillageNamePack)GSON.fromJson(json, VillageNamePack.class);
    }

    public static VillageNamePack fromJson(JsonObject jsonObject) {
        return (VillageNamePack)GSON.fromJson((JsonElement)jsonObject, VillageNamePack.class);
    }
}

