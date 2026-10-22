package com.rotacore.world.zoning;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Modelo de Zoneamento e Cadastro Geográfico de Assentamentos (Vilas).
 * Modela os 3 anéis/perímetros perfeitamente definidos:
 * 1. structuralBounds (Roxa): Envelope de reserva cadastral total da estrutura no nível macro (minX, maxX, minZ, maxZ).
 * 2. urbanCoreBounds (Verde): Envelope estrito das peças, casas e edificações reais.
 * 3. approachPerimeterBounds (Amarela): Margem justa de aproximação segura para estradas (ex: 8 a 10 blocos ao redor das casas).
 * Também armazena e expõe as peças individuais e a lista de saídas viárias (streetEndpoints).
 */
public class SettlementZone {

    public static final int DEFAULT_APPROACH_MARGIN = 8;

    private final SettlementBounds structuralBounds;
    private final SettlementBounds urbanCoreBounds;
    private final SettlementBounds approachPerimeterBounds;
    private final List<SettlementBounds> pieces;
    private final List<BlockPos> streetEndpoints;

    public SettlementZone(
            SettlementBounds structuralBounds,
            SettlementBounds urbanCoreBounds,
            SettlementBounds approachPerimeterBounds,
            Collection<SettlementBounds> pieces,
            Collection<BlockPos> streetEndpoints
    ) {
        this.structuralBounds = structuralBounds != null ? structuralBounds : new SettlementBounds(0, 0, 0, 0);
        this.urbanCoreBounds = urbanCoreBounds != null ? urbanCoreBounds : this.structuralBounds;
        this.approachPerimeterBounds = approachPerimeterBounds != null ? approachPerimeterBounds : this.urbanCoreBounds.expanded(DEFAULT_APPROACH_MARGIN);
        this.pieces = (pieces != null) ? Collections.unmodifiableList(new ArrayList<>(pieces)) : Collections.emptyList();
        this.streetEndpoints = (streetEndpoints != null) ? Collections.unmodifiableList(new ArrayList<>(streetEndpoints)) : Collections.emptyList();
    }

    public SettlementBounds getStructuralBounds() {
        return structuralBounds;
    }

    public SettlementBounds getUrbanCoreBounds() {
        return urbanCoreBounds;
    }

    public SettlementBounds getApproachPerimeterBounds() {
        return approachPerimeterBounds;
    }

    public List<SettlementBounds> getPieces() {
        return pieces;
    }

    public List<BlockPos> getStreetEndpoints() {
        return streetEndpoints;
    }

    public boolean isInsideStructural(int x, int z) {
        return structuralBounds.contains2D(x, z);
    }

    public boolean isInsideStructural(BlockPos pos) {
        return pos != null && isInsideStructural(pos.getX(), pos.getZ());
    }

    public boolean isInsideUrbanCore(int x, int z) {
        return urbanCoreBounds.contains2D(x, z);
    }

    public boolean isInsideUrbanCore(BlockPos pos) {
        return pos != null && isInsideUrbanCore(pos.getX(), pos.getZ());
    }

    public boolean isInsideApproachPerimeter(int x, int z) {
        return approachPerimeterBounds.contains2D(x, z);
    }

    public boolean isInsideApproachPerimeter(BlockPos pos) {
        return pos != null && isInsideApproachPerimeter(pos.getX(), pos.getZ());
    }

    public boolean isInsideAnyPiece(int x, int y, int z) {
        for (SettlementBounds piece : pieces) {
            if (piece.contains3D(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInsideAnyPiece(BlockPos pos) {
        return pos != null && isInsideAnyPiece(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean isNearAnyPiece(int x, int z, int margin) {
        for (SettlementBounds piece : pieces) {
            if (x >= piece.minX() - margin && x <= piece.maxX() + margin &&
                z >= piece.minZ() - margin && z <= piece.maxZ() + margin) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula o ponto de ancoragem perimétrica na Borda Amarela (approachPerimeterBounds)
     * na direção de um destino informado, garantindo que as estradas terminem exatamente
     * na margem de aproximação segura das casas e nunca invadam o miolo urbano.
     */
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

        int bMinX = approachPerimeterBounds.minX();
        int bMaxX = approachPerimeterBounds.maxX();
        int bMinZ = approachPerimeterBounds.minZ();
        int bMaxZ = approachPerimeterBounds.maxZ();

        double tX = Double.MAX_VALUE;
        if (dirX > 1e-5) {
            tX = (bMaxX - center.getX()) / dirX;
        } else if (dirX < -1e-5) {
            tX = (bMinX - center.getX()) / dirX;
        }

        double tZ = Double.MAX_VALUE;
        if (dirZ > 1e-5) {
            tZ = (bMaxZ - center.getZ()) / dirZ;
        } else if (dirZ < -1e-5) {
            tZ = (bMinZ - center.getZ()) / dirZ;
        }

        double hitDist = 0.0;
        if (tX > 0 && tZ > 0) {
            hitDist = Math.min(tX, tZ);
        } else if (tX > 0) {
            hitDist = tX;
        } else if (tZ > 0) {
            hitDist = tZ;
        }

        // Não projeta além de 45% da distância total até o alvo
        hitDist = Math.min(hitDist, dist * 0.45);

        int px = (int) Math.round(center.getX() + dirX * hitDist);
        int pz = (int) Math.round(center.getZ() + dirZ * hitDist);

        return new BlockPos(px, center.getY(), pz);
    }

    /**
     * Constrói e recalcula uma SettlementZone a partir de uma coleção de peças reais e pontos viários.
     */
    public static SettlementZone computeFromPieces(
            SettlementBounds structuralBounds,
            Collection<SettlementBounds> pieces,
            Collection<BlockPos> streetEndpoints,
            int approachMargin
    ) {
        int margin = Math.max(4, approachMargin);

        if (pieces == null || pieces.isEmpty()) {
            SettlementBounds struct = (structuralBounds != null)
                    ? structuralBounds
                    : new SettlementBounds(-32, -64, -32, 32, 320, 32);
            SettlementBounds approach = struct.expanded(margin);
            return new SettlementZone(struct, struct, approach, Collections.emptyList(), streetEndpoints);
        }

        int pMinX = Integer.MAX_VALUE, pMaxX = Integer.MIN_VALUE;
        int pMinY = Integer.MAX_VALUE, pMaxY = Integer.MIN_VALUE;
        int pMinZ = Integer.MAX_VALUE, pMaxZ = Integer.MIN_VALUE;

        List<SettlementBounds> piecesList = new ArrayList<>(pieces);
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
        if (struct == null) {
            struct = urbanCore.expanded(16);
        } else {
            // Garante que a reserva estrutural englobe pelo menos o núcleo urbano
            struct = struct.union(urbanCore);
        }

        return new SettlementZone(struct, urbanCore, approach, piecesList, streetEndpoints);
    }

    public static SettlementZone computeFromPieces(
            SettlementBounds structuralBounds,
            Collection<SettlementBounds> pieces,
            Collection<BlockPos> streetEndpoints
    ) {
        return computeFromPieces(structuralBounds, pieces, streetEndpoints, DEFAULT_APPROACH_MARGIN);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("structural_bounds", structuralBounds.save());
        tag.put("urban_core_bounds", urbanCoreBounds.save());
        tag.put("approach_bounds", approachPerimeterBounds.save());

        ListTag piecesList = new ListTag();
        for (SettlementBounds piece : pieces) {
            piecesList.add(piece.save());
        }
        tag.put("pieces", piecesList);

        ListTag endpointsList = new ListTag();
        for (BlockPos pos : streetEndpoints) {
            CompoundTag pt = new CompoundTag();
            pt.putInt("x", pos.getX());
            pt.putInt("y", pos.getY());
            pt.putInt("z", pos.getZ());
            endpointsList.add(pt);
        }
        tag.put("street_endpoints", endpointsList);

        return tag;
    }

    public static SettlementZone load(CompoundTag tag) {
        if (tag == null) {
            return new SettlementZone(null, null, null, null, null);
        }

        SettlementBounds structural = tag.contains("structural_bounds")
                ? SettlementBounds.load(tag.getCompound("structural_bounds"))
                : null;
        SettlementBounds urbanCore = tag.contains("urban_core_bounds")
                ? SettlementBounds.load(tag.getCompound("urban_core_bounds"))
                : null;
        SettlementBounds approach = tag.contains("approach_bounds")
                ? SettlementBounds.load(tag.getCompound("approach_bounds"))
                : null;

        List<SettlementBounds> pieces = new ArrayList<>();
        if (tag.contains("pieces", Tag.TAG_LIST)) {
            ListTag list = tag.getList("pieces", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                pieces.add(SettlementBounds.load(list.getCompound(i)));
            }
        }

        List<BlockPos> endpoints = new ArrayList<>();
        if (tag.contains("street_endpoints", Tag.TAG_LIST)) {
            ListTag list = tag.getList("street_endpoints", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pt = list.getCompound(i);
                endpoints.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
            }
        }

        return new SettlementZone(structural, urbanCore, approach, pieces, endpoints);
    }
}
