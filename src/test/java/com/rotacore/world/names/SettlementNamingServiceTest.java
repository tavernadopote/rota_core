package com.rotacore.world.names;

import com.rotacore.world.zoning.SettlementBounds;
import com.rotacore.world.zoning.SettlementZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SettlementNamingServiceTest {

    @BeforeEach
    public void setup() {
        VillageNameDataLoader.getInstance().registerDefaultPacks();
        SettlementNamingService.setCustomWaystoneResolver(null);
    }

    @AfterEach
    public void cleanup() {
        SettlementNamingService.setCustomWaystoneResolver(null);
    }

    @Test
    public void testProceduralDeterminism() {
        long seed = 123456789L;
        int x = 250;
        int z = -450;
        String culture = "plains";

        String name1 = SettlementNamingService.generateProceduralName(seed, x, z, culture, "pt_br");
        String name2 = SettlementNamingService.generateProceduralName(seed, x, z, culture, "pt_br");

        assertNotNull(name1);
        assertFalse(name1.isEmpty());
        assertEquals(name1, name2, "A geração procedural com mesma semente e coordenadas deve ser 100% determinística");
    }

    @Test
    public void testCultureNormalization() {
        assertEquals("desert", SettlementNamingService.normalizeCulture("minecraft:desert"));
        assertEquals("desert", SettlementNamingService.normalizeCulture("badlands_plateau"));
        assertEquals("savanna", SettlementNamingService.normalizeCulture("savanna_plateau"));
        assertEquals("taiga", SettlementNamingService.normalizeCulture("snowy_taiga"));
        assertEquals("snowy", SettlementNamingService.normalizeCulture("snowy_plains"));
        assertEquals("plains", SettlementNamingService.normalizeCulture("plains"));
        assertEquals("plains", SettlementNamingService.normalizeCulture(null));
    }

    @Test
    public void testFormatCompoundName() {
        assertEquals("Belo Monte", SettlementNamingService.formatCompoundName("Belo", "Monte"));
        assertEquals("Beloval", SettlementNamingService.formatCompoundName("Belo", "val"));
        assertEquals("Al-Kharid", SettlementNamingService.formatCompoundName("Al-", "Kharid"));
        assertEquals("Porto-sarai", SettlementNamingService.formatCompoundName("Porto", "-sarai"));
    }

    @Test
    public void testPriority1Waystones() {
        SettlementNamingService.setCustomWaystoneResolver((level, bounds) -> "Waystone Capital");

        SettlementBounds bounds = new SettlementBounds(0, 60, 0, 100, 70, 100);
        SettlementZone zone = new SettlementZone(bounds, bounds, bounds.expanded(8), List.of(), List.of());

        String resolved = SettlementNamingService.resolveSettlementName(null, zone, 9999L, 50, 50, "plains", "pt_br");

        assertEquals("Waystone Capital", resolved);
        assertEquals("Waystone Capital", zone.getSettlementName());
    }

    @Test
    public void testPriority2DataPackSpecificCoordinates() {
        String json = """
        {
            "culture": "desert",
            "replace": false,
            "specific_names": {
                "300,400": "Oásis Secreto"
            },
            "translations": {
                "pt_br": { "Oásis Secreto": "Oásis Secreto" },
                "en_us": { "Oásis Secreto": "Secret Oasis" }
            }
        }
        """;
        VillageNameDataLoader.getInstance().loadFromJson("desert", json);

        SettlementBounds bounds = new SettlementBounds(250, 60, 350, 350, 70, 450);
        SettlementZone zone = new SettlementZone(bounds, bounds, bounds.expanded(8), List.of(), List.of());

        String resolvedPt = SettlementNamingService.resolveSettlementName(null, zone, 9999L, 300, 400, "desert", "pt_br");
        assertEquals("Oásis Secreto", resolvedPt);
        assertEquals("Oásis Secreto", zone.getSettlementName());
    }

    @Test
    public void testPriority3ProceduralFallback() {
        SettlementBounds bounds = new SettlementBounds(1000, 60, 1000, 1100, 70, 1100);
        SettlementZone zone = new SettlementZone(bounds, bounds, bounds.expanded(8), List.of(), List.of());

        String resolved = SettlementNamingService.resolveSettlementName(null, zone, 42L, 1050, 1050, "taiga", "pt_br");

        assertNotNull(resolved);
        assertFalse(resolved.isEmpty());
        assertTrue(zone.hasSettlementName());
        assertEquals(resolved, zone.getSettlementName());
    }

    @Test
    public void testZonePreservesPreExistingName() {
        SettlementBounds bounds = new SettlementBounds(0, 60, 0, 100, 70, 100);
        SettlementZone zone = new SettlementZone(bounds, bounds, bounds.expanded(8), List.of(), List.of());
        zone.setSettlementName("Alvorada");

        String resolvedEn = SettlementNamingService.resolveSettlementName(null, zone, 12345L, 50, 50, "plains", "en_us");
        assertEquals("Daybreak", resolvedEn);
    }
}
