/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.DyeColor
 *  net.neoforged.fml.loading.FMLPaths
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.rotacore.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rotacore.config.RotaConfigOption;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RotaPlayerIdentity {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rota_core/player_identity");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<DyeColor, ColorInfo> COLOR_DATA = new EnumMap<DyeColor, ColorInfo>(DyeColor.class);
    private static DyeColor playerColor;
    private static Path customConfigPath;
    private static boolean loaded;

    private RotaPlayerIdentity() {
    }

    public static synchronized void setCustomConfigPath(Path path) {
        customConfigPath = path;
    }

    public static Path getConfigPath() {
        if (customConfigPath != null) {
            return customConfigPath;
        }
        try {
            return FMLPaths.CONFIGDIR.get().resolve("rota_core_player.json");
        }
        catch (Throwable t) {
            return Path.of("config", "rota_core_player.json");
        }
    }

    public static synchronized DyeColor getPlayerColor() {
        RotaPlayerIdentity.ensureLoaded();
        return playerColor;
    }

    public static synchronized void setPlayerColor(DyeColor color) {
        RotaPlayerIdentity.ensureLoaded();
        if (color != null && playerColor != color) {
            playerColor = color;
            RotaPlayerIdentity.save();
        }
    }

    public static synchronized int getPlayerColorRGB() {
        return RotaPlayerIdentity.getPlayerColorRGB(RotaPlayerIdentity.getPlayerColor());
    }

    public static int getPlayerColorRGB(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.rgb() : 8439583;
    }

    public static String getColorHex(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.hex() : "#80C71F";
    }

    public static String getColorNamePt(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.namePt() : "Verde Lim\u00e3o";
    }

    public static String getColorNameEn(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.nameEn() : "Lime";
    }

    public static String getColorName(DyeColor color, boolean portuguese) {
        return portuguese ? RotaPlayerIdentity.getColorNamePt(color) : RotaPlayerIdentity.getColorNameEn(color);
    }

    public static String getColorName(DyeColor color) {
        boolean pt = true;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null && mc.options.languageCode != null) {
                pt = mc.options.languageCode.toLowerCase(Locale.ROOT).startsWith("pt");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return RotaPlayerIdentity.getColorName(color, pt);
    }

    public static synchronized void cyclePlayerColor(boolean forward) {
        RotaPlayerIdentity.ensureLoaded();
        DyeColor[] values = DyeColor.values();
        int currentIndex = playerColor.ordinal();
        int nextIndex = forward ? (currentIndex + 1) % values.length : (currentIndex - 1 + values.length) % values.length;
        RotaPlayerIdentity.setPlayerColor(values[nextIndex]);
    }

    public static void cyclePlayerColor() {
        RotaPlayerIdentity.cyclePlayerColor(true);
    }

    public static synchronized void load() {
        block11: {
            Path path = RotaPlayerIdentity.getConfigPath();
            if (Files.exists(path, new LinkOption[0])) {
                try (BufferedReader reader = Files.newBufferedReader(path);){
                    JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                    if (!json.has("playerColor")) break block11;
                    String colorName = json.get("playerColor").getAsString();
                    for (DyeColor color : DyeColor.values()) {
                        if (!color.name().equalsIgnoreCase(colorName)) continue;
                        playerColor = color;
                        break;
                    }
                }
                catch (Exception e) {
                    LOGGER.warn("Falha ao carregar rota_core_player.json, usando padr\u00e3o (LIME): {}", (Object)e.getMessage());
                    playerColor = DyeColor.LIME;
                }
            } else {
                playerColor = DyeColor.LIME;
            }
        }
        loaded = true;
    }

    public static synchronized void save() {
        Path path = RotaPlayerIdentity.getConfigPath();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
            }
            JsonObject json = new JsonObject();
            json.addProperty("playerColor", playerColor.name());
            json.addProperty("colorHex", RotaPlayerIdentity.getColorHex(playerColor));
            try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
                GSON.toJson((JsonElement)json, (Appendable)writer);
            }
        }
        catch (IOException e) {
            LOGGER.error("N\u00e3o foi poss\u00edvel salvar rota_core_player.json: {}", (Object)e.getMessage());
        }
    }

    private static synchronized void ensureLoaded() {
        if (!loaded) {
            RotaPlayerIdentity.load();
        }
    }

    public static RotaConfigOption createConfigOption() {
        return RotaConfigOption.cycle((Component)Component.translatable((String)"rota_core.config.player_color"), "Define a cor visual de identifica\u00e7\u00e3o do jogador para marcadores do mapa, silhueta hologr\u00e1fica da luneta e barras dieg\u00e9ticas.", "Op\u00e7\u00e3o cosm\u00e9tica de cliente. Escolha entre as 16 cores cl\u00e1ssicas do Minecraft.", RotaPlayerIdentity.getColorNamePt(DyeColor.LIME), () -> RotaPlayerIdentity.getColorName(RotaPlayerIdentity.getPlayerColor()), () -> RotaPlayerIdentity.cyclePlayerColor(true)).requiresAdmin(false).clientOnly(true).withForceTip(true);
    }

    static {
        COLOR_DATA.put(DyeColor.WHITE, new ColorInfo(0xFFFFFF, "#FFFFFF", "Branco", "White"));
        COLOR_DATA.put(DyeColor.ORANGE, new ColorInfo(16351261, "#F9801D", "Laranja", "Orange"));
        COLOR_DATA.put(DyeColor.MAGENTA, new ColorInfo(13061821, "#C74EBD", "Magenta", "Magenta"));
        COLOR_DATA.put(DyeColor.LIGHT_BLUE, new ColorInfo(3847130, "#3AB3DA", "Azul Claro", "Light Blue"));
        COLOR_DATA.put(DyeColor.YELLOW, new ColorInfo(16701501, "#FED83D", "Amarelo", "Yellow"));
        COLOR_DATA.put(DyeColor.LIME, new ColorInfo(8439583, "#80C71F", "Verde Lim\u00e3o", "Lime"));
        COLOR_DATA.put(DyeColor.PINK, new ColorInfo(16036834, "#F4B3E2", "Rosa", "Pink"));
        COLOR_DATA.put(DyeColor.GRAY, new ColorInfo(4673362, "#474F52", "Cinza", "Gray"));
        COLOR_DATA.put(DyeColor.LIGHT_GRAY, new ColorInfo(0x9D9D97, "#9D9D97", "Cinza Claro", "Light Gray"));
        COLOR_DATA.put(DyeColor.CYAN, new ColorInfo(1481884, "#169C9C", "Ciano", "Cyan"));
        COLOR_DATA.put(DyeColor.PURPLE, new ColorInfo(8991416, "#8932B8", "Roxo", "Purple"));
        COLOR_DATA.put(DyeColor.BLUE, new ColorInfo(3949738, "#3C44AA", "Azul", "Blue"));
        COLOR_DATA.put(DyeColor.BROWN, new ColorInfo(8606770, "#835432", "Marrom", "Brown"));
        COLOR_DATA.put(DyeColor.GREEN, new ColorInfo(6192150, "#5E7C16", "Verde", "Green"));
        COLOR_DATA.put(DyeColor.RED, new ColorInfo(11546150, "#B02E26", "Vermelho", "Red"));
        COLOR_DATA.put(DyeColor.BLACK, new ColorInfo(0x1D1D21, "#1D1D21", "Preto", "Black"));
        playerColor = DyeColor.LIME;
        customConfigPath = null;
        loaded = false;
    }

    public record ColorInfo(int rgb, String hex, String namePt, String nameEn) {
    }
}

