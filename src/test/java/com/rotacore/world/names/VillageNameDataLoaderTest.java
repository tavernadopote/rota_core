package com.rotacore.world.names;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VillageNameDataLoaderTest {

    private VillageNameDataLoader loader;

    @BeforeEach
    public void setup() {
        loader = VillageNameDataLoader.getInstance();
        loader.registerDefaultPacks();
    }

    @Test
    public void testDefaultPacksRegistration() {
        VillageNamePack plains = loader.getPack("plains");
        assertNotNull(plains);
        assertEquals("plains", plains.getCulture());
        assertFalse(plains.getPrefixes().isEmpty());
        assertFalse(plains.getSuffixes().isEmpty());
        assertFalse(plains.getUniqueNames().isEmpty());

        VillageNamePack desert = loader.getPack("desert");
        assertNotNull(desert);
        assertEquals("desert", desert.getCulture());

        VillageNamePack savanna = loader.getPack("savanna");
        assertNotNull(savanna);
        assertEquals("savanna", savanna.getCulture());

        VillageNamePack taiga = loader.getPack("taiga");
        assertNotNull(taiga);
        assertEquals("taiga", taiga.getCulture());

        VillageNamePack snowy = loader.getPack("snowy");
        assertNotNull(snowy);
        assertEquals("snowy", snowy.getCulture());
    }

    @Test
    public void testUnknownCultureFallback() {
        VillageNamePack fallback = loader.getPack("unknown_culture_xyz");
        assertNotNull(fallback);
        assertEquals("plains", fallback.getCulture());
    }

    @Test
    public void testLoadFromJsonAndSpecificNames() {
        String json = """
        {
            "culture": "mountain_dwarf",
            "replace": false,
            "prefixes": ["Karak", "Khazad"],
            "suffixes": ["-dum", "-bar"],
            "specific_names": {
                "500,600": "Karak-Vlag"
            },
            "translations": {
                "en_us": {
                    "Karak-Vlag": "Iron Peak"
                },
                "pt_br": {
                    "Karak-Vlag": "Pico de Ferro"
                }
            }
        }
        """;

        loader.loadFromJson("mountain_dwarf", json);

        VillageNamePack dwarfPack = loader.getPack("mountain_dwarf");
        assertNotNull(dwarfPack);
        assertEquals("mountain_dwarf", dwarfPack.getCulture());
        assertTrue(dwarfPack.getPrefixes().contains("Karak"));

        String specificPt = loader.getSpecificName(500, 600, "pt_br");
        assertEquals("Pico de Ferro", specificPt);

        String specificEn = loader.getSpecificName(500, 600, "en_us");
        assertEquals("Iron Peak", specificEn);

        assertNull(loader.getSpecificName(999, 999, "pt_br"));
    }

    @Test
    public void testTranslateName() {
        String translatedEn = loader.translateName("Alvorada", "plains", "en_us");
        assertEquals("Daybreak", translatedEn);

        String translatedPt = loader.translateName("Alvorada", "plains", "pt_br");
        assertEquals("Alvorada", translatedPt);
    }
}
