/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.ChunkAccess
 *  net.minecraft.world.level.chunk.status.ChunkStatus
 *  net.neoforged.fml.ModList
 */
package com.rotacore.world.names;

import com.rotacore.world.names.VillageNameDataLoader;
import com.rotacore.world.names.VillageNamePack;
import com.rotacore.world.zoning.SettlementBounds;
import com.rotacore.world.zoning.SettlementZone;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.fml.ModList;

public class SettlementNamingService {
    private static BiFunction<LevelAccessor, SettlementBounds, String> customWaystoneResolver = null;

    public static void setCustomWaystoneResolver(BiFunction<LevelAccessor, SettlementBounds, String> resolver) {
        customWaystoneResolver = resolver;
    }

    public static String resolveSettlementName(LevelAccessor level, SettlementZone zone, long worldSeed, int centerX, int centerZ, String cultureOrBiome, String locale) {
        String lang;
        String string = lang = locale != null && !locale.trim().isEmpty() ? locale.trim().toLowerCase() : "pt_br";
        if (zone != null && zone.hasSettlementName()) {
            String existing = zone.getSettlementName();
            return VillageNameDataLoader.getInstance().translateName(existing, cultureOrBiome, lang);
        }
        String waystoneName = SettlementNamingService.findWaystoneName(level, zone);
        if (waystoneName != null && !waystoneName.trim().isEmpty()) {
            String trimmed = waystoneName.trim();
            if (zone != null) {
                zone.setSettlementName(trimmed);
            }
            return trimmed;
        }
        String specificName = VillageNameDataLoader.getInstance().getSpecificName(centerX, centerZ, lang);
        if (specificName != null && !specificName.trim().isEmpty()) {
            String trimmed = specificName.trim();
            if (zone != null) {
                zone.setSettlementName(trimmed);
            }
            return trimmed;
        }
        String procedural = SettlementNamingService.generateProceduralName(worldSeed, centerX, centerZ, cultureOrBiome, lang);
        if (zone != null) {
            zone.setSettlementName(procedural);
        }
        return procedural;
    }

    public static String generateProceduralName(long worldSeed, int centerX, int centerZ, String cultureOrBiome, String locale) {
        String lang = locale != null && !locale.trim().isEmpty() ? locale.trim().toLowerCase() : "pt_br";
        String culture = SettlementNamingService.normalizeCulture(cultureOrBiome);
        VillageNamePack pack = VillageNameDataLoader.getInstance().getPack(culture);
        long combinedSeed = worldSeed ^ ((long)centerX << 32 | (long)centerZ & 0xFFFFFFFFL);
        Random random = new Random(combinedSeed);
        List<String> uniqueNames = pack.getUniqueNames();
        List<String> prefixes = pack.getPrefixes();
        List<String> suffixes = pack.getSuffixes();
        if (!uniqueNames.isEmpty() && (random.nextInt(100) < 35 || prefixes.isEmpty() || suffixes.isEmpty())) {
            int idx = random.nextInt(uniqueNames.size());
            String rawUnique = uniqueNames.get(idx);
            return pack.translate(rawUnique, lang);
        }
        String prefix = prefixes.isEmpty() ? "Vila" : prefixes.get(random.nextInt(prefixes.size()));
        String suffix = suffixes.isEmpty() ? "Nova" : suffixes.get(random.nextInt(suffixes.size()));
        String transPrefix = pack.translate(prefix, lang);
        String transSuffix = pack.translate(suffix, lang);
        return SettlementNamingService.formatCompoundName(transPrefix, transSuffix);
    }

    public static String formatCompoundName(String prefix, String suffix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return suffix != null ? suffix.trim() : "";
        }
        if (suffix == null || suffix.trim().isEmpty()) {
            return prefix.trim();
        }
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
        if (s.contains("desert") || s.contains("badlands") || s.contains("mesa")) {
            return "desert";
        }
        if (s.contains("savanna") || s.contains("savannah")) {
            return "savanna";
        }
        if (s.contains("taiga") || s.contains("old_growth") || s.contains("spruce") || s.contains("grove")) {
            return "taiga";
        }
        if (s.contains("snow") || s.contains("ice") || s.contains("frozen") || s.contains("cold")) {
            return "snowy";
        }
        if (s.contains("plains") || s.contains("meadow")) {
            return "plains";
        }
        return s;
    }

    public static String findWaystoneName(LevelAccessor level, SettlementZone zone) {
        String custom;
        if (zone == null) {
            return null;
        }
        SettlementBounds bounds = zone.getStructuralBounds();
        if (bounds == null) {
            bounds = zone.getUrbanCoreBounds();
        }
        if (bounds == null) {
            return null;
        }
        if (customWaystoneResolver != null && (custom = customWaystoneResolver.apply(level, bounds)) != null && !custom.trim().isEmpty()) {
            return custom.trim();
        }
        if (level == null) {
            return null;
        }
        try {
            if (!ModList.get().isLoaded("waystones")) {
                return null;
            }
            int minChunkX = bounds.minX() >> 4;
            int maxChunkX = bounds.maxX() >> 4;
            int minChunkZ = bounds.minZ() >> 4;
            int maxChunkZ = bounds.maxZ() >> 4;
            if (maxChunkX - minChunkX > 8 || maxChunkZ - minChunkZ > 8) {
                maxChunkX = minChunkX + 8;
                maxChunkZ = minChunkZ + 8;
            }
            for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
                for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                    ChunkAccess chunk;
                    if (!level.hasChunk(cx, cz) || (chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false)) == null) continue;
                    for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                        String name;
                        BlockEntity be;
                        if (!bounds.contains2D(pos.getX(), pos.getZ()) || (be = chunk.getBlockEntity(pos)) == null || !SettlementNamingService.isWaystoneBlockEntity(be) || (name = SettlementNamingService.extractWaystoneName(be)) == null || name.trim().isEmpty()) continue;
                        return name.trim();
                    }
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static boolean isWaystoneBlockEntity(BlockEntity be) {
        if (be == null) {
            return false;
        }
        String className = be.getClass().getName().toLowerCase();
        return className.contains("waystone");
    }

    private static String extractWaystoneName(BlockEntity be) {
        try {
            Component comp;
            Object res;
            Method m2;
            Class<?> clazz = be.getClass();
            try {
                m2 = clazz.getMethod("getWaystoneName", new Class[0]);
                res = m2.invoke((Object)be, new Object[0]);
                if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            }
            catch (NoSuchMethodException m2) {
                // empty catch block
            }
            try {
                Method getNameMethod;
                Object nameRes;
                m2 = clazz.getMethod("getWaystone", new Class[0]);
                Object waystoneObj = m2.invoke((Object)be, new Object[0]);
                if (waystoneObj != null && (nameRes = (getNameMethod = waystoneObj.getClass().getMethod("getName", new Class[0])).invoke(waystoneObj, new Object[0])) != null && !nameRes.toString().trim().isEmpty()) {
                    return nameRes.toString().trim();
                }
            }
            catch (NoSuchMethodException m3) {
                // empty catch block
            }
            try {
                m2 = clazz.getMethod("getCustomName", new Class[0]);
                res = m2.invoke((Object)be, new Object[0]);
                if (res instanceof Component) {
                    comp = (Component)res;
                    return comp.getString();
                }
                if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            }
            catch (NoSuchMethodException m4) {
                // empty catch block
            }
            try {
                m2 = clazz.getMethod("getName", new Class[0]);
                res = m2.invoke((Object)be, new Object[0]);
                if (res instanceof Component) {
                    comp = (Component)res;
                    return comp.getString();
                }
                if (res != null && !res.toString().trim().isEmpty()) {
                    return res.toString().trim();
                }
            }
            catch (NoSuchMethodException noSuchMethodException) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }
}

