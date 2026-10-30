package com.rotacore.world.names;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VillageNamePackTest {

    @Test
    public void testSerializationRoundTrip() {
        VillageNamePack pack = new VillageNamePack("plains", false);
        pack.addPrefix("Belo");
        pack.addSuffix("monte");
        pack.addUniqueName("Alvorada");
        pack.addSpecificName(100, 200, "Vila Central");
        pack.addTranslation("pt_br", "Belo", "Belo");
        pack.addTranslation("en_us", "Belo", "Fair");

        String json = pack.toJson();
        assertNotNull(json);

        VillageNamePack restored = VillageNamePack.fromJson(json);
        assertNotNull(restored);
        assertEquals("plains", restored.getCulture());
        assertFalse(restored.isReplace());
        assertTrue(restored.getPrefixes().contains("Belo"));
        assertTrue(restored.getSuffixes().contains("monte"));
        assertTrue(restored.getUniqueNames().contains("Alvorada"));
        assertEquals("Vila Central", restored.getSpecificNames().get("100,200"));
        assertEquals("Fair", restored.translate("Belo", "en_us"));
        assertEquals("Belo", restored.translate("Belo", "pt_br"));
    }

    @Test
    public void testTranslationFallbacks() {
        VillageNamePack pack = new VillageNamePack("custom", false);
        pack.addTranslation("en_us", "Castle", "Castle");
        pack.addTranslation("pt_br", "Castle", "Castelo");
        pack.addTranslation("es_es", "Tower", "Torre");

        // Idioma exato
        assertEquals("Castelo", pack.translate("Castle", "pt_br"));
        assertEquals("Castle", pack.translate("Castle", "en_us"));

        // Fallback para formato abreviado (es_es -> es)
        pack.addTranslation("de", "River", "Fluss");
        assertEquals("Fluss", pack.translate("River", "de_de"));

        // Fallback para en_us quando idioma solicitado não tem tradução
        assertEquals("Castle", pack.translate("Castle", "fr_fr"));

        // Retorna chave original se nada encontrado
        assertEquals("UnknownWord", pack.translate("UnknownWord", "pt_br"));
    }

    @Test
    public void testMergeWithoutReplace() {
        VillageNamePack base = new VillageNamePack("plains", false);
        base.addPrefix("Belo");
        base.addSuffix("val");
        base.addUniqueName("Alvorada");
        base.addTranslation("pt_br", "Belo", "Belo");

        VillageNamePack extension = new VillageNamePack("plains", false);
        extension.addPrefix("Monte");
        extension.addSuffix("burgo");
        extension.addUniqueName("Nova Terra");
        extension.addTranslation("en_us", "Monte", "Mount");

        base.merge(extension);

        assertEquals(2, base.getPrefixes().size());
        assertTrue(base.getPrefixes().contains("Belo"));
        assertTrue(base.getPrefixes().contains("Monte"));

        assertEquals(2, base.getSuffixes().size());
        assertTrue(base.getSuffixes().contains("val"));
        assertTrue(base.getSuffixes().contains("burgo"));

        assertEquals(2, base.getUniqueNames().size());
        assertTrue(base.getUniqueNames().contains("Alvorada"));
        assertTrue(base.getUniqueNames().contains("Nova Terra"));

        assertEquals("Belo", base.translate("Belo", "pt_br"));
        assertEquals("Mount", base.translate("Monte", "en_us"));
    }

    @Test
    public void testMergeWithReplace() {
        VillageNamePack base = new VillageNamePack("plains", false);
        base.addPrefix("Belo");
        base.addSuffix("val");
        base.addUniqueName("Alvorada");

        VillageNamePack overridePack = new VillageNamePack("plains", true);
        overridePack.addPrefix("Novo");
        overridePack.addSuffix("burgo");

        base.merge(overridePack);

        assertTrue(base.isReplace());
        assertEquals(1, base.getPrefixes().size());
        assertEquals("Novo", base.getPrefixes().get(0));
        assertEquals(1, base.getSuffixes().size());
        assertEquals("burgo", base.getSuffixes().get(0));
        assertTrue(base.getUniqueNames().isEmpty());
    }
}
