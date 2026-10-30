package com.rotacore.world.names;

import com.rotacore.world.zoning.SettlementBounds;
import com.rotacore.world.zoning.SettlementZone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

/**
 * Serviço central de resolução de nomes para vilas e assentamentos do ecossistema Rota Mods.
 * Executa a hierarquia tripla oficial:
 * 1. Prioridade 1 (Waystones): Leitura de blocos de Waystone presentes na vila via reflexão segura.
 * 2. Prioridade 2 (DataPacks): Nomes específicos definidos para coordenadas ou regras customizadas via DataPack.
 * 3. Prioridade 3 (Procedural Rota Core): Gerador determinístico por semente e coordenadas centrais por bioma/cultura.
 */
public class SettlementNamingService {

    private static BiFunction<LevelAccessor, SettlementBounds, String> customWaystoneResolver = null;

    /**
     * Permite injetar um resolvedor de waystone mockado em ambientes de teste.
     */
    public static void setCustomWaystoneResolver(BiFunction<LevelAccessor, SettlementBounds, String> resolver) {
        customWaystoneResolver = resolver;
    }

    /**
     * Resolve o nome definitivo de um assentamento seguindo a hierarquia tripla.
     */
    public static String resolveSettlementName(
            LevelAccessor level,
            SettlementZone zone,
            long worldSeed,
            int centerX,
            int centerZ,
            String cultureOrBiome,
            String locale
    ) {
        String lang = (locale != null && !locale.trim().isEmpty()) ? locale.trim().toLowerCase() : "pt_br";

        // Se a zona já possui nome atribuído previamente
        if (zone != null && zone.hasSettlementName()) {
            String existing = zone.getSettlementName();
            return VillageNameDataLoader.getInstance().translateName(existing, cultureOrBiome, lang);
        }

        // 1. Prioridade 1: Waystones
        String waystoneName = findWaystoneName(level, zone);
        if (waystoneName != null && !waystoneName.trim().isEmpty()) {
            String trimmed = waystoneName.trim();
            if (zone != null) {
                zone.setSettlementName(trimmed);
            }
            return trimmed;
        }

        // 2. Prioridade 2: Nome configurado via DataPack para estas coordenadas
        String specificName = VillageNameDataLoader.getInstance().getSpecificName(centerX, centerZ, lang);
        if (specificName != null && !specificName.trim().isEmpty()) {
            String trimmed = specificName.trim();
            if (zone != null) {
                zone.setSettlementName(trimmed);
            }
            return trimmed;
        }

        // 3. Prioridade 3: Gerador procedural determinístico nativo do Rota Core
        String procedural = generateProceduralName(worldSeed, centerX, centerZ, cultureOrBiome, lang);
        if (zone != null) {
            zone.setSettlementName(procedural);
        }
        return procedural;
    }

    /**
     * Gera um nome procedural determinístico baseado na semente mundial, coordenadas centrais e bioma/cultura.
     */
    public static String generateProceduralName(
            long worldSeed,
            int centerX,
            int centerZ,
            String cultureOrBiome,
            String locale
    ) {
        String lang = (locale != null && !locale.trim().isEmpty()) ? locale.trim().toLowerCase() : "pt_br";
        String culture = normalizeCulture(cultureOrBiome);
        VillageNamePack pack = VillageNameDataLoader.getInstance().getPack(culture);

        long combinedSeed = worldSeed ^ ((((long) centerX) << 32) | (centerZ & 0xFFFFFFFFL));
        Random random = new Random(combinedSeed);

        List<String> uniqueNames = pack.getUniqueNames();
        List<String> prefixes = pack.getPrefixes();
        List<String> suffixes = pack.getSuffixes();

        // 35% de probabilidade determinística de usar um nome único pré-definido da cultura caso disponível
        if (!uniqueNames.isEmpty() && (random.nextInt(100) < 35 || prefixes.isEmpty() || suffixes.isEmpty())) {
            int idx = random.nextInt(uniqueNames.size());
            String rawUnique = uniqueNames.get(idx);
            return pack.translate(rawUnique, lang);
        }

        String prefix = prefixes.isEmpty() ? "Vila" : prefixes.get(random.nextInt(prefixes.size()));
        String suffix = suffixes.isEmpty() ? "Nova" : suffixes.get(random.nextInt(suffixes.size()));

        String transPrefix = pack.translate(prefix, lang);
        String transSuffix = pack.translate(suffix, lang);

        return formatCompoundName(transPrefix, transSuffix);
    }

    public static String formatCompoundName(String prefix, String suffix) {
        if (prefix == null || prefix.trim().isEmpty()) return suffix != null ? suffix.trim() : "";
        if (suffix == null || suffix.trim().isEmpty()) return prefix.trim();

        String p = prefix.trim();
        String s = suffix.trim();

        if (p.endsWith("-") && s.startsWith("-")) {
            return p + s.substring(1);
        }
        if (p.endsWith("-") || s.startsWith("-")) {
            return p + s;
        }
        if (Character.isLowerCase(s.charAt(0))) {
            return p + s;
        }
        return p + " " + s;
    }

    public static String normalizeCulture(String cultureOrBiome) {
        if (cultureOrBiome == null || cultureOrBiome.trim().isEmpty()) {
            return "plains";
        }
        String s = cultureOrBiome.toLowerCase().trim();
        if (s.contains("desert") || s.contains("badlands") || s.contains("mesa")) return "desert";
        if (s.contains("savanna") || s.contains("savannah")) return "savanna";
        if (s.contains("taiga") || s.contains("old_growth") || s.contains("spruce") || s.contains("grove")) return "taiga";
        if (s.contains("snow") || s.contains("ice") || s.contains("frozen") || s.contains("cold")) return "snowy";
        if (s.contains("plains") || s.contains("meadow")) return "plains";
        return s;
    }

    /**
     * Tenta localizar um bloco de Waystone dentro dos limites da vila e extrair seu nome via reflexão segura.
     */
    public static String findWaystoneName(LevelAccessor level, SettlementZone zone) {
        if (zone == null) return null;
        SettlementBounds bounds = zone.getStructuralBounds();
        if (bounds == null) bounds = zone.getUrbanCoreBounds();
        if (bounds == null) return null;

        if (customWaystoneResolver != null) {
            String custom = customWaystoneResolver.apply(level, bounds);
            if (custom != null && !custom.trim().isEmpty()) {
                return custom.trim();
            }
        }

        if (level == null) return null;

        try {
            if (!ModList.get().isLoaded("waystones")) {
                return null;
            }

            int minChunkX = bounds.minX() >> 4;
            int maxChunkX = bounds.maxX() >> 4;
            int minChunkZ = bounds.minZ() >> 4;
            int maxChunkZ = bounds.maxZ() >> 4;

            if ((maxChunkX - minChunkX) > 8 || (maxChunkZ - minChunkZ) > 8) {
                maxChunkX = minChunkX + 8;
                maxChunkZ = minChunkZ + 8;
            }

            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                    if (!level.hasChunk(cx, cz)) continue;
                    ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false);
                    if (chunk == null) continue;

                    for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                        if (bounds.contains2D(pos.getX(), pos.getZ())) {
                            BlockEntity be = chunk.getBlockEntity(pos);
                            if (be != null && isWaystoneBlockEntity(be)) {
                                String name = extractWaystoneName(be);
                                if (name != null && !name.trim().isEmpty()) {
                                    return name.trim();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            // Isolamento total contra erros de reflexão de mods terceiros
        }

        return null;
    }

    private static boolean isWaystoneBlockEntity(BlockEntity be) {
        if (be == null) return false;
        String className = be.getClass().getName().toLowerCase();
        return className.contains("waystone");
    }

    private static String extractWaystoneName(BlockEntity be) {
        try {
            Class<?> clazz = be.getClass();

            // 1. Tenta getWaystoneName()
            try {
                Method m = clazz.getMethod("getWaystoneName");
                Object res = m.invoke(be);
                if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            } catch (NoSuchMethodException ignored) {}

            // 2. Tenta getWaystone() -> getName()
            try {
                Method m = clazz.getMethod("getWaystone");
                Object waystoneObj = m.invoke(be);
                if (waystoneObj != null) {
                    Method getNameMethod = waystoneObj.getClass().getMethod("getName");
                    Object nameRes = getNameMethod.invoke(waystoneObj);
                    if (nameRes != null && !nameRes.toString().trim().isEmpty()) {
                        return nameRes.toString().trim();
                    }
                }
            } catch (NoSuchMethodException ignored) {}

            // 3. Tenta getCustomName() se implementa Nameable
            try {
                Method m = clazz.getMethod("getCustomName");
                Object res = m.invoke(be);
                if (res instanceof Component comp) {
                    return comp.getString();
                } else if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            } catch (NoSuchMethodException ignored) {}

            // 4. Tenta getName()
            try {
                Method m = clazz.getMethod("getName");
                Object res = m.invoke(be);
                if (res instanceof Component comp) {
                    return comp.getString();
                } else if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            } catch (NoSuchMethodException ignored) {}
        } catch (Throwable ignored) {}

        return null;
    }
}
