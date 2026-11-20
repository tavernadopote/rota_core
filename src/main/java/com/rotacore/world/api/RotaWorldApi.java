/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 */
package com.rotacore.world.api;

import com.rotacore.RotaCore;
import com.rotacore.world.naming.RouteNamingStyle;
import com.rotacore.world.routes.MasterRouteGraph;
import com.rotacore.world.routes.RoadCurvatureMode;
import com.rotacore.world.routes.SplineRoute;
import com.rotacore.world.zone.TerritoryZone;
import com.rotacore.world.zone.TerritoryZonePlanner;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class RotaWorldApi {
    private static final Map<Long, WorldStateHolder> WORLD_STATES = new ConcurrentHashMap<Long, WorldStateHolder>();
    private static volatile RouteNamingStyle overrideNamingStyle = null;
    private static volatile RoadCurvatureMode overrideCurvatureMode = null;

    private static WorldStateHolder getState(long seed) {
        return WORLD_STATES.computeIfAbsent(seed, WorldStateHolder::new);
    }

    public static RouteNamingStyle getNamingStyle() {
        if (overrideNamingStyle != null) {
            return overrideNamingStyle;
        }
        try {
            if (RotaCore.getConfig() != null && RotaCore.getConfig().routeNamingStyle != null) {
                return RotaCore.getConfig().routeNamingStyle;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return RouteNamingStyle.FANTASY_KINGDOM;
    }

    public static void setNamingStyle(RouteNamingStyle style) {
        overrideNamingStyle = style;
        try {
            if (RotaCore.getConfig() != null && style != null) {
                RotaCore.getConfig().routeNamingStyle = style;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static RoadCurvatureMode getCurvatureMode() {
        if (overrideCurvatureMode != null) {
            return overrideCurvatureMode;
        }
        try {
            if (RotaCore.getConfig() != null && RotaCore.getConfig().roadCurvatureMode != null) {
                return RotaCore.getConfig().roadCurvatureMode;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return RoadCurvatureMode.BALANCED;
    }

    public static void setCurvatureMode(RoadCurvatureMode mode) {
        overrideCurvatureMode = mode;
        try {
            if (RotaCore.getConfig() != null && mode != null) {
                RotaCore.getConfig().roadCurvatureMode = mode;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static TerritoryZone getZoneAt(long worldSeed, int blockX, int blockZ) {
        WorldStateHolder state = RotaWorldApi.getState(worldSeed);
        return state.zonePlanner.getZoneAt(blockX, blockZ, RotaWorldApi.getNamingStyle());
    }

    public static TerritoryZone getZoneAt(Level level, int blockX, int blockZ) {
        long seed = RotaWorldApi.extractSeed(level);
        return RotaWorldApi.getZoneAt(seed, blockX, blockZ);
    }

    public static List<SplineRoute> getRoutesInChunk(long worldSeed, int chunkX, int chunkZ) {
        return RotaWorldApi.getRoutesInChunk(worldSeed, chunkX, chunkZ, RotaWorldApi.getCurvatureMode());
    }

    public static List<SplineRoute> getRoutesInChunk(long worldSeed, int chunkX, int chunkZ, RoadCurvatureMode curvatureMode) {
        WorldStateHolder state = RotaWorldApi.getState(worldSeed);
        return state.routeGraph.getRoutesInChunk(chunkX, chunkZ, RotaWorldApi.getNamingStyle(), curvatureMode);
    }

    public static List<SplineRoute> getRoutesInChunk(Level level, ChunkPos chunkPos) {
        return RotaWorldApi.getRoutesInChunk(level, chunkPos, RotaWorldApi.getCurvatureMode());
    }

    public static List<SplineRoute> getRoutesInChunk(Level level, ChunkPos chunkPos, RoadCurvatureMode curvatureMode) {
        if (chunkPos == null) {
            return List.of();
        }
        long seed = RotaWorldApi.extractSeed(level);
        return RotaWorldApi.getRoutesInChunk(seed, chunkPos.x, chunkPos.z, curvatureMode);
    }

    public static String getRouteDisplayName(SplineRoute route) {
        if (route == null) {
            return "";
        }
        if (RotaWorldApi.getNamingStyle() == RouteNamingStyle.DISABLED) {
            return "";
        }
        return route.getName();
    }

    public static String getZoneDisplayName(TerritoryZone zone) {
        if (zone == null) {
            return "";
        }
        if (RotaWorldApi.getNamingStyle() == RouteNamingStyle.DISABLED) {
            return "";
        }
        return zone.getName();
    }

    public static TerritoryZonePlanner getZonePlanner(long worldSeed) {
        return RotaWorldApi.getState((long)worldSeed).zonePlanner;
    }

    public static MasterRouteGraph getRouteGraph(long worldSeed) {
        return RotaWorldApi.getState((long)worldSeed).routeGraph;
    }

    public static void clearCache() {
        WORLD_STATES.clear();
        overrideNamingStyle = null;
        overrideCurvatureMode = null;
    }

    private static long extractSeed(Level level) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return serverLevel.getSeed();
        }
        return 1234567890L;
    }

    private static class WorldStateHolder {
        final TerritoryZonePlanner zonePlanner;
        final MasterRouteGraph routeGraph;

        WorldStateHolder(long seed) {
            this.zonePlanner = new TerritoryZonePlanner(seed);
            this.routeGraph = new MasterRouteGraph(seed, this.zonePlanner);
        }
    }
}

