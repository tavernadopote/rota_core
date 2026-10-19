package com.rotacore.world.zoning;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro dinâmico e inteligente de classificação de estruturas.
 * Mapeia automaticamente estruturas vanilla e de mods (como CTOV, Towns and Towers, etc.)
 * para suas respectivas zonas funcionais (SETTLEMENT, OUTPOST, LANDMARK, UNRESTRICTED).
 */
public class StructureZoneRegistry {

    private static final Map<ChunkGeneratorStructureState, Map<StructurePlacement, ZoneType>> STATE_CACHE = new WeakHashMap<>();
    private static final ResourceLocation PILLAGER_OUTPOST_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "pillager_outpost");

    /**
     * Identifica o ZoneType associado a um determinado StructurePlacement no estado de estruturas do mundo.
     */
    public static ZoneType getZoneType(ChunkGeneratorStructureState structureState, StructurePlacement placement) {
        if (structureState == null || placement == null) {
            return ZoneType.UNRESTRICTED;
        }

        Map<StructurePlacement, ZoneType> map;
        synchronized (STATE_CACHE) {
            map = STATE_CACHE.computeIfAbsent(structureState, StructureZoneRegistry::buildPlacementMap);
        }

        return map.getOrDefault(placement, ZoneType.UNRESTRICTED);
    }

    private static Map<StructurePlacement, ZoneType> buildPlacementMap(ChunkGeneratorStructureState state) {
        Map<StructurePlacement, ZoneType> map = new ConcurrentHashMap<>();

        try {
            for (Holder<StructureSet> setHolder : state.possibleStructureSets()) {
                StructureSet set = setHolder.value();
                StructurePlacement placement = set.placement();
                ZoneType type = classifyStructureSet(set);
                map.put(placement, type);
            }
        } catch (Exception ignored) {}

        return map;
    }

    private static ZoneType classifyStructureSet(StructureSet set) {
        if (set == null) return ZoneType.UNRESTRICTED;

        for (StructureSet.StructureSelectionEntry entry : set.structures()) {
            Holder<Structure> structHolder = entry.structure();
            if (structHolder == null) continue;

            // 1. Vilas civis (Vanilla ou Mods que usam a tag oficial de vila como CTOV)
            if (structHolder.is(StructureTags.VILLAGE)) {
                return ZoneType.SETTLEMENT;
            }

            // 2. Posto Militar Hostil (Pillager Outpost)
            String path = structHolder.unwrapKey().map(k -> k.location().getPath()).orElse("");
            if (structHolder.is(net.minecraft.world.level.levelgen.structure.BuiltinStructures.PILLAGER_OUTPOST) || path.contains("outpost")) {
                return ZoneType.OUTPOST;
            }

            // 3. Templos e Marcos Históricos
            if (structHolder.is(net.minecraft.world.level.levelgen.structure.BuiltinStructures.DESERT_PYRAMID) ||
                structHolder.is(net.minecraft.world.level.levelgen.structure.BuiltinStructures.JUNGLE_TEMPLE) ||
                structHolder.is(net.minecraft.world.level.levelgen.structure.BuiltinStructures.SWAMP_HUT)) {
                return ZoneType.LANDMARK;
            }
        }

        return ZoneType.UNRESTRICTED;
    }

    public static void clearCache() {
        synchronized (STATE_CACHE) {
            STATE_CACHE.clear();
        }
    }
}
