package com.rotacore.world.routes;

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
}
