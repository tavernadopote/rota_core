package com.rotacore.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Camada unificada de identidade de cor do jogador da suite Rota Mods.
 * Gerencia a cor de identificação compartilhada entre Rota Map (marcadores),
 * Rota Questbook (silhueta holográfica estilo V.A.T.S. e barra diegética de scan),
 * e demais módulos do ecossistema.
 */
public final class RotaPlayerIdentity {

    private static final Logger LOGGER = LoggerFactory.getLogger("rota_core/player_identity");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ColorInfo(int rgb, String hex, String namePt, String nameEn) {}

    private static final Map<DyeColor, ColorInfo> COLOR_DATA = new EnumMap<>(DyeColor.class);

    static {
        COLOR_DATA.put(DyeColor.WHITE, new ColorInfo(0xFFFFFF, "#FFFFFF", "Branco", "White"));
        COLOR_DATA.put(DyeColor.ORANGE, new ColorInfo(0xF9801D, "#F9801D", "Laranja", "Orange"));
        COLOR_DATA.put(DyeColor.MAGENTA, new ColorInfo(0xC74EBD, "#C74EBD", "Magenta", "Magenta"));
        COLOR_DATA.put(DyeColor.LIGHT_BLUE, new ColorInfo(0x3AB3DA, "#3AB3DA", "Azul Claro", "Light Blue"));
        COLOR_DATA.put(DyeColor.YELLOW, new ColorInfo(0xFED83D, "#FED83D", "Amarelo", "Yellow"));
        COLOR_DATA.put(DyeColor.LIME, new ColorInfo(0x80C71F, "#80C71F", "Verde Limão", "Lime"));
        COLOR_DATA.put(DyeColor.PINK, new ColorInfo(0xF4B3E2, "#F4B3E2", "Rosa", "Pink"));
        COLOR_DATA.put(DyeColor.GRAY, new ColorInfo(0x474F52, "#474F52", "Cinza", "Gray"));
        COLOR_DATA.put(DyeColor.LIGHT_GRAY, new ColorInfo(0x9D9D97, "#9D9D97", "Cinza Claro", "Light Gray"));
        COLOR_DATA.put(DyeColor.CYAN, new ColorInfo(0x169C9C, "#169C9C", "Ciano", "Cyan"));
        COLOR_DATA.put(DyeColor.PURPLE, new ColorInfo(0x8932B8, "#8932B8", "Roxo", "Purple"));
        COLOR_DATA.put(DyeColor.BLUE, new ColorInfo(0x3C44AA, "#3C44AA", "Azul", "Blue"));
        COLOR_DATA.put(DyeColor.BROWN, new ColorInfo(0x835432, "#835432", "Marrom", "Brown"));
        COLOR_DATA.put(DyeColor.GREEN, new ColorInfo(0x5E7C16, "#5E7C16", "Verde", "Green"));
        COLOR_DATA.put(DyeColor.RED, new ColorInfo(0xB02E26, "#B02E26", "Vermelho", "Red"));
        COLOR_DATA.put(DyeColor.BLACK, new ColorInfo(0x1D1D21, "#1D1D21", "Preto", "Black"));
    }

    private static DyeColor playerColor = DyeColor.LIME;
    private static Path customConfigPath = null;
    private static boolean loaded = false;

    private RotaPlayerIdentity() {}

    public static synchronized void setCustomConfigPath(Path path) {
        customConfigPath = path;
    }

    public static Path getConfigPath() {
        if (customConfigPath != null) {
            return customConfigPath;
        }
        try {
            return FMLPaths.CONFIGDIR.get().resolve("rota_core_player.json");
        } catch (Throwable t) {
            return Path.of("config", "rota_core_player.json");
        }
    }

    public static synchronized DyeColor getPlayerColor() {
        ensureLoaded();
        return playerColor;
    }

    public static synchronized void setPlayerColor(DyeColor color) {
        ensureLoaded();
        if (color != null && playerColor != color) {
            playerColor = color;
            save();
        }
    }

    public static synchronized int getPlayerColorRGB() {
        return getPlayerColorRGB(getPlayerColor());
    }

    public static int getPlayerColorRGB(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.rgb() : 0x80C71F;
    }

    public static String getColorHex(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.hex() : "#80C71F";
    }

    public static String getColorNamePt(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.namePt() : "Verde Limão";
    }

    public static String getColorNameEn(DyeColor color) {
        ColorInfo info = COLOR_DATA.get(color != null ? color : DyeColor.LIME);
        return info != null ? info.nameEn() : "Lime";
    }

    public static String getColorName(DyeColor color, boolean portuguese) {
        return portuguese ? getColorNamePt(color) : getColorNameEn(color);
    }

    public static String getColorName(DyeColor color) {
        boolean pt = true;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null && mc.options.languageCode != null) {
                pt = mc.options.languageCode.toLowerCase(Locale.ROOT).startsWith("pt");
            }
        } catch (Throwable ignored) {
            // Ambiente de teste ou headless
        }
        return getColorName(color, pt);
    }

    public static synchronized void cyclePlayerColor(boolean forward) {
        ensureLoaded();
        DyeColor[] values = DyeColor.values();
        int currentIndex = playerColor.ordinal();
        int nextIndex;
        if (forward) {
            nextIndex = (currentIndex + 1) % values.length;
        } else {
            nextIndex = (currentIndex - 1 + values.length) % values.length;
        }
        setPlayerColor(values[nextIndex]);
    }

    public static void cyclePlayerColor() {
        cyclePlayerColor(true);
    }

    public static synchronized void load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("playerColor")) {
                    String colorName = json.get("playerColor").getAsString();
                    for (DyeColor color : DyeColor.values()) {
                        if (color.name().equalsIgnoreCase(colorName)) {
                            playerColor = color;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Falha ao carregar rota_core_player.json, usando padrão (LIME): {}", e.getMessage());
                playerColor = DyeColor.LIME;
            }
        } else {
            playerColor = DyeColor.LIME;
        }
        loaded = true;
    }

    public static synchronized void save() {
        Path path = getConfigPath();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            JsonObject json = new JsonObject();
            json.addProperty("playerColor", playerColor.name());
            json.addProperty("colorHex", getColorHex(playerColor));
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Não foi possível salvar rota_core_player.json: {}", e.getMessage());
        }
    }

    private static synchronized void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    /**
     * Cria a opção de configuração padrão para seleção da cor de identidade no menu Rota Core.
     * Como configuração cosmética de cliente, define requiresAdmin(false) e clientOnly(true).
     */
    public static RotaConfigOption createConfigOption() {
        return RotaConfigOption.cycle(
                Component.translatable("rota_core.config.player_color"),
                "Define a cor visual de identificação do jogador para marcadores do mapa, silhueta holográfica da luneta e barras diegéticas.",
                "Opção cosmética de cliente. Escolha entre as 16 cores clássicas do Minecraft.",
                getColorNamePt(DyeColor.LIME),
                () -> getColorName(getPlayerColor()),
                () -> cyclePlayerColor(true)
        ).requiresAdmin(false).clientOnly(true).withForceTip(true);
    }
}
