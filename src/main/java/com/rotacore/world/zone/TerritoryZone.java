/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 */
package com.rotacore.world.zone;

import com.rotacore.world.zone.ZoneType;
import java.util.Objects;
import net.minecraft.core.BlockPos;

public class TerritoryZone {
    private final String id;
    private final String name;
    private final ZoneType zoneType;
    private final BlockPos viableCenter;
    private final int cellGridX;
    private final int cellGridZ;
    private final int seedX;
    private final int seedZ;
    private final String biomeCategory;
    private final int minBlockX;
    private final int minBlockZ;
    private final int maxBlockX;
    private final int maxBlockZ;

    public TerritoryZone(String id, String name, ZoneType zoneType, BlockPos viableCenter, int cellGridX, int cellGridZ, int seedX, int seedZ, String biomeCategory, int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        this.id = Objects.requireNonNull(id, "ID da zona nao pode ser nulo");
        this.name = name != null ? name : id;
        this.zoneType = zoneType != null ? zoneType : ZoneType.CIVIL_KINGDOM;
        this.viableCenter = viableCenter != null ? viableCenter : new BlockPos(seedX, 64, seedZ);
        this.cellGridX = cellGridX;
        this.cellGridZ = cellGridZ;
        this.seedX = seedX;
        this.seedZ = seedZ;
        this.biomeCategory = biomeCategory != null ? biomeCategory : "plains";
        this.minBlockX = minBlockX;
        this.minBlockZ = minBlockZ;
        this.maxBlockX = maxBlockX;
        this.maxBlockZ = maxBlockZ;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ZoneType getZoneType() {
        return this.zoneType;
    }

    public BlockPos getViableCenter() {
        return this.viableCenter;
    }

    public int getCellGridX() {
        return this.cellGridX;
    }

    public int getCellGridZ() {
        return this.cellGridZ;
    }

    public int getSeedX() {
        return this.seedX;
    }

    public int getSeedZ() {
        return this.seedZ;
    }

    public String getBiomeCategory() {
        return this.biomeCategory;
    }

    public int getMinBlockX() {
        return this.minBlockX;
    }

    public int getMinBlockZ() {
        return this.minBlockZ;
    }

    public int getMaxBlockX() {
        return this.maxBlockX;
    }

    public int getMaxBlockZ() {
        return this.maxBlockZ;
    }

    public double getDistanceToViableCenter(int blockX, int blockZ) {
        double dx = this.viableCenter.getX() - blockX;
        double dz = this.viableCenter.getZ() - blockZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TerritoryZone that = (TerritoryZone)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "TerritoryZone{id='" + this.id + "', name='" + this.name + "', zoneType=" + String.valueOf((Object)this.zoneType) + ", center=" + this.viableCenter.getX() + "," + this.viableCenter.getZ() + ", cell=(" + this.cellGridX + "," + this.cellGridZ + ")}";
    }
}

