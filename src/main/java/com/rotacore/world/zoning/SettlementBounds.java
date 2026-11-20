/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 */
package com.rotacore.world.zoning;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record SettlementBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public SettlementBounds(int minX, int maxX, int minZ, int maxZ) {
        this(minX, -64, minZ, maxX, 320, maxZ);
    }

    public static SettlementBounds of(int minX, int maxX, int minZ, int maxZ) {
        return new SettlementBounds(Math.min(minX, maxX), -64, Math.min(minZ, maxZ), Math.max(minX, maxX), 320, Math.max(minZ, maxZ));
    }

    public static SettlementBounds of(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new SettlementBounds(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
    }

    public int width() {
        return Math.max(0, this.maxX - this.minX + 1);
    }

    public int height() {
        return Math.max(0, this.maxY - this.minY + 1);
    }

    public int length() {
        return Math.max(0, this.maxZ - this.minZ + 1);
    }

    public int span() {
        return Math.max(Math.abs(this.maxX - this.minX), Math.abs(this.maxZ - this.minZ));
    }

    public int centerX() {
        return this.minX + (this.maxX - this.minX) / 2;
    }

    public int centerZ() {
        return this.minZ + (this.maxZ - this.minZ) / 2;
    }

    public int centerY() {
        return this.minY + (this.maxY - this.minY) / 2;
    }

    public boolean contains2D(int x, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }

    public boolean contains2D(BlockPos pos) {
        return pos != null && this.contains2D(pos.getX(), pos.getZ());
    }

    public boolean contains3D(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    public boolean contains3D(BlockPos pos) {
        return pos != null && this.contains3D(pos.getX(), pos.getY(), pos.getZ());
    }

    public SettlementBounds expanded(int margin) {
        return new SettlementBounds(this.minX - margin, this.minY, this.minZ - margin, this.maxX + margin, this.maxY, this.maxZ + margin);
    }

    public SettlementBounds expanded(int marginX, int marginY, int marginZ) {
        return new SettlementBounds(this.minX - marginX, this.minY - marginY, this.minZ - marginZ, this.maxX + marginX, this.maxY + marginY, this.maxZ + marginZ);
    }

    public SettlementBounds union(SettlementBounds other) {
        if (other == null) {
            return this;
        }
        return new SettlementBounds(Math.min(this.minX, other.minX), Math.min(this.minY, other.minY), Math.min(this.minZ, other.minZ), Math.max(this.maxX, other.maxX), Math.max(this.maxY, other.maxY), Math.max(this.maxZ, other.maxZ));
    }

    public boolean intersects2D(SettlementBounds other) {
        if (other == null) {
            return false;
        }
        return this.maxX >= other.minX && this.minX <= other.maxX && this.maxZ >= other.minZ && this.minZ <= other.maxZ;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("min_x", this.minX);
        tag.putInt("min_y", this.minY);
        tag.putInt("min_z", this.minZ);
        tag.putInt("max_x", this.maxX);
        tag.putInt("max_y", this.maxY);
        tag.putInt("max_z", this.maxZ);
        return tag;
    }

    public static SettlementBounds load(CompoundTag tag) {
        int y2;
        int x2;
        int z1;
        int y1;
        int x1;
        if (tag == null) {
            return new SettlementBounds(0, 0, 0, 0);
        }
        int n = tag.contains("min_x") ? tag.getInt("min_x") : (x1 = tag.contains("minX") ? tag.getInt("minX") : 0);
        int n2 = tag.contains("min_y") ? tag.getInt("min_y") : (y1 = tag.contains("minY") ? tag.getInt("minY") : -64);
        int n3 = tag.contains("min_z") ? tag.getInt("min_z") : (z1 = tag.contains("minZ") ? tag.getInt("minZ") : 0);
        int n4 = tag.contains("max_x") ? tag.getInt("max_x") : (x2 = tag.contains("maxX") ? tag.getInt("maxX") : 0);
        int n5 = tag.contains("max_y") ? tag.getInt("max_y") : (y2 = tag.contains("maxY") ? tag.getInt("maxY") : 320);
        int z2 = tag.contains("max_z") ? tag.getInt("max_z") : (tag.contains("maxZ") ? tag.getInt("maxZ") : 0);
        return new SettlementBounds(x1, y1, z1, x2, y2, z2);
    }
}

