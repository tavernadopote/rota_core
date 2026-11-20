/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 */
package com.rotacore.world.zoning;

import com.rotacore.world.names.SettlementCommercialData;
import com.rotacore.world.zoning.SettlementBounds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SettlementZone {
    public static final int DEFAULT_APPROACH_MARGIN = 8;
    private final SettlementBounds structuralBounds;
    private final SettlementBounds urbanCoreBounds;
    private final SettlementBounds approachPerimeterBounds;
    private final List<SettlementBounds> pieces;
    private final List<BlockPos> streetEndpoints;
    private String settlementName;
    private SettlementCommercialData commercialData;

    public SettlementZone(SettlementBounds structuralBounds, SettlementBounds urbanCoreBounds, SettlementBounds approachPerimeterBounds, Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints, String settlementName, SettlementCommercialData commercialData) {
        this.structuralBounds = structuralBounds != null ? structuralBounds : new SettlementBounds(0, 0, 0, 0);
        this.urbanCoreBounds = urbanCoreBounds != null ? urbanCoreBounds : this.structuralBounds;
        this.approachPerimeterBounds = approachPerimeterBounds != null ? approachPerimeterBounds : this.urbanCoreBounds.expanded(8);
        this.pieces = pieces != null ? Collections.unmodifiableList(new ArrayList<SettlementBounds>(pieces)) : Collections.emptyList();
        this.streetEndpoints = streetEndpoints != null ? Collections.unmodifiableList(new ArrayList<BlockPos>(streetEndpoints)) : Collections.emptyList();
        this.settlementName = settlementName != null ? settlementName : "";
        this.commercialData = commercialData;
    }

    public SettlementZone(SettlementBounds structuralBounds, SettlementBounds urbanCoreBounds, SettlementBounds approachPerimeterBounds, Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints) {
        this(structuralBounds, urbanCoreBounds, approachPerimeterBounds, pieces, streetEndpoints, "", null);
    }

    public SettlementBounds getStructuralBounds() {
        return this.structuralBounds;
    }

    public SettlementBounds getUrbanCoreBounds() {
        return this.urbanCoreBounds;
    }

    public SettlementBounds getApproachPerimeterBounds() {
        return this.approachPerimeterBounds;
    }

    public List<SettlementBounds> getPieces() {
        return this.pieces;
    }

    public List<BlockPos> getStreetEndpoints() {
        return this.streetEndpoints;
    }

    public String getSettlementName() {
        return this.settlementName;
    }

    public void setSettlementName(String settlementName) {
        this.settlementName = settlementName != null ? settlementName : "";
    }

    public boolean hasSettlementName() {
        return this.settlementName != null && !this.settlementName.trim().isEmpty();
    }

    public SettlementCommercialData getCommercialData() {
        return this.commercialData;
    }

    public void setCommercialData(SettlementCommercialData commercialData) {
        this.commercialData = commercialData;
    }

    public boolean isInsideStructural(int x, int z) {
        return this.structuralBounds.contains2D(x, z);
    }

    public boolean isInsideStructural(BlockPos pos) {
        return pos != null && this.isInsideStructural(pos.getX(), pos.getZ());
    }

    public boolean isInsideUrbanCore(int x, int z) {
        return this.urbanCoreBounds.contains2D(x, z);
    }

    public boolean isInsideUrbanCore(BlockPos pos) {
        return pos != null && this.isInsideUrbanCore(pos.getX(), pos.getZ());
    }

    public boolean isInsideApproachPerimeter(int x, int z) {
        return this.approachPerimeterBounds.contains2D(x, z);
    }

    public boolean isInsideApproachPerimeter(BlockPos pos) {
        return pos != null && this.isInsideApproachPerimeter(pos.getX(), pos.getZ());
    }

    public boolean isInsideAnyPiece(int x, int y, int z) {
        for (SettlementBounds piece : this.pieces) {
            if (!piece.contains3D(x, y, z)) continue;
            return true;
        }
        return false;
    }

    public boolean isInsideAnyPiece(BlockPos pos) {
        return pos != null && this.isInsideAnyPiece(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isNearAnyPiece(int x, int z, int margin) {
        for (SettlementBounds piece : this.pieces) {
            if (x < piece.minX() - margin || x > piece.maxX() + margin || z < piece.minZ() - margin || z > piece.maxZ() + margin) continue;
            return true;
        }
        return false;
    }

    public BlockPos getApproachPerimeterPoint(BlockPos center, BlockPos target) {
        double dz;
        if (target == null || center == null) {
            return center != null ? center : BlockPos.ZERO;
        }
        double dx = target.getX() - center.getX();
        double dist = Math.hypot(dx, dz = (double)(target.getZ() - center.getZ()));
        if (dist < 1.0) {
            return center;
        }
        double dirX = dx / dist;
        double dirZ = dz / dist;
        int bMinX = this.approachPerimeterBounds.minX();
        int bMaxX = this.approachPerimeterBounds.maxX();
        int bMinZ = this.approachPerimeterBounds.minZ();
        int bMaxZ = this.approachPerimeterBounds.maxZ();
        double tX = Double.MAX_VALUE;
        if (dirX > 1.0E-5) {
            tX = (double)(bMaxX - center.getX()) / dirX;
        } else if (dirX < -1.0E-5) {
            tX = (double)(bMinX - center.getX()) / dirX;
        }
        double tZ = Double.MAX_VALUE;
        if (dirZ > 1.0E-5) {
            tZ = (double)(bMaxZ - center.getZ()) / dirZ;
        } else if (dirZ < -1.0E-5) {
            tZ = (double)(bMinZ - center.getZ()) / dirZ;
        }
        double hitDist = 0.0;
        if (tX > 0.0 && tZ > 0.0) {
            hitDist = Math.min(tX, tZ);
        } else if (tX > 0.0) {
            hitDist = tX;
        } else if (tZ > 0.0) {
            hitDist = tZ;
        }
        hitDist = Math.min(hitDist, dist * 0.45);
        int px = (int)Math.round((double)center.getX() + dirX * hitDist);
        int pz = (int)Math.round((double)center.getZ() + dirZ * hitDist);
        return new BlockPos(px, center.getY(), pz);
    }

    public static SettlementZone computeFromPieces(SettlementBounds structuralBounds, Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints, int approachMargin) {
        int margin = Math.max(4, approachMargin);
        if (pieces == null || pieces.isEmpty()) {
            SettlementBounds struct = structuralBounds != null ? structuralBounds : new SettlementBounds(-32, -64, -32, 32, 320, 32);
            SettlementBounds approach = struct.expanded(margin);
            return new SettlementZone(struct, struct, approach, Collections.emptyList(), streetEndpoints);
        }
        int pMinX = Integer.MAX_VALUE;
        int pMaxX = Integer.MIN_VALUE;
        int pMinY = Integer.MAX_VALUE;
        int pMaxY = Integer.MIN_VALUE;
        int pMinZ = Integer.MAX_VALUE;
        int pMaxZ = Integer.MIN_VALUE;
        ArrayList<SettlementBounds> piecesList = new ArrayList<SettlementBounds>(pieces);
        for (SettlementBounds pb : piecesList) {
            pMinX = Math.min(pMinX, pb.minX());
            pMaxX = Math.max(pMaxX, pb.maxX());
            pMinY = Math.min(pMinY, pb.minY());
            pMaxY = Math.max(pMaxY, pb.maxY());
            pMinZ = Math.min(pMinZ, pb.minZ());
            pMaxZ = Math.max(pMaxZ, pb.maxZ());
        }
        SettlementBounds urbanCore = new SettlementBounds(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ);
        SettlementBounds approach = urbanCore.expanded(margin);
        SettlementBounds struct = structuralBounds;
        struct = struct == null ? urbanCore.expanded(16) : struct.union(urbanCore);
        return new SettlementZone(struct, urbanCore, approach, piecesList, streetEndpoints);
    }

    public static SettlementZone computeFromPieces(SettlementBounds structuralBounds, Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints) {
        return SettlementZone.computeFromPieces(structuralBounds, pieces, streetEndpoints, 8);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("structural_bounds", (Tag)this.structuralBounds.save());
        tag.put("urban_core_bounds", (Tag)this.urbanCoreBounds.save());
        tag.put("approach_bounds", (Tag)this.approachPerimeterBounds.save());
        ListTag piecesList = new ListTag();
        for (SettlementBounds piece : this.pieces) {
            piecesList.add((Object)piece.save());
        }
        tag.put("pieces", (Tag)piecesList);
        ListTag endpointsList = new ListTag();
        for (BlockPos pos : this.streetEndpoints) {
            CompoundTag pt = new CompoundTag();
            pt.putInt("x", pos.getX());
            pt.putInt("y", pos.getY());
            pt.putInt("z", pos.getZ());
            endpointsList.add((Object)pt);
        }
        tag.put("street_endpoints", (Tag)endpointsList);
        if (this.settlementName != null && !this.settlementName.trim().isEmpty()) {
            tag.putString("settlement_name", this.settlementName);
        }
        if (this.commercialData != null) {
            tag.put("commercial_data", (Tag)this.commercialData.save());
        }
        return tag;
    }

    public static SettlementZone load(CompoundTag tag) {
        if (tag == null) {
            return new SettlementZone(null, null, null, null, null);
        }
        SettlementBounds structural = tag.contains("structural_bounds") ? SettlementBounds.load(tag.getCompound("structural_bounds")) : null;
        SettlementBounds urbanCore = tag.contains("urban_core_bounds") ? SettlementBounds.load(tag.getCompound("urban_core_bounds")) : null;
        SettlementBounds approach = tag.contains("approach_bounds") ? SettlementBounds.load(tag.getCompound("approach_bounds")) : null;
        ArrayList<SettlementBounds> pieces = new ArrayList<SettlementBounds>();
        if (tag.contains("pieces", 9)) {
            ListTag list = tag.getList("pieces", 10);
            for (int i = 0; i < list.size(); ++i) {
                pieces.add(SettlementBounds.load(list.getCompound(i)));
            }
        }
        ArrayList<BlockPos> endpoints = new ArrayList<BlockPos>();
        if (tag.contains("street_endpoints", 9)) {
            ListTag list = tag.getList("street_endpoints", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag pt = list.getCompound(i);
                endpoints.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
            }
        }
        String settlementName = tag.getString("settlement_name");
        SettlementCommercialData commercialData = tag.contains("commercial_data", 10) ? SettlementCommercialData.load(tag.getCompound("commercial_data")) : null;
        return new SettlementZone(structural, urbanCore, approach, pieces, endpoints, settlementName, commercialData);
    }
}

