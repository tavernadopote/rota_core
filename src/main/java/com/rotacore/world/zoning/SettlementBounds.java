package com.rotacore.world.zoning;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Representa os limites tridimensionais ou bidimensionais (bounding box) de uma zona ou peça de assentamento.
 */
public record SettlementBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public SettlementBounds(int minX, int maxX, int minZ, int maxZ) {
        this(minX, -64, minZ, maxX, 320, maxZ);
    }

    public static SettlementBounds of(int minX, int maxX, int minZ, int maxZ) {
        return new SettlementBounds(
                Math.min(minX, maxX), -64, Math.min(minZ, maxZ),
                Math.max(minX, maxX), 320, Math.max(minZ, maxZ)
        );
    }

    public static SettlementBounds of(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new SettlementBounds(
                Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ)
        );
    }

    public int width() {
        return Math.max(0, maxX - minX + 1);
    }

    public int height() {
        return Math.max(0, maxY - minY + 1);
    }

    public int length() {
        return Math.max(0, maxZ - minZ + 1);
    }

    public int span() {
        return Math.max(Math.abs(maxX - minX), Math.abs(maxZ - minZ));
    }

    public int centerX() {
        return minX + (maxX - minX) / 2;
    }

    public int centerZ() {
        return minZ + (maxZ - minZ) / 2;
    }

    public int centerY() {
        return minY + (maxY - minY) / 2;
    }

    public boolean contains2D(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean contains2D(BlockPos pos) {
        return pos != null && contains2D(pos.getX(), pos.getZ());
    }

    public boolean contains3D(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains3D(BlockPos pos) {
        return pos != null && contains3D(pos.getX(), pos.getY(), pos.getZ());
    }

    public SettlementBounds expanded(int margin) {
        return new SettlementBounds(minX - margin, minY, minZ - margin, maxX + margin, maxY, maxZ + margin);
    }

    public SettlementBounds expanded(int marginX, int marginY, int marginZ) {
        return new SettlementBounds(minX - marginX, minY - marginY, minZ - marginZ, maxX + marginX, maxY + marginY, maxZ + marginZ);
    }

    public SettlementBounds union(SettlementBounds other) {
        if (other == null) return this;
        return new SettlementBounds(
                Math.min(this.minX, other.minX),
                Math.min(this.minY, other.minY),
                Math.min(this.minZ, other.minZ),
                Math.max(this.maxX, other.maxX),
                Math.max(this.maxY, other.maxY),
                Math.max(this.maxZ, other.maxZ)
        );
    }

    public boolean intersects2D(SettlementBounds other) {
        if (other == null) return false;
        return this.maxX >= other.minX && this.minX <= other.maxX &&
               this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("min_x", minX);
        tag.putInt("min_y", minY);
        tag.putInt("min_z", minZ);
        tag.putInt("max_x", maxX);
        tag.putInt("max_y", maxY);
        tag.putInt("max_z", maxZ);
        return tag;
    }

    public static SettlementBounds load(CompoundTag tag) {
        if (tag == null) return new SettlementBounds(0, 0, 0, 0);
        int x1 = tag.contains("min_x") ? tag.getInt("min_x") : (tag.contains("minX") ? tag.getInt("minX") : 0);
        int y1 = tag.contains("min_y") ? tag.getInt("min_y") : (tag.contains("minY") ? tag.getInt("minY") : -64);
        int z1 = tag.contains("min_z") ? tag.getInt("min_z") : (tag.contains("minZ") ? tag.getInt("minZ") : 0);
        int x2 = tag.contains("max_x") ? tag.getInt("max_x") : (tag.contains("maxX") ? tag.getInt("maxX") : 0);
        int y2 = tag.contains("max_y") ? tag.getInt("max_y") : (tag.contains("maxY") ? tag.getInt("maxY") : 320);
        int z2 = tag.contains("max_z") ? tag.getInt("max_z") : (tag.contains("maxZ") ? tag.getInt("maxZ") : 0);
        return new SettlementBounds(x1, y1, z1, x2, y2, z2);
    }
}
