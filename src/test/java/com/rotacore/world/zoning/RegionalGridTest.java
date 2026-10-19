package com.rotacore.world.zoning;

import com.rotacore.config.RotaCoreConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RegionalGridTest {

    @Test
    public void testRegionalGridPartitioning() {
        long seed = 123456789L;
        int cellSize = 48;

        RegionalCell cell = RegionalGrid.getCell(seed, 0, 0, cellSize);
        assertNotNull(cell);
        assertEquals(0, cell.getCellX());
        assertEquals(0, cell.getCellZ());
        assertEquals(0, cell.getMinChunkX());
        assertEquals(47, cell.getMaxChunkX());
        assertEquals(0, cell.getMinChunkZ());
        assertEquals(47, cell.getMaxChunkZ());

        // Any chunk inside [0..47, 0..47] should resolve to the exact same cell
        for (int cx = 0; cx < 48; cx += 8) {
            for (int cz = 0; cz < 48; cz += 8) {
                RegionalCell resolved = RegionalGrid.getCellForChunk(seed, cx, cz, cellSize);
                assertEquals(cell.getCellX(), resolved.getCellX());
                assertEquals(cell.getCellZ(), resolved.getCellZ());
                assertEquals(cell.getVillageChunkX(), resolved.getVillageChunkX());
                assertEquals(cell.getVillageChunkZ(), resolved.getVillageChunkZ());
            }
        }

        System.out.println("  ✓ Teste Particionamento de Célula Regional: OK");
    }

    @Test
    public void testCandidateUniqueness() {
        long seed = 987654321L;
        RotaCoreConfig config = new RotaCoreConfig();
        config.enableControlledWorldgen = true;
        config.regionalCellSizeChunks = 48;

        int villageCandidateCount = 0;
        int outpostCandidateCount = 0;

        // In a 48x48 chunk area (2304 chunks), there must be EXACTLY 1 village candidate and 1 outpost candidate
        for (int cx = 0; cx < 48; cx++) {
            for (int cz = 0; cz < 48; cz++) {
                if (RegionalGrid.isCandidateVillageChunk(seed, cx, cz, config)) {
                    villageCandidateCount++;
                }
                if (RegionalGrid.isCandidateOutpostChunk(seed, cx, cz, config)) {
                    outpostCandidateCount++;
                }
            }
        }

        assertEquals(1, villageCandidateCount, "Deve haver exatamente 1 chunk de vila por célula regional");
        assertEquals(1, outpostCandidateCount, "Deve haver exatamente 1 chunk de posto pillager por célula regional");

        System.out.println("  ✓ Teste Unicidade de Assentamentos por Célula: OK");
    }

    @Test
    public void testSettlementDistances() {
        long seed = 42L;
        int cellSize = 48;

        RegionalCell cell = RegionalGrid.getCell(seed, 0, 0, cellSize);
        double distBetweenVillageAndOutpost = cell.getDistanceBetweenVillageAndOutpost();

        // Distance between village and outpost in the same cell must be at least 350 blocks
        assertTrue(distBetweenVillageAndOutpost >= 350.0,
                "Distância entre vila e posto militar deve ser >= 350 blocos, foi: " + distBetweenVillageAndOutpost);

        // Distance between villages of adjacent cells (cell 0,0 and cell 1,0)
        RegionalCell cellEast = RegionalGrid.getCell(seed, 1, 0, cellSize);
        double distBetweenVillages = Math.hypot(
                cell.getVillageBlockX() - cellEast.getVillageBlockX(),
                cell.getVillageBlockZ() - cellEast.getVillageBlockZ()
        );

        assertTrue(distBetweenVillages >= 500.0,
                "Distância entre vilas vizinhas deve ser >= 500 blocos, foi: " + distBetweenVillages);

        System.out.println("  ✓ Teste Distâncias de Segurança Territorial: OK (Dist Outpost: " +
                String.format("%.1f", distBetweenVillageAndOutpost) + "m, Dist Vila Vizinha: " +
                String.format("%.1f", distBetweenVillages) + "m)");
    }

    @Test
    public void testRadiusSampling() {
        long seed = 12345L;
        int radius = 1500;
        int cellSize = 48; // 768 blocks

        List<RegionalCell> cells = RegionalGrid.getCellsInRadius(seed, 0, 0, radius, cellSize);
        assertFalse(cells.isEmpty());
        // For a 3000x3000m box with 768m cells, around 4x4 or 5x5 cells (16 to 25 cells)
        assertTrue(cells.size() >= 16 && cells.size() <= 36, "Quantidade esperada de células na vizinhança");

        System.out.println("  ✓ Teste Varredura de Células no Raio do Spawn: OK (Células no raio: " + cells.size() + ")");
    }
}
