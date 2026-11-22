package com.rotacore.world.routes;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public record ConnectionPole(
        BlockPos pos,
        Direction direction,
        WayBodyType preferredBodyType,
        String ownerId,
        boolean isMaritime,
        int magneticRadius
) {
    public static final int DEFAULT_MAGNETIC_RADIUS = 8;

    public ConnectionPole {
        if (direction == null) {
            direction = Direction.NORTH;
        }
        if (preferredBodyType == null) {
            preferredBodyType = isMaritime ? WayBodyType.MARITIME : WayBodyType.ROAD;
        }
        if (ownerId == null) {
            ownerId = "";
        }
        if (magneticRadius <= 0) {
            magneticRadius = DEFAULT_MAGNETIC_RADIUS;
        }
    }

    public ConnectionPole(BlockPos pos, Direction direction, WayBodyType preferredBodyType, String ownerId, boolean isMaritime) {
        this(pos, direction, preferredBodyType, ownerId, isMaritime, DEFAULT_MAGNETIC_RADIUS);
    }

    public ConnectionPole(BlockPos pos, Direction direction, boolean isMaritime) {
        this(pos, direction, isMaritime ? WayBodyType.MARITIME : WayBodyType.ROAD, "", isMaritime, DEFAULT_MAGNETIC_RADIUS);
    }

    public ConnectionPole(BlockPos pos, boolean isMaritime) {
        this(pos, Direction.NORTH, isMaritime ? WayBodyType.MARITIME : WayBodyType.ROAD, "", isMaritime, DEFAULT_MAGNETIC_RADIUS);
    }

    public boolean isWithinMagneticReach(BlockPos targetPos) {
        if (targetPos == null || this.pos == null) {
            return false;
        }
        int dx = this.pos.getX() - targetPos.getX();
        int dz = this.pos.getZ() - targetPos.getZ();
        return (dx * dx + dz * dz) <= (this.magneticRadius * this.magneticRadius);
    }

    public CompoundTag save() {
        return save(new CompoundTag());
    }

    public CompoundTag save(CompoundTag tag) {
        if (tag == null) {
            tag = new CompoundTag();
        }
        tag.putInt("x", this.pos.getX());
        tag.putInt("y", this.pos.getY());
        tag.putInt("z", this.pos.getZ());
        tag.putString("direction", this.direction.name());
        tag.putString("preferred_body", this.preferredBodyType.name());
        tag.putString("owner_id", this.ownerId);
        tag.putBoolean("is_maritime", this.isMaritime);
        tag.putInt("magnetic_radius", this.magneticRadius);
        return tag;
    }

    public static ConnectionPole load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        Direction dir = Direction.NORTH;
        if (tag.contains("direction")) {
            try {
                dir = Direction.valueOf(tag.getString("direction"));
            } catch (Exception ignored) {}
        }
        WayBodyType bodyType = WayBodyType.ROAD;
        if (tag.contains("preferred_body")) {
            try {
                bodyType = WayBodyType.valueOf(tag.getString("preferred_body"));
            } catch (Exception ignored) {}
        }
        String owner = tag.contains("owner_id") ? tag.getString("owner_id") : "";
        boolean maritime = tag.contains("is_maritime") && tag.getBoolean("is_maritime");
        int radius = tag.contains("magnetic_radius") ? tag.getInt("magnetic_radius") : DEFAULT_MAGNETIC_RADIUS;
        return new ConnectionPole(pos, dir, bodyType, owner, maritime, radius);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", this.pos.getX());
        obj.addProperty("y", this.pos.getY());
        obj.addProperty("z", this.pos.getZ());
        obj.addProperty("direction", this.direction.name());
        obj.addProperty("preferred_body", this.preferredBodyType.name());
        obj.addProperty("owner_id", this.ownerId);
        obj.addProperty("is_maritime", this.isMaritime);
        obj.addProperty("magnetic_radius", this.magneticRadius);
        return obj;
    }

    public static ConnectionPole fromJson(JsonObject obj) {
        if (obj == null || !obj.has("x") || !obj.has("z")) {
            return null;
        }
        int x = obj.get("x").getAsInt();
        int y = obj.has("y") ? obj.get("y").getAsInt() : 64;
        int z = obj.get("z").getAsInt();
        Direction dir = Direction.NORTH;
        if (obj.has("direction")) {
            try {
                dir = Direction.valueOf(obj.get("direction").getAsString());
            } catch (Exception ignored) {}
        }
        WayBodyType bodyType = WayBodyType.ROAD;
        if (obj.has("preferred_body")) {
            try {
                bodyType = WayBodyType.valueOf(obj.get("preferred_body").getAsString());
            } catch (Exception ignored) {}
        }
        String owner = obj.has("owner_id") ? obj.get("owner_id").getAsString() : "";
        boolean maritime = obj.has("is_maritime") && obj.get("is_maritime").getAsBoolean();
        int radius = obj.has("magnetic_radius") ? obj.get("magnetic_radius").getAsInt() : DEFAULT_MAGNETIC_RADIUS;
        return new ConnectionPole(new BlockPos(x, y, z), dir, bodyType, owner, maritime, radius);
    }
}
