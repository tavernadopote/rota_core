package com.rotacore.world.zoning;

import com.rotacore.world.names.SettlementCommercialData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class SettlementZone {
    public static final int DEFAULT_APPROACH_MARGIN = 8;

    public enum SettlementPortalType {
        TERRESTRIAL,
        MARITIME
    }

    public record SettlementPortal(BlockPos pos, SettlementPortalType type) {
    }

    private final SettlementBounds structuralBounds;
    private final SettlementBounds urbanCoreBounds;
    private final SettlementBounds approachPerimeterBounds;
    private final List<SettlementBounds> pieces;
    private final List<BlockPos> streetEndpoints;
    private final List<SettlementPortal> portals;
    private String settlementName;
    private SettlementCommercialData commercialData;

    public SettlementZone(SettlementBounds structuralBounds, SettlementBounds urbanCoreBounds, SettlementBounds approachPerimeterBounds,
                          Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints,
                          String settlementName, SettlementCommercialData commercialData,
                          Collection<SettlementPortal> portals) {
        this.structuralBounds = structuralBounds != null ? structuralBounds : new SettlementBounds(0, 0, 0, 0);
        this.urbanCoreBounds = urbanCoreBounds != null ? urbanCoreBounds : this.structuralBounds;
        this.approachPerimeterBounds = approachPerimeterBounds != null ? approachPerimeterBounds : this.urbanCoreBounds.expanded(DEFAULT_APPROACH_MARGIN);
        this.pieces = pieces != null ? Collections.unmodifiableList(new ArrayList<>(pieces)) : Collections.emptyList();
        this.streetEndpoints = streetEndpoints != null ? Collections.unmodifiableList(new ArrayList<>(streetEndpoints)) : Collections.emptyList();
        this.settlementName = settlementName != null ? settlementName : "";
        this.commercialData = commercialData;
        this.portals = portals != null ? Collections.unmodifiableList(new ArrayList<>(portals)) : Collections.emptyList();
    }

    public SettlementZone(SettlementBounds structuralBounds, SettlementBounds urbanCoreBounds, SettlementBounds approachPerimeterBounds,
                          Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints,
                          String settlementName, SettlementCommercialData commercialData) {
        this(structuralBounds, urbanCoreBounds, approachPerimeterBounds, pieces, streetEndpoints, settlementName, commercialData, Collections.emptyList());
    }

    public SettlementZone(SettlementBounds structuralBounds, SettlementBounds urbanCoreBounds, SettlementBounds approachPerimeterBounds,
                          Collection<SettlementBounds> pieces, Collection<BlockPos> streetEndpoints) {
        this(structuralBounds, urbanCoreBounds, approachPerimeterBounds, pieces, streetEndpoints, "", null, Collections.emptyList());
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

    public List<SettlementPortal> getPortals() {
        return this.portals;
    }

    public List<SettlementPortal> getTerrestrialPortals() {
        if (this.portals == null || this.portals.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<SettlementPortal> terrestrial = new ArrayList<>();
        for (SettlementPortal portal : this.portals) {
            if (portal.type() == SettlementPortalType.TERRESTRIAL) {
                terrestrial.add(portal);
            }
        }
        return Collections.unmodifiableList(terrestrial);
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
            if (piece.contains3D(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsideAnyPiece(BlockPos pos) {
        return pos != null && this.isInsideAnyPiece(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isNearAnyPiece(int x, int z, int margin) {
        for (SettlementBounds piece : this.pieces) {
            if (x >= piece.minX() - margin && x <= piece.maxX() + margin && z >= piece.minZ() - margin && z <= piece.maxZ() + margin) {
                return true;
            }
        }
        return false;
    }

    public BlockPos getApproachPerimeterPoint(BlockPos center, BlockPos target) {
        if (target == null || center == null) {
            return center != null ? center : BlockPos.ZERO;
        }
        double dx = target.getX() - center.getX();
        double dz = target.getZ() - center.getZ();
        double dist = Math.hypot(dx, dz);
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
        ArrayList<SettlementBounds> piecesList = new ArrayList<>(pieces);
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
        return SettlementZone.computeFromPieces(structuralBounds, pieces, streetEndpoints, DEFAULT_APPROACH_MARGIN);
    }

    public static SettlementZone computeFromPiecesWithPortals(
            SettlementBounds structuralBounds,
            Collection<SettlementBounds> buildingBounds,
            Collection<SettlementBounds> streetBounds,
            Collection<BlockPos> streetEndpoints,
            int approachMargin,
            BiPredicate<Integer, Integer> isWater
    ) {
        int margin = Math.max(4, approachMargin);
        Collection<SettlementBounds> primaryPieces = (buildingBounds != null && !buildingBounds.isEmpty()) ? buildingBounds : streetBounds;
        if (primaryPieces == null || primaryPieces.isEmpty()) {
            SettlementBounds struct = structuralBounds != null ? structuralBounds : new SettlementBounds(-32, -64, -32, 32, 320, 32);
            SettlementBounds approach = struct.expanded(margin);
            return new SettlementZone(struct, struct, approach, Collections.emptyList(), streetEndpoints, "", null, Collections.emptyList());
        }

        int pMinX = Integer.MAX_VALUE;
        int pMaxX = Integer.MIN_VALUE;
        int pMinY = Integer.MAX_VALUE;
        int pMaxY = Integer.MIN_VALUE;
        int pMinZ = Integer.MAX_VALUE;
        int pMaxZ = Integer.MIN_VALUE;
        ArrayList<SettlementBounds> piecesList = new ArrayList<>(primaryPieces);
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

        Map<Long, SettlementPortal> portalMap = new LinkedHashMap<>();

        if (streetEndpoints != null && !streetEndpoints.isEmpty()) {
            for (BlockPos ep : streetEndpoints) {
                if (ep == null) continue;
                BlockPos snapped = snapToPerimeter(ep, approach);
                SettlementPortalType type = (isWater != null && isWater.test(snapped.getX(), snapped.getZ()))
                        ? SettlementPortalType.MARITIME
                        : SettlementPortalType.TERRESTRIAL;
                long key = BlockPos.asLong(snapped.getX(), 0, snapped.getZ());
                portalMap.putIfAbsent(key, new SettlementPortal(snapped, type));
            }
        }

        if (streetBounds != null && !streetBounds.isEmpty()) {
            for (SettlementBounds sb : streetBounds) {
                int cx = (sb.minX() + sb.maxX()) / 2;
                int cz = (sb.minZ() + sb.maxZ()) / 2;
                int cy = sb.minY();
                if (approach.expanded(2).contains2D(cx, cz)) {
                    BlockPos candidate = snapToPerimeter(new BlockPos(cx, cy, cz), approach);
                    SettlementPortalType type = (isWater != null && isWater.test(candidate.getX(), candidate.getZ()))
                            ? SettlementPortalType.MARITIME
                            : SettlementPortalType.TERRESTRIAL;
                    long key = BlockPos.asLong(candidate.getX(), 0, candidate.getZ());
                    portalMap.putIfAbsent(key, new SettlementPortal(candidate, type));
                }
            }
        }

        boolean hasTerrestrial = false;
        for (SettlementPortal portal : portalMap.values()) {
            if (portal.type() == SettlementPortalType.TERRESTRIAL) {
                hasTerrestrial = true;
                break;
            }
        }

        if (!hasTerrestrial) {
            int midX = (approach.minX() + approach.maxX()) / 2;
            int midZ = (approach.minZ() + approach.maxZ()) / 2;
            int y = urbanCore.minY();
            BlockPos[] cardinals = new BlockPos[]{
                    new BlockPos(midX, y, approach.minZ()),
                    new BlockPos(midX, y, approach.maxZ()),
                    new BlockPos(approach.minX(), y, midZ),
                    new BlockPos(approach.maxX(), y, midZ)
            };
            for (BlockPos cp : cardinals) {
                SettlementPortalType type = (isWater != null && isWater.test(cp.getX(), cp.getZ()))
                        ? SettlementPortalType.MARITIME
                        : SettlementPortalType.TERRESTRIAL;
                long key = BlockPos.asLong(cp.getX(), 0, cp.getZ());
                portalMap.putIfAbsent(key, new SettlementPortal(cp, type));
            }
        }

        return new SettlementZone(struct, urbanCore, approach, piecesList, streetEndpoints, "", null, portalMap.values());
    }

    private static BlockPos snapToPerimeter(BlockPos pos, SettlementBounds perimeter) {
        int x = Math.max(perimeter.minX(), Math.min(perimeter.maxX(), pos.getX()));
        int z = Math.max(perimeter.minZ(), Math.min(perimeter.maxZ(), pos.getZ()));
        int dMinX = Math.abs(x - perimeter.minX());
        int dMaxX = Math.abs(x - perimeter.maxX());
        int dMinZ = Math.abs(z - perimeter.minZ());
        int dMaxZ = Math.abs(z - perimeter.maxZ());
        int minD = Math.min(Math.min(dMinX, dMaxX), Math.min(dMinZ, dMaxZ));
        if (minD == dMinX) {
            x = perimeter.minX();
        } else if (minD == dMaxX) {
            x = perimeter.maxX();
        } else if (minD == dMinZ) {
            z = perimeter.minZ();
        } else {
            z = perimeter.maxZ();
        }
        return new BlockPos(x, pos.getY(), z);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("structural_bounds", this.structuralBounds.save());
        tag.put("urban_core_bounds", this.urbanCoreBounds.save());
        tag.put("approach_bounds", this.approachPerimeterBounds.save());
        ListTag piecesList = new ListTag();
        for (SettlementBounds piece : this.pieces) {
            piecesList.add(piece.save());
        }
        tag.put("pieces", piecesList);
        ListTag endpointsList = new ListTag();
        for (BlockPos pos : this.streetEndpoints) {
            CompoundTag pt = new CompoundTag();
            pt.putInt("x", pos.getX());
            pt.putInt("y", pos.getY());
            pt.putInt("z", pos.getZ());
            endpointsList.add(pt);
        }
        tag.put("street_endpoints", endpointsList);
        if (!this.portals.isEmpty()) {
            ListTag portalsList = new ListTag();
            for (SettlementPortal portal : this.portals) {
                CompoundTag pt = new CompoundTag();
                pt.putInt("x", portal.pos().getX());
                pt.putInt("y", portal.pos().getY());
                pt.putInt("z", portal.pos().getZ());
                pt.putString("type", portal.type().name());
                portalsList.add(pt);
            }
            tag.put("portals", portalsList);
        }
        if (this.settlementName != null && !this.settlementName.trim().isEmpty()) {
            tag.putString("settlement_name", this.settlementName);
        }
        if (this.commercialData != null) {
            tag.put("commercial_data", this.commercialData.save());
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
        ArrayList<SettlementBounds> pieces = new ArrayList<>();
        if (tag.contains("pieces", 9)) {
            ListTag list = tag.getList("pieces", 10);
            for (int i = 0; i < list.size(); ++i) {
                pieces.add(SettlementBounds.load(list.getCompound(i)));
            }
        }
        ArrayList<BlockPos> endpoints = new ArrayList<>();
        if (tag.contains("street_endpoints", 9)) {
            ListTag list = tag.getList("street_endpoints", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag pt = list.getCompound(i);
                endpoints.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
            }
        }
        ArrayList<SettlementPortal> portals = new ArrayList<>();
        if (tag.contains("portals", 9)) {
            ListTag list = tag.getList("portals", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag pt = list.getCompound(i);
                BlockPos pos = new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z"));
                SettlementPortalType type = SettlementPortalType.TERRESTRIAL;
                if (pt.contains("type")) {
                    try {
                        type = SettlementPortalType.valueOf(pt.getString("type"));
                    } catch (Exception ignored) {
                    }
                }
                portals.add(new SettlementPortal(pos, type));
            }
        }
        String settlementName = tag.getString("settlement_name");
        SettlementCommercialData commercialData = tag.contains("commercial_data", 10) ? SettlementCommercialData.load(tag.getCompound("commercial_data")) : null;
        return new SettlementZone(structural, urbanCore, approach, pieces, endpoints, settlementName, commercialData, portals);
    }
}
