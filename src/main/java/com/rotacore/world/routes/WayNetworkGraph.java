package com.rotacore.world.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class WayNetworkGraph {
    private final Map<String, WayChain> chainsById = new ConcurrentHashMap<>();

    public void addChain(WayChain chain) {
        if (chain != null && chain.getId() != null) {
            this.chainsById.put(chain.getId(), chain);
        }
    }

    public WayChain getChain(String id) {
        return id != null ? this.chainsById.get(id) : null;
    }

    public List<WayChain> getAllChains() {
        return new ArrayList<>(this.chainsById.values());
    }

    public void clear() {
        this.chainsById.clear();
    }

    public record ProjectionResult(WayChain chain, WaySegment segment, BlockPos closestPoint, double distanceBlocks) {}

    public ProjectionResult findClosestAnchor(BlockPos targetPos, double maxDistanceBlocks) {
        return findClosestAnchor(targetPos, maxDistanceBlocks, 6.0);
    }

    public ProjectionResult findClosestAnchor(BlockPos targetPos, double maxDistanceBlocks, double maxVerticalDelta) {
        if (targetPos == null) {
            return null;
        }
        ProjectionResult best = null;
        double bestDistSq = maxDistanceBlocks * maxDistanceBlocks;

        for (WayChain chain : this.chainsById.values()) {
            for (WaySegment seg : chain.getSegments()) {
                WayBodyConfig cfg = WayBodyRegistry.getConfig(seg.getBodyType());
                if (cfg != null && !cfg.isAllowBodyMagnetism()) {
                    continue;
                }
                List<BlockPos> waypoints = seg.getWaypoints();
                for (int i = 0; i < waypoints.size() - 1; i++) {
                    BlockPos a = waypoints.get(i);
                    BlockPos b = waypoints.get(i + 1);
                    BlockPos closest = projectPointOnSegment(targetPos, a, b);
                    if (maxVerticalDelta > 0 && Math.abs(targetPos.getY() - closest.getY()) > maxVerticalDelta) {
                        continue;
                    }
                    double distSq = targetPos.distSqr(closest);
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = new ProjectionResult(chain, seg, closest, Math.sqrt(distSq));
                    }
                }
            }
        }
        return best;
    }

    private static BlockPos projectPointOnSegment(BlockPos p, BlockPos a, BlockPos b) {
        double px = p.getX();
        double py = p.getY();
        double pz = p.getZ();
        double ax = a.getX();
        double ay = a.getY();
        double az = a.getZ();
        double bx = b.getX();
        double by = b.getY();
        double bz = b.getZ();

        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double lenSq = dx * dx + dy * dy + dz * dz;
        if (lenSq < 1e-6) {
            return a;
        }

        double t = ((px - ax) * dx + (py - ay) * dy + (pz - az) * dz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        return new BlockPos(
                (int) Math.round(ax + t * dx),
                (int) Math.round(ay + t * dy),
                (int) Math.round(az + t * dz)
        );
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag chainList = new ListTag();
        for (WayChain chain : this.chainsById.values()) {
            chainList.add(chain.save());
        }
        tag.put("chains", chainList);
        return tag;
    }

    public static WayNetworkGraph load(CompoundTag tag) {
        WayNetworkGraph graph = new WayNetworkGraph();
        if (tag != null && tag.contains("chains", Tag.TAG_LIST)) {
            ListTag chainList = tag.getList("chains", Tag.TAG_COMPOUND);
            for (int i = 0; i < chainList.size(); i++) {
                WayChain chain = WayChain.load(chainList.getCompound(i));
                if (chain != null) {
                    graph.addChain(chain);
                }
            }
        }
        return graph;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        for (WayChain chain : this.chainsById.values()) {
            arr.add(chain.toJson());
        }
        obj.add("chains", arr);
        return obj;
    }

    public static WayNetworkGraph fromJson(JsonObject obj) {
        WayNetworkGraph graph = new WayNetworkGraph();
        if (obj != null && obj.has("chains") && obj.get("chains").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("chains")) {
                if (el.isJsonObject()) {
                    WayChain chain = WayChain.fromJson(el.getAsJsonObject());
                    if (chain != null) {
                        graph.addChain(chain);
                    }
                }
            }
        }
        return graph;
    }
}
