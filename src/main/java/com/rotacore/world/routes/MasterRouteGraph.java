/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.phys.Vec3
 */
package com.rotacore.world.routes;

import com.rotacore.world.api.RotaWorldApi;
import com.rotacore.world.naming.RouteNamingEngine;
import com.rotacore.world.naming.RouteNamingStyle;
import com.rotacore.world.routes.ContourRoutePlanner;
import com.rotacore.world.routes.RoadCurvatureMode;
import com.rotacore.world.routes.SplineRoute;
import com.rotacore.world.zone.TerritoryZone;
import com.rotacore.world.zone.TerritoryZonePlanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class MasterRouteGraph {
    private final long worldSeed;
    private final TerritoryZonePlanner zonePlanner;
    private final Map<String, SplineRoute> routesById = new ConcurrentHashMap<String, SplineRoute>();
    private final Map<Long, List<SplineRoute>> chunkRoutesIndex = new ConcurrentHashMap<Long, List<SplineRoute>>();
    private final Set<Long> generatedMacroCells = Collections.newSetFromMap(new ConcurrentHashMap());
    private final SimplePerlinNoise2D topoNoise1;
    private final SimplePerlinNoise2D topoNoise2;

    public MasterRouteGraph(long worldSeed, TerritoryZonePlanner zonePlanner) {
        this.worldSeed = worldSeed;
        this.zonePlanner = Objects.requireNonNull(zonePlanner, "TerritoryZonePlanner nao pode ser nulo");
        this.topoNoise1 = new SimplePerlinNoise2D(worldSeed ^ 0x3C6EF35F5A5A5A5AL);
        this.topoNoise2 = new SimplePerlinNoise2D(worldSeed ^ 0x9E3779B97F4A7C15L);
    }

    public int sampleTopographicHeight(double blockX, double blockZ) {
        double n1 = this.topoNoise1.noise(blockX * 0.003, blockZ * 0.003);
        double n2 = this.topoNoise2.noise(blockX * 0.012, blockZ * 0.012);
        double combined = n1 * 0.7 + n2 * 0.3;
        return (int)Math.round(68.0 + combined * 20.0);
    }

    public long getWorldSeed() {
        return this.worldSeed;
    }

    public TerritoryZonePlanner getZonePlanner() {
        return this.zonePlanner;
    }

    public List<SplineRoute> getRoutesInChunk(int chunkX, int chunkZ, RouteNamingStyle namingStyle) {
        return this.getRoutesInChunk(chunkX, chunkZ, namingStyle, RotaWorldApi.getCurvatureMode());
    }

    public List<SplineRoute> getRoutesInChunk(int chunkX, int chunkZ, RouteNamingStyle namingStyle, RoadCurvatureMode curvatureMode) {
        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;
        int cellX = (int)Math.floor((double)blockX / (double)this.zonePlanner.getCellSize());
        int cellZ = (int)Math.floor((double)blockZ / (double)this.zonePlanner.getCellSize());
        this.ensureRoutesForMacroCell(cellX, cellZ, namingStyle, curvatureMode);
        long chunkKey = ChunkPos.asLong((int)chunkX, (int)chunkZ);
        List<SplineRoute> list = this.chunkRoutesIndex.get(chunkKey);
        return list != null ? list : List.of();
    }

    public void ensureRoutesForMacroCell(int cellX, int cellZ, RouteNamingStyle namingStyle) {
        this.ensureRoutesForMacroCell(cellX, cellZ, namingStyle, RotaWorldApi.getCurvatureMode());
    }

    public void ensureRoutesForMacroCell(int cellX, int cellZ, RouteNamingStyle namingStyle, RoadCurvatureMode curvatureMode) {
        int[][] neighborOffsets;
        long cellKey = MasterRouteGraph.packCellCoords(cellX, cellZ);
        if (!this.generatedMacroCells.add(cellKey)) {
            return;
        }
        TerritoryZone sourceZone = this.zonePlanner.getOrCreateZone(cellX, cellZ, namingStyle);
        for (int[] offset : neighborOffsets = new int[][]{{1, 0}, {0, 1}}) {
            int neighborCellX = cellX + offset[0];
            int neighborCellZ = cellZ + offset[1];
            TerritoryZone targetZone = this.zonePlanner.getOrCreateZone(neighborCellX, neighborCellZ, namingStyle);
            SplineRoute route = this.createConnectingRoute(sourceZone, targetZone, namingStyle, curvatureMode);
            if (route == null) continue;
            this.registerRoute(route);
        }
    }

    public SplineRoute createConnectingRoute(TerritoryZone from, TerritoryZone to, RouteNamingStyle namingStyle) {
        return this.createConnectingRoute(from, to, namingStyle, RotaWorldApi.getCurvatureMode());
    }

    public SplineRoute createConnectingRoute(TerritoryZone from, TerritoryZone to, RouteNamingStyle namingStyle, RoadCurvatureMode curvatureMode) {
        if (from == null || to == null || from.equals(to)) {
            return null;
        }
        RoadCurvatureMode mode = curvatureMode != null ? curvatureMode : RoadCurvatureMode.BALANCED;
        String routeId = "route_" + from.getId() + "_to_" + to.getId();
        SplineRoute existing = this.routesById.get(routeId);
        if (existing != null) {
            return existing;
        }
        BlockPos pStart = from.getViableCenter();
        BlockPos pEnd = to.getViableCenter();
        Vec3 start = new Vec3((double)pStart.getX(), (double)pStart.getY(), (double)pStart.getZ());
        Vec3 end = new Vec3((double)pEnd.getX(), (double)pEnd.getY(), (double)pEnd.getZ());
        long routeSeed = MasterRouteGraph.hashTwoCoords(this.worldSeed, from.getCellGridX(), from.getCellGridZ(), to.getCellGridX(), to.getCellGridZ());
        int seaLevel = 63;
        List<Vec3> controlPoints = ContourRoutePlanner.planContourWaypoints(start, end, mode, this::sampleTopographicHeight, seaLevel, routeSeed);
        SplineRoute.Tier tier = from.getZoneType().isCivil() && to.getZoneType().isCivil() ? SplineRoute.Tier.PRIMARY : SplineRoute.Tier.SECONDARY;
        int routeIndex = Math.abs(from.getCellGridX() * 73 ^ to.getCellGridZ() * 37);
        String routeName = RouteNamingEngine.generateRouteName(routeSeed, tier.name(), from.getName(), to.getName(), namingStyle, routeIndex);
        return new SplineRoute(routeId, routeName, tier, controlPoints);
    }

    public void registerRoute(SplineRoute route) {
        if (route == null) {
            return;
        }
        this.routesById.put(route.getId(), route);
        List<Vec3> discretized = route.discretize(8.0);
        HashSet<Long> touchedChunks = new HashSet<Long>();
        for (Vec3 pt : discretized) {
            int chunkX = (int)Math.floor(pt.x) >> 4;
            int chunkZ = (int)Math.floor(pt.z) >> 4;
            touchedChunks.add(ChunkPos.asLong((int)chunkX, (int)chunkZ));
        }
        for (Long chunkKey : touchedChunks) {
            this.chunkRoutesIndex.computeIfAbsent(chunkKey, k -> new ArrayList()).add(route);
        }
    }

    public SplineRoute getRouteById(String id) {
        return this.routesById.get(id);
    }

    public Collection<SplineRoute> getAllRoutes() {
        return Collections.unmodifiableCollection(this.routesById.values());
    }

    public void clearCache() {
        this.routesById.clear();
        this.chunkRoutesIndex.clear();
        this.generatedMacroCells.clear();
    }

    private static long packCellCoords(int cellX, int cellZ) {
        return (long)cellX << 32 | (long)cellZ & 0xFFFFFFFFL;
    }

    private static long hashTwoCoords(long seed, int x1, int z1, int x2, int z2) {
        long h = seed ^ (long)x1 * 341873128712L + (long)z1 * 132897987541L;
        h ^= (long)x2 * 987654321123L + (long)z2 * 456789123321L;
        h ^= h >> 33;
        h *= -49064778989728563L;
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

