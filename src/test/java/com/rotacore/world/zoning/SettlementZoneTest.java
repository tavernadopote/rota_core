package com.rotacore.world.zoning;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SettlementZoneTest {

    @Test
    public void testSettlementBoundsDimensionsAndContainment() {
        SettlementBounds bounds = new SettlementBounds(100, 60, 200, 150, 80, 260);

        assertEquals(51, bounds.width());
        assertEquals(21, bounds.height());
        assertEquals(61, bounds.length());
        assertEquals(60, bounds.span());
        assertEquals(125, bounds.centerX());
        assertEquals(70, bounds.centerY());
        assertEquals(230, bounds.centerZ());

        assertTrue(bounds.contains2D(120, 220));
        assertFalse(bounds.contains2D(90, 220));
        assertFalse(bounds.contains2D(120, 300));

        assertTrue(bounds.contains3D(125, 70, 230));
        assertFalse(bounds.contains3D(125, 85, 230));

        SettlementBounds expanded = bounds.expanded(10);
        assertEquals(90, expanded.minX());
        assertEquals(160, expanded.maxX());
        assertEquals(190, expanded.minZ());
        assertEquals(270, expanded.maxZ());
    }

    @Test
    public void testSettlementZoneComputationFromPieces() {
        SettlementBounds structural = new SettlementBounds(0, 50, 0, 300, 100, 300);

        List<SettlementBounds> pieces = new ArrayList<>();
        // 3 casas na vila
        pieces.add(new SettlementBounds(100, 64, 100, 115, 75, 115));
        pieces.add(new SettlementBounds(130, 64, 105, 145, 76, 120));
        pieces.add(new SettlementBounds(110, 64, 140, 125, 74, 155));

        List<BlockPos> endpoints = List.of(
                new BlockPos(95, 64, 110),
                new BlockPos(148, 64, 115)
        );

        SettlementZone zone = SettlementZone.computeFromPieces(structural, pieces, endpoints, 8);

        assertNotNull(zone);
        assertEquals(structural.minX(), zone.getStructuralBounds().minX());
        assertEquals(structural.maxX(), zone.getStructuralBounds().maxX());

        // Urban core deve ser o envelope exato das casas: [100..145, 100..155]
        SettlementBounds urbanCore = zone.getUrbanCoreBounds();
        assertEquals(100, urbanCore.minX());
        assertEquals(145, urbanCore.maxX());
        assertEquals(100, urbanCore.minZ());
        assertEquals(155, urbanCore.maxZ());

        // Approach bounds deve ter a margem de 8 blocos: [92..153, 92..163]
        SettlementBounds approach = zone.getApproachPerimeterBounds();
        assertEquals(92, approach.minX());
        assertEquals(153, approach.maxX());
        assertEquals(92, approach.minZ());
        assertEquals(163, approach.maxZ());

        // Teste de contenção
        assertTrue(zone.isInsideUrbanCore(110, 110));
        assertFalse(zone.isInsideUrbanCore(95, 95));
        assertTrue(zone.isInsideApproachPerimeter(95, 95));
        assertFalse(zone.isInsideApproachPerimeter(80, 80));
        assertTrue(zone.isInsideStructural(80, 80));

        // Teste de proximidade de peças
        assertTrue(zone.isNearAnyPiece(98, 102, 5));
        assertFalse(zone.isNearAnyPiece(50, 50, 5));

        // Teste de saídas viárias
        assertEquals(2, zone.getStreetEndpoints().size());
    }

    @Test
    public void testApproachPerimeterProjection() {
        SettlementBounds structural = new SettlementBounds(0, 60, 0, 200, 90, 200);
        List<SettlementBounds> pieces = List.of(
                new SettlementBounds(80, 64, 80, 120, 75, 120)
        );
        SettlementZone zone = SettlementZone.computeFromPieces(structural, pieces, List.of(), 10);

        BlockPos center = new BlockPos(100, 64, 100);
        BlockPos eastTarget = new BlockPos(500, 64, 100);

        BlockPos perimeterPt = zone.getApproachPerimeterPoint(center, eastTarget);
        assertNotNull(perimeterPt);

        // O approach perimeter max X é 120 + 10 = 130
        assertEquals(130, perimeterPt.getX());
        assertEquals(100, perimeterPt.getZ());
    }

    @Test
    public void testSerializationRoundTrip() {
        SettlementBounds structural = new SettlementBounds(10, 50, 10, 100, 80, 100);
        SettlementBounds urbanCore = new SettlementBounds(20, 60, 20, 80, 75, 80);
        SettlementBounds approach = urbanCore.expanded(8);
        List<SettlementBounds> pieces = List.of(new SettlementBounds(25, 60, 25, 35, 70, 35));
        List<BlockPos> endpoints = List.of(new BlockPos(18, 64, 30));

        SettlementZone original = new SettlementZone(structural, urbanCore, approach, pieces, endpoints);
        CompoundTag tag = original.save();
        assertNotNull(tag);

        SettlementZone loaded = SettlementZone.load(tag);
        assertNotNull(loaded);
        assertEquals(original.getStructuralBounds().minX(), loaded.getStructuralBounds().minX());
        assertEquals(original.getUrbanCoreBounds().maxX(), loaded.getUrbanCoreBounds().maxX());
        assertEquals(original.getApproachPerimeterBounds().minZ(), loaded.getApproachPerimeterBounds().minZ());
        assertEquals(1, loaded.getPieces().size());
        assertEquals(1, loaded.getStreetEndpoints().size());
        assertEquals(18, loaded.getStreetEndpoints().get(0).getX());
    }
}
