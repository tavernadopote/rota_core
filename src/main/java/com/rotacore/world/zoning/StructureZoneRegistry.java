/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.StructureTags
 *  net.minecraft.world.level.chunk.ChunkGeneratorStructureState
 *  net.minecraft.world.level.levelgen.structure.BuiltinStructures
 *  net.minecraft.world.level.levelgen.structure.StructureSet
 *  net.minecraft.world.level.levelgen.structure.StructureSet$StructureSelectionEntry
 *  net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
 */
package com.rotacore.world.zoning;

import com.rotacore.world.zoning.ZoneType;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class StructureZoneRegistry {
    private static final Map<ChunkGeneratorStructureState, Map<StructurePlacement, ZoneType>> STATE_CACHE = new WeakHashMap<ChunkGeneratorStructureState, Map<StructurePlacement, ZoneType>>();
    private static final ResourceLocation PILLAGER_OUTPOST_ID = ResourceLocation.fromNamespaceAndPath((String)"minecraft", (String)"pillager_outpost");

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ZoneType getZoneType(ChunkGeneratorStructureState structureState, StructurePlacement placement) {
        Map map;
        if (structureState == null || placement == null) {
            return ZoneType.UNRESTRICTED;
        }
        Map<ChunkGeneratorStructureState, Map<StructurePlacement, ZoneType>> map2 = STATE_CACHE;
        synchronized (map2) {
            map = STATE_CACHE.computeIfAbsent(structureState, StructureZoneRegistry::buildPlacementMap);
        }
        return map.getOrDefault(placement, ZoneType.UNRESTRICTED);
    }

    private static Map<StructurePlacement, ZoneType> buildPlacementMap(ChunkGeneratorStructureState state) {
        ConcurrentHashMap<StructurePlacement, ZoneType> map = new ConcurrentHashMap<StructurePlacement, ZoneType>();
        try {
            for (Holder setHolder : state.possibleStructureSets()) {
                StructureSet set = (StructureSet)setHolder.value();
                StructurePlacement placement = set.placement();
                ZoneType type = StructureZoneRegistry.classifyStructureSet(set);
                map.put(placement, type);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return map;
    }

    private static ZoneType classifyStructureSet(StructureSet set) {
        if (set == null) {
            return ZoneType.UNRESTRICTED;
        }
        for (StructureSet.StructureSelectionEntry entry : set.structures()) {
            Holder structHolder = entry.structure();
            if (structHolder == null) continue;
            if (structHolder.is(StructureTags.VILLAGE)) {
                return ZoneType.SETTLEMENT;
            }
            String path = structHolder.unwrapKey().map(k -> k.location().getPath()).orElse("");
            if (structHolder.is(BuiltinStructures.PILLAGER_OUTPOST) || path.contains("outpost")) {
                return ZoneType.OUTPOST;
            }
            if (!structHolder.is(BuiltinStructures.DESERT_PYRAMID) && !structHolder.is(BuiltinStructures.JUNGLE_TEMPLE) && !structHolder.is(BuiltinStructures.SWAMP_HUT)) continue;
            return ZoneType.LANDMARK;
        }
        return ZoneType.UNRESTRICTED;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearCache() {
        Map<ChunkGeneratorStructureState, Map<StructurePlacement, ZoneType>> map = STATE_CACHE;
        synchronized (map) {
            STATE_CACHE.clear();
        }
    }
}

