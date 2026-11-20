/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.rotacore.world.zone;

import com.rotacore.world.naming.RouteNamingEngine;
import com.rotacore.world.naming.RouteNamingStyle;
import com.rotacore.world.zone.TerritoryZone;
import com.rotacore.world.zone.ZoneType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;

public class TerritoryZonePlanner {
    public static final int DEFAULT_CELL_SIZE = 800;
    public static final double WARP_AMPLITUDE = 128.0;
    public static final double WARP_FREQUENCY = 0.003125;
    private final long worldSeed;
    private final int cellSize;
    private final Map<Long, TerritoryZone> zoneCache = new ConcurrentHashMap<Long, TerritoryZone>();
    private final SimplePerlinNoise2D warpNoiseX;
    private final SimplePerlinNoise2D warpNoiseZ;

    public TerritoryZonePlanner(long worldSeed) {
        this(worldSeed, 800);
    }

    public TerritoryZonePlanner(long worldSeed, int cellSize) {
        this.worldSeed = worldSeed;
        this.cellSize = Math.max(300, cellSize);
        this.warpNoiseX = new SimplePerlinNoise2D(worldSeed ^ 0xA5A5A5A55A5A5A5AL);
        this.warpNoiseZ = new SimplePerlinNoise2D(worldSeed ^ 0x5B5B5B5BB5B5B5B5L);
    }

    public long getWorldSeed() {
        return this.worldSeed;
    }

    public int getCellSize() {
        return this.cellSize;
    }

    public double[] getWarpedCoordinates(double blockX, double blockZ) {
        double dx = this.warpNoiseX.noise(blockX * 0.003125, blockZ * 0.003125) * 128.0;
        double dz = this.warpNoiseZ.noise(blockX * 0.003125, blockZ * 0.003125) * 128.0;
        return new double[]{blockX + dx, blockZ + dz};
    }

    public TerritoryZone getZoneAt(int blockX, int blockZ, RouteNamingStyle namingStyle) {
        double[] warped = this.getWarpedCoordinates(blockX, blockZ);
        double wx = warped[0];
        double wz = warped[1];
        int approxCellX = (int)Math.floor(wx / (double)this.cellSize);
        int approxCellZ = (int)Math.floor(wz / (double)this.cellSize);
        int bestCellX = approxCellX;
        int bestCellZ = approxCellZ;
        double minDistanceSq = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                int cx = approxCellX + dx;
                int cz = approxCellZ + dz;
                int[] seedPos = this.getSeedPointForCell(cx, cz);
                double distSq = TerritoryZonePlanner.distanceSq(wx, wz, seedPos[0], seedPos[1]);
                if (!(distSq < minDistanceSq)) continue;
                minDistanceSq = distSq;
                bestCellX = cx;
                bestCellZ = cz;
            }
        }
        return this.getOrCreateZone(bestCellX, bestCellZ, namingStyle);
    }

    public int[] getSeedPointForCell(int cellX, int cellZ) {
        long cellSeed = TerritoryZonePlanner.hashCoords(this.worldSeed, cellX, cellZ);
        int margin = (int)((double)this.cellSize * 0.15);
        int range = this.cellSize - margin * 2;
        int offsetX = margin + (int)(Math.abs(cellSeed) % (long)range);
        int offsetZ = margin + (int)(Math.abs(cellSeed >> 16) % (long)range);
        int seedX = cellX * this.cellSize + offsetX;
        int seedZ = cellZ * this.cellSize + offsetZ;
        return new int[]{seedX, seedZ};
    }

    public TerritoryZone getOrCreateZone(int cellX, int cellZ, RouteNamingStyle namingStyle) {
        long key = TerritoryZonePlanner.packCellCoords(cellX, cellZ);
        return this.zoneCache.computeIfAbsent(key, k -> this.createZone(cellX, cellZ, namingStyle));
    }

    private TerritoryZone createZone(int cellX, int cellZ, RouteNamingStyle namingStyle) {
        long zoneSeed = TerritoryZonePlanner.hashCoords(this.worldSeed, cellX, cellZ);
        String zoneId = "zone_" + cellX + "_" + cellZ;
        int typeRoll = (int)(Math.abs(zoneSeed ^ 0xFFFFFFFFC2B2AE35L) % 100L);
        ZoneType zoneType = typeRoll < 70 ? ZoneType.CIVIL_KINGDOM : (typeRoll < 85 ? ZoneType.WILDERNESS : ZoneType.PILLAGER_DOMAIN);
        int[] seedPoint = this.getSeedPointForCell(cellX, cellZ);
        BlockPos viableCenter = new BlockPos(seedPoint[0], 64, seedPoint[1]);
        int zoneIndex = Math.abs(cellX * 31 ^ cellZ * 17);
        String name = RouteNamingEngine.generateZoneName(zoneSeed, zoneType.name(), "plains", namingStyle, zoneIndex);
        int minX = cellX * this.cellSize - 128;
        int minZ = cellZ * this.cellSize - 128;
        int maxX = (cellX + 1) * this.cellSize + 128;
        int maxZ = (cellZ + 1) * this.cellSize + 128;
        return new TerritoryZone(zoneId, name, zoneType, viableCenter, cellX, cellZ, seedPoint[0], seedPoint[1], "plains", minX, minZ, maxX, maxZ);
    }

    public void clearCache() {
        this.zoneCache.clear();
    }

    private static double distanceSq(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    private static long packCellCoords(int cellX, int cellZ) {
        return (long)cellX << 32 | (long)cellZ & 0xFFFFFFFFL;
    }

    private static long hashCoords(long seed, int x, int z) {
        long h = seed ^ (long)x * 341873128712L + (long)z * 132897987541L;
        h ^= h >> 33;
        h *= -49064778989728563L;
        h ^= h >> 33;
        h *= -4265267296055464877L;
        h ^= h >> 33;
        return h;
    }

    private static class SimplePerlinNoise2D {
        private final int[] permutation = new int[512];

        public SimplePerlinNoise2D(long seed) {
            int i;
            int[] p = new int[256];
            for (int i2 = 0; i2 < 256; ++i2) {
                p[i2] = i2;
            }
            long s = seed;
            for (i = 255; i > 0; --i) {
                s = s * 6364136223846793005L + 1442695040888963407L;
                int j = (int)(Math.abs(s) % (long)(i + 1));
                int tmp = p[i];
                p[i] = p[j];
                p[j] = tmp;
            }
            for (i = 0; i < 512; ++i) {
                this.permutation[i] = p[i & 0xFF];
            }
        }

        public double noise(double x, double z) {
            int xi = (int)Math.floor(x) & 0xFF;
            int zi = (int)Math.floor(z) & 0xFF;
            double xf = x - Math.floor(x);
            double zf = z - Math.floor(z);
            double u = SimplePerlinNoise2D.fade(xf);
            double v = SimplePerlinNoise2D.fade(zf);
            int aa = this.permutation[this.permutation[xi] + zi];
            int ab = this.permutation[this.permutation[xi] + zi + 1];
            int ba = this.permutation[this.permutation[xi + 1] + zi];
            int bb = this.permutation[this.permutation[xi + 1] + zi + 1];
            double x1 = SimplePerlinNoise2D.lerp(u, SimplePerlinNoise2D.grad(aa, xf, zf), SimplePerlinNoise2D.grad(ba, xf - 1.0, zf));
            double x2 = SimplePerlinNoise2D.lerp(u, SimplePerlinNoise2D.grad(ab, xf, zf - 1.0), SimplePerlinNoise2D.grad(bb, xf - 1.0, zf - 1.0));
            return SimplePerlinNoise2D.lerp(v, x1, x2);
        }

        private static double fade(double t) {
            return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
        }

        private static double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        private static double grad(int hash, double x, double z) {
            int h = hash & 3;
            double u = h < 2 ? x : z;
            double v = h < 2 ? z : x;
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
}

