package com.rotacore.world.names;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SettlementCommercialDataTest {

    @Test
    public void testWorkstationMappingAndEstimation() {
        Map<String, Integer> workstations = Map.of(
                "minecraft:composter", 5,
                "minecraft:smoker", 2,
                "minecraft:blast_furnace", 1
        );

        SettlementCommercialData dataPt = SettlementCommercialData.estimateFromWorkstations(workstations, "pt_br");
        assertNotNull(dataPt);
        assertEquals("Agropecuária", dataPt.getPrimaryVocation());
        assertEquals("Metalurgia e Armaria", dataPt.getSecondaryVocation());
        assertTrue(dataPt.getHighlightedTradeItems().contains("Pão") || dataPt.getHighlightedTradeItems().contains("Trigo"));

        SettlementCommercialData dataEn = SettlementCommercialData.estimateFromWorkstations(workstations, "en_us");
        assertEquals("Agriculture", dataEn.getPrimaryVocation());
        assertEquals("Metallurgy & Smithing", dataEn.getSecondaryVocation());
        assertTrue(dataEn.getHighlightedTradeItems().contains("Bread") || dataEn.getHighlightedTradeItems().contains("Wheat"));
    }

    @Test
    public void testEmptyWorkstationsFallback() {
        SettlementCommercialData fallback = SettlementCommercialData.estimateFromWorkstations(Map.of(), "pt_br");
        assertEquals("Comércio Geral", fallback.getPrimaryVocation());
        assertEquals("", fallback.getSecondaryVocation());
    }

    @Test
    public void testDeterministicCreation() {
        long seed = 55555L;
        int x = 200;
        int z = 300;

        SettlementCommercialData c1 = SettlementCommercialData.createDeterministic(seed, x, z, "pt_br");
        SettlementCommercialData c2 = SettlementCommercialData.createDeterministic(seed, x, z, "pt_br");

        assertEquals(c1.getPrimaryVocation(), c2.getPrimaryVocation());
        assertEquals(c1.getSecondaryVocation(), c2.getSecondaryVocation());
        assertEquals(c1.getHighlightedTradeItems(), c2.getHighlightedTradeItems());
    }

    @Test
    public void testNbtSerializationRoundTrip() {
        SettlementCommercialData original = new SettlementCommercialData(
                "Metalurgia e Armaria",
                "Manufatura e Alvenaria",
                List.of("Armaduras de Ferro", "Espadas", "Pedras Talhadas"),
                Map.of("minecraft:blast_furnace", 3, "minecraft:stonecutter", 2)
        );

        CompoundTag tag = original.save();
        assertNotNull(tag);

        SettlementCommercialData loaded = SettlementCommercialData.load(tag);
        assertNotNull(loaded);
        assertEquals(original.getPrimaryVocation(), loaded.getPrimaryVocation());
        assertEquals(original.getSecondaryVocation(), loaded.getSecondaryVocation());
        assertEquals(original.getHighlightedTradeItems(), loaded.getHighlightedTradeItems());
        assertEquals(3, loaded.getWorkstationCounts().get("minecraft:blast_furnace"));
        assertEquals(2, loaded.getWorkstationCounts().get("minecraft:stonecutter"));
        assertEquals("Metalurgia e Armaria / Manufatura e Alvenaria", loaded.getFormattedDescription());
    }
}
