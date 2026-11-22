package com.rotacore.world.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class WaySegment {
    private final String id;
    private final WayBodyType bodyType;
    private final ConnectionPole startPole;
    private final ConnectionPole endPole;
    private final List<BlockPos> waypoints;
    private int mapColor;
    private boolean constructed;

    public WaySegment(String id, WayBodyType bodyType, ConnectionPole startPole, ConnectionPole endPole,
                      List<BlockPos> waypoints, int mapColor, boolean constructed) {
        this.id = Objects.requireNonNull(id, "ID do segmento nao pode ser nulo");
        this.bodyType = bodyType != null ? bodyType : WayBodyType.ROAD;
        this.startPole = startPole;
        this.endPole = endPole;
        this.waypoints = waypoints != null ? new ArrayList<>(waypoints) : new ArrayList<>();
        this.mapColor = mapColor != 0 ? mapColor : this.bodyType.getDefaultMapColor();
        this.constructed = constructed;
    }

    public WaySegment(String id, WayBodyType bodyType, ConnectionPole startPole, ConnectionPole endPole,
                      List<BlockPos> waypoints) {
        this(id, bodyType, startPole, endPole, waypoints, bodyType != null ? bodyType.getDefaultMapColor() : 0xFFFFFFFF, false);
    }

    public String getId() {
        return id;
    }

    public WayBodyType getBodyType() {
        return bodyType;
    }

    public ConnectionPole getStartPole() {
        return startPole;
    }

    public ConnectionPole getEndPole() {
        return endPole;
    }

    public List<BlockPos> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public int getMapColor() {
        return mapColor;
    }

    public void setMapColor(int mapColor) {
        this.mapColor = mapColor;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean constructed) {
        this.constructed = constructed;
    }

    public double getLengthBlocks() {
        if (waypoints.size() < 2) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            BlockPos a = waypoints.get(i);
            BlockPos b = waypoints.get(i + 1);
            total += Math.sqrt(a.distSqr(b));
        }
        return total;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", this.id);
        tag.putString("body_type", this.bodyType.name());
        if (this.startPole != null) {
            tag.put("start_pole", this.startPole.save());
        }
        if (this.endPole != null) {
            tag.put("end_pole", this.endPole.save());
        }
        tag.putInt("map_color", this.mapColor);
        tag.putBoolean("constructed", this.constructed);

        ListTag wpList = new ListTag();
        for (BlockPos pos : this.waypoints) {
            CompoundTag wpTag = new CompoundTag();
            wpTag.putInt("x", pos.getX());
            wpTag.putInt("y", pos.getY());
            wpTag.putInt("z", pos.getZ());
            wpList.add(wpTag);
        }
        tag.put("waypoints", wpList);
        return tag;
    }

    public static WaySegment load(CompoundTag tag) {
        if (tag == null || !tag.contains("id")) {
            return null;
        }
        String id = tag.getString("id");
        WayBodyType bodyType = WayBodyType.ROAD;
        if (tag.contains("body_type")) {
            try {
                bodyType = WayBodyType.valueOf(tag.getString("body_type"));
            } catch (Exception ignored) {}
        }
        ConnectionPole startPole = tag.contains("start_pole") ? ConnectionPole.load(tag.getCompound("start_pole")) : null;
        ConnectionPole endPole = tag.contains("end_pole") ? ConnectionPole.load(tag.getCompound("end_pole")) : null;
        int color = tag.contains("map_color") ? tag.getInt("map_color") : bodyType.getDefaultMapColor();
        boolean constructed = tag.getBoolean("constructed");

        List<BlockPos> waypoints = new ArrayList<>();
        if (tag.contains("waypoints", Tag.TAG_LIST)) {
            ListTag wpList = tag.getList("waypoints", Tag.TAG_COMPOUND);
            for (int i = 0; i < wpList.size(); i++) {
                CompoundTag wpTag = wpList.getCompound(i);
                waypoints.add(new BlockPos(wpTag.getInt("x"), wpTag.getInt("y"), wpTag.getInt("z")));
            }
        }
        return new WaySegment(id, bodyType, startPole, endPole, waypoints, color, constructed);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("body_type", this.bodyType.name());
        obj.addProperty("map_color", String.format("#%08X", this.mapColor));
        obj.addProperty("constructed", this.constructed);
        if (this.startPole != null) {
            obj.add("start_pole", this.startPole.toJson());
        }
        if (this.endPole != null) {
            obj.add("end_pole", this.endPole.toJson());
        }
        JsonArray wpArr = new JsonArray();
        for (BlockPos p : this.waypoints) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("x", p.getX());
            pObj.addProperty("y", p.getY());
            pObj.addProperty("z", p.getZ());
            wpArr.add(pObj);
        }
        obj.add("waypoints", wpArr);
        return obj;
    }

    public static WaySegment fromJson(JsonObject obj) {
        if (obj == null || !obj.has("id")) {
            return null;
        }
        String id = obj.get("id").getAsString();
        WayBodyType bodyType = WayBodyType.ROAD;
        if (obj.has("body_type")) {
            try {
                bodyType = WayBodyType.valueOf(obj.get("body_type").getAsString());
            } catch (Exception ignored) {}
        }
        ConnectionPole startPole = obj.has("start_pole") ? ConnectionPole.fromJson(obj.getAsJsonObject("start_pole")) : null;
        ConnectionPole endPole = obj.has("end_pole") ? ConnectionPole.fromJson(obj.getAsJsonObject("end_pole")) : null;

        int color = bodyType.getDefaultMapColor();
        if (obj.has("map_color")) {
            String colorStr = obj.get("map_color").getAsString();
            if (colorStr.startsWith("#")) {
                color = (int) Long.parseLong(colorStr.substring(1), 16);
            } else {
                color = obj.get("map_color").getAsInt();
            }
        }
        boolean constructed = obj.has("constructed") && obj.get("constructed").getAsBoolean();

        List<BlockPos> waypoints = new ArrayList<>();
        if (obj.has("waypoints") && obj.get("waypoints").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("waypoints")) {
                if (el.isJsonObject()) {
                    JsonObject pObj = el.getAsJsonObject();
                    waypoints.add(new BlockPos(pObj.get("x").getAsInt(), pObj.get("y").getAsInt(), pObj.get("z").getAsInt()));
                }
            }
        }
        return new WaySegment(id, bodyType, startPole, endPole, waypoints, color, constructed);
    }
}
