/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.zoning;

import com.rotacore.config.RotaCoreConfig;
import com.rotacore.world.zoning.RegionalCell;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionalGrid {
    private static final Map<Long, RegionalCell> CELL_CACHE = new ConcurrentHashMap<Long, RegionalCell>();
    private static final int MAX_CACHE_SIZE = 4096;

    public static RegionalCell getCellForChunk(long worldSeed, int chunkX, int chunkZ, int cellSizeChunks) {
        int cellX = Math.floorDiv(chunkX, cellSizeChunks);
        int cellZ = Math.floorDiv(chunkZ, cellSizeChunks);
        return RegionalGrid.getCell(worldSeed, cellX, cellZ, cellSizeChunks);
    }

    public static RegionalCell getCell(long worldSeed, int cellX, int cellZ, int cellSizeChunks) {
        long key = RegionalGrid.hashCellKey(worldSeed, cellX, cellZ, cellSizeChunks);
        RegionalCell cached = CELL_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (CELL_CACHE.size() > 4096) {
            CELL_CACHE.clear();
        }
        RegionalCell cell = new RegionalCell(worldSeed, cellX, cellZ, cellSizeChunks);
        CELL_CACHE.put(key, cell);
        return cell;
    }

    public static boolean isCandidateVillageChunk(long worldSeed, int chunkX, int chunkZ, RotaCoreConfig config) {
        if (config == null || !config.enableControlledWorldgen) {
            return true;
        }
        RegionalCell cell = RegionalGrid.getCellForChunk(worldSeed, chunkX, chunkZ, config.regionalCellSizeChunks);
        return cell.getVillageChunkX() == chunkX && cell.getVillageChunkZ() == chunkZ;
    }

    public static boolean isCandidateOutpostChunk(long worldSeed, int chunkX, int chunkZ, RotaCoreConfig config) {
        if (config == null || !config.enableControlledWorldgen) {
            return true;
        }
        RegionalCell cell = RegionalGrid.getCellForChunk(worldSeed, chunkX, chunkZ, config.regionalCellSizeChunks);
        return cell.getOutpostChunkX() == chunkX && cell.getOutpostChunkZ() == chunkZ;
    }

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
        ArrayList<RegionalCell> cells = new ArrayList<RegionalCell>();
        for (int cx = minCellX; cx <= maxCellX; ++cx) {
            for (int cz = minCellZ; cz <= maxCellZ; ++cz) {
                cells.add(RegionalGrid.getCell(worldSeed, cx, cz, cellSizeChunks));
            }
        }
        return cells;
    }

    public static void clearCache() {
        CELL_CACHE.clear();
    }

    private static long hashCellKey(long worldSeed, int cellX, int cellZ, int cellSize) {
        long h = worldSeed;
        h = 31L * h + (long)cellX;
        h = 31L * h + (long)cellZ;
        h = 31L * h + (long)cellSize;
        return h;
    }
}

