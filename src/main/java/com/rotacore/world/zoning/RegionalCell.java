/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.world.zoning;

public class RegionalCell {
    private final int cellX;
    private final int cellZ;
    private final int cellSizeChunks;
    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;
    private final int villageChunkX;
    private final int villageChunkZ;
    private final int outpostChunkX;
    private final int outpostChunkZ;

    public RegionalCell(long worldSeed, int cellX, int cellZ, int cellSizeChunks) {
        this.cellX = cellX;
        this.cellZ = cellZ;
        this.cellSizeChunks = Math.max(16, cellSizeChunks);
        this.minChunkX = cellX * this.cellSizeChunks;
        this.maxChunkX = this.minChunkX + this.cellSizeChunks - 1;
        this.minChunkZ = cellZ * this.cellSizeChunks;
        this.maxChunkZ = this.minChunkZ + this.cellSizeChunks - 1;
        long h = (worldSeed ^ 0x5DEECE66DL) + (long)cellX * 341873128712L + (long)cellZ * 132897987541L;
        h = (h ^ h >>> 16) * 2246822507L;
        h = (h ^ h >>> 13) * 3266489909L;
        long cellSeed = h ^ h >>> 16;
        int margin = Math.max(4, this.cellSizeChunks / 6);
        int span = Math.max(1, this.cellSizeChunks - margin * 2);
        int vOffsetX = margin + (int)Math.abs(cellSeed % (long)span);
        int vOffsetZ = margin + (int)Math.abs(cellSeed / 31L % (long)span);
        this.villageChunkX = this.minChunkX + vOffsetX;
        this.villageChunkZ = this.minChunkZ + vOffsetZ;
        int borderMargin = 2;
        int oOffsetX = vOffsetX < this.cellSizeChunks / 2 ? this.cellSizeChunks - borderMargin - 1 - (int)Math.abs(cellSeed / 17L % 4L) : borderMargin + (int)Math.abs(cellSeed / 17L % 4L);
        int oOffsetZ = vOffsetZ < this.cellSizeChunks / 2 ? this.cellSizeChunks - borderMargin - 1 - (int)Math.abs(cellSeed / 23L % 4L) : borderMargin + (int)Math.abs(cellSeed / 23L % 4L);
        this.outpostChunkX = this.minChunkX + oOffsetX;
        this.outpostChunkZ = this.minChunkZ + oOffsetZ;
    }

    public int getCellX() {
        return this.cellX;
    }

    public int getCellZ() {
        return this.cellZ;
    }

    public int getCellSizeChunks() {
        return this.cellSizeChunks;
    }

    public int getMinChunkX() {
        return this.minChunkX;
    }

    public int getMaxChunkX() {
        return this.maxChunkX;
    }

    public int getMinChunkZ() {
        return this.minChunkZ;
    }

    public int getMaxChunkZ() {
        return this.maxChunkZ;
    }

    public int getMinBlockX() {
        return this.minChunkX << 4;
    }

    public int getMaxBlockX() {
        return (this.maxChunkX << 4) + 15;
    }

    public int getMinBlockZ() {
        return this.minChunkZ << 4;
    }

    public int getMaxBlockZ() {
        return (this.maxChunkZ << 4) + 15;
    }

    public int getVillageChunkX() {
        return this.villageChunkX;
    }

    public int getVillageChunkZ() {
        return this.villageChunkZ;
    }

    public int getVillageBlockX() {
        return (this.villageChunkX << 4) + 8;
    }

    public int getVillageBlockZ() {
        return (this.villageChunkZ << 4) + 8;
    }

    public int getOutpostChunkX() {
        return this.outpostChunkX;
    }

    public int getOutpostChunkZ() {
        return this.outpostChunkZ;
    }

    public int getOutpostBlockX() {
        return (this.outpostChunkX << 4) + 8;
    }

    public int getOutpostBlockZ() {
        return (this.outpostChunkZ << 4) + 8;
    }

    public double getDistanceBetweenVillageAndOutpost() {
        int dx = this.getVillageBlockX() - this.getOutpostBlockX();
        int dz = this.getVillageBlockZ() - this.getOutpostBlockZ();
        return Math.hypot(dx, dz);
    }
}

