package com.rotacore.world.zoning;

import com.rotacore.config.RotaCoreConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Motor matemático determinístico de Zoneamento Regional.
 * Particiona o mundo infinito em células regionais $O(1)$ garantindo espaçamento harmônico
 * e posições ideais para assentamentos civis e postos militares.
 */
public class RegionalGrid {

    private static final Map<Long, RegionalCell> CELL_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 4096;

    /**
     * Obtém a célula regional correspondente a um chunk específico.
     */
    public static RegionalCell getCellForChunk(long worldSeed, int chunkX, int chunkZ, int cellSizeChunks) {
        int cellX = Math.floorDiv(chunkX, cellSizeChunks);
        int cellZ = Math.floorDiv(chunkZ, cellSizeChunks);
        return getCell(worldSeed, cellX, cellZ, cellSizeChunks);
    }

    /**
     * Obtém uma célula regional pelos índices de grid (cellX, cellZ).
     */
    public static RegionalCell getCell(long worldSeed, int cellX, int cellZ, int cellSizeChunks) {
        long key = hashCellKey(worldSeed, cellX, cellZ, cellSizeChunks);
        RegionalCell cached = CELL_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        if (CELL_CACHE.size() > MAX_CACHE_SIZE) {
            CELL_CACHE.clear();
        }

        RegionalCell cell = new RegionalCell(worldSeed, cellX, cellZ, cellSizeChunks);
        CELL_CACHE.put(key, cell);
        return cell;
    }

    /**
     * Determina se o chunk informado é o chunk eleito para a Vila de sua célula regional.
     */
    public static boolean isCandidateVillageChunk(long worldSeed, int chunkX, int chunkZ, RotaCoreConfig config) {
        if (config == null || !config.enableControlledWorldgen) {
            return true;
        }
        RegionalCell cell = getCellForChunk(worldSeed, chunkX, chunkZ, config.regionalCellSizeChunks);
        return (cell.getVillageChunkX() == chunkX && cell.getVillageChunkZ() == chunkZ);
    }

    /**
     * Determina se o chunk informado é o chunk eleito para o Posto Militar de sua célula regional.
     */
    public static boolean isCandidateOutpostChunk(long worldSeed, int chunkX, int chunkZ, RotaCoreConfig config) {
        if (config == null || !config.enableControlledWorldgen) {
            return true;
        }
        RegionalCell cell = getCellForChunk(worldSeed, chunkX, chunkZ, config.regionalCellSizeChunks);
        return (cell.getOutpostChunkX() == chunkX && cell.getOutpostChunkZ() == chunkZ);
    }

    /**
     * Retorna todas as células regionais contidas dentro de um raio a partir de um ponto central (ex: World Spawn).
     */
    public static List<RegionalCell> getCellsInRadius(long worldSeed, int centerBlockX, int centerBlockZ, int radiusBlocks, int cellSizeChunks) {
        int minBlockX = centerBlockX - radiusBlocks;
        int maxBlockX = centerBlockX + radiusBlocks;
        int minBlockZ = centerBlockZ - radiusBlocks;
        int maxBlockZ = centerBlockZ + radiusBlocks;

        int minChunkX = minBlockX >> 4;
        int maxChunkX = maxBlockX >> 4;
        int minChunkZ = minBlockZ >> 4;
        int maxChunkZ = maxBlockZ >> 4;

        int minCellX = Math.floorDiv(minChunkX, cellSizeChunks);
        int maxCellX = Math.floorDiv(maxChunkX, cellSizeChunks);
        int minCellZ = Math.floorDiv(minChunkZ, cellSizeChunks);
        int maxCellZ = Math.floorDiv(maxChunkZ, cellSizeChunks);

        List<RegionalCell> cells = new ArrayList<>();
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                cells.add(getCell(worldSeed, cx, cz, cellSizeChunks));
            }
        }
        return cells;
    }

    public static void clearCache() {
        CELL_CACHE.clear();
    }

    private static long hashCellKey(long worldSeed, int cellX, int cellZ, int cellSize) {
        long h = worldSeed;
        h = 31 * h + cellX;
        h = 31 * h + cellZ;
        h = 31 * h + cellSize;
        return h;
    }
}
