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

public class WayChain {
    private final String id;
    private String name;
    private String originStructureId;
    private String targetStructureId;
    private final List<WaySegment> segments;

    public WayChain(String id, String name, String originStructureId, String targetStructureId, List<WaySegment> segments) {
        this.id = Objects.requireNonNull(id, "ID da cadeia nao pode ser nulo");
        this.name = name != null ? name : id;
        this.originStructureId = originStructureId != null ? originStructureId : "";
        this.targetStructureId = targetStructureId != null ? targetStructureId : "";
        this.segments = new ArrayList<>(segments != null ? segments : Collections.emptyList());
    }

    public WayChain(String id, String name, String originStructureId, String targetStructureId) {
        this(id, name, originStructureId, targetStructureId, Collections.emptyList());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginStructureId() {
        return originStructureId;
    }

    public void setOriginStructureId(String originStructureId) {
        this.originStructureId = originStructureId;
    }

    public String getTargetStructureId() {
        return targetStructureId;
    }

    public void setTargetStructureId(String targetStructureId) {
        this.targetStructureId = targetStructureId;
    }

    public List<WaySegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public void addSegment(WaySegment segment) {
        if (segment != null) {
            this.segments.add(segment);
        }
    }

    public ConnectionPole getStartPole() {
        if (this.segments.isEmpty()) {
            return null;
        }
        return this.segments.get(0).getStartPole();
    }

    public ConnectionPole getEndPole() {
        if (this.segments.isEmpty()) {
            return null;
        }
        return this.segments.get(this.segments.size() - 1).getEndPole();
    }

    public List<BlockPos> getAllWaypoints() {
        List<BlockPos> all = new ArrayList<>();
        for (WaySegment seg : this.segments) {
            all.addAll(seg.getWaypoints());
        }
        return all;
    }

    public double getTotalLengthBlocks() {
        double total = 0.0;
        for (WaySegment seg : this.segments) {
            total += seg.getLengthBlocks();
        }
        return total;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", this.id);
        tag.putString("name", this.name);
        tag.putString("origin_structure_id", this.originStructureId);
        tag.putString("target_structure_id", this.targetStructureId);

        ListTag segList = new ListTag();
        for (WaySegment seg : this.segments) {
            segList.add(seg.save());
        }
        tag.put("segments", segList);
        return tag;
    }

    public static WayChain load(CompoundTag tag) {
        if (tag == null || !tag.contains("id")) {
            return null;
        }
        String id = tag.getString("id");
        String name = tag.contains("name") ? tag.getString("name") : id;
        String originId = tag.getString("origin_structure_id");
        String targetId = tag.getString("target_structure_id");

        List<WaySegment> segments = new ArrayList<>();
        if (tag.contains("segments", Tag.TAG_LIST)) {
            ListTag segList = tag.getList("segments", Tag.TAG_COMPOUND);
            for (int i = 0; i < segList.size(); i++) {
                WaySegment seg = WaySegment.load(segList.getCompound(i));
                if (seg != null) {
                    segments.add(seg);
                }
            }
        }
        return new WayChain(id, name, originId, targetId, segments);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("name", this.name);
        obj.addProperty("origin_structure_id", this.originStructureId);
        obj.addProperty("target_structure_id", this.targetStructureId);

        JsonArray segArr = new JsonArray();
        for (WaySegment seg : this.segments) {
            segArr.add(seg.toJson());
        }
        obj.add("segments", segArr);
        return obj;
    }

    public static WayChain fromJson(JsonObject obj) {
        if (obj == null || !obj.has("id")) {
            return null;
        }
        String id = obj.get("id").getAsString();
        String name = obj.has("name") ? obj.get("name").getAsString() : id;
        String originId = obj.has("origin_structure_id") ? obj.get("origin_structure_id").getAsString() : "";
        String targetId = obj.has("target_structure_id") ? obj.get("target_structure_id").getAsString() : "";

        List<WaySegment> segments = new ArrayList<>();
        if (obj.has("segments") && obj.get("segments").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("segments")) {
                if (el.isJsonObject()) {
                    WaySegment seg = WaySegment.fromJson(el.getAsJsonObject());
                    if (seg != null) {
                        segments.add(seg);
                    }
                }
            }
        }
        return new WayChain(id, name, originId, targetId, segments);
    }
}
