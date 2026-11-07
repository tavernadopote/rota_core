package com.rotacore.player;

import com.rotacore.config.RotaConfigOption;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class RotaPlayerIdentityTest {

    @TempDir
    Path tempDir;

    private Path testConfigFile;

    @BeforeEach
    public void setup() {
        testConfigFile = tempDir.resolve("test_player.json");
        RotaPlayerIdentity.setCustomConfigPath(testConfigFile);
        RotaPlayerIdentity.setPlayerColor(DyeColor.LIME);
    }

    @AfterEach
    public void cleanup() {
        RotaPlayerIdentity.setCustomConfigPath(null);
    }

    @Test
    public void testAllSixteenColorsHaveValidRgbAndHex() {
        for (DyeColor color : DyeColor.values()) {
            int rgb = RotaPlayerIdentity.getPlayerColorRGB(color);
            assertTrue(rgb >= 0 && rgb <= 0xFFFFFF, "RGB fora dos limites para " + color);

            String hex = RotaPlayerIdentity.getColorHex(color);
            assertNotNull(hex);
            assertTrue(hex.startsWith("#"));
            assertEquals(7, hex.length(), "Hex inválido para " + color);

            String namePt = RotaPlayerIdentity.getColorNamePt(color);
            assertNotNull(namePt);
            assertFalse(namePt.isBlank());

            String nameEn = RotaPlayerIdentity.getColorNameEn(color);
            assertNotNull(nameEn);
            assertFalse(nameEn.isBlank());
        }
    }

    @Test
    public void testSpecificColorValues() {
        assertEquals(0xFFFFFF, RotaPlayerIdentity.getPlayerColorRGB(DyeColor.WHITE));
        assertEquals("#FFFFFF", RotaPlayerIdentity.getColorHex(DyeColor.WHITE));
        assertEquals("Branco", RotaPlayerIdentity.getColorNamePt(DyeColor.WHITE));
        assertEquals("White", RotaPlayerIdentity.getColorNameEn(DyeColor.WHITE));

        assertEquals(0x80C71F, RotaPlayerIdentity.getPlayerColorRGB(DyeColor.LIME));
        assertEquals("#80C71F", RotaPlayerIdentity.getColorHex(DyeColor.LIME));
        assertEquals("Verde Limão", RotaPlayerIdentity.getColorNamePt(DyeColor.LIME));
        assertEquals("Lime", RotaPlayerIdentity.getColorNameEn(DyeColor.LIME));

        assertEquals(0x3AB3DA, RotaPlayerIdentity.getPlayerColorRGB(DyeColor.LIGHT_BLUE));
        assertEquals("#3AB3DA", RotaPlayerIdentity.getColorHex(DyeColor.LIGHT_BLUE));
        assertEquals("Azul Claro", RotaPlayerIdentity.getColorNamePt(DyeColor.LIGHT_BLUE));
        assertEquals("Light Blue", RotaPlayerIdentity.getColorNameEn(DyeColor.LIGHT_BLUE));
    }

    @Test
    public void testPersistenceSaveAndLoad() {
        RotaPlayerIdentity.setPlayerColor(DyeColor.CYAN);
        assertEquals(DyeColor.CYAN, RotaPlayerIdentity.getPlayerColor());
        assertTrue(Files.exists(testConfigFile));

        // Força recarregamento simulado apontando para o mesmo arquivo salvo
        RotaPlayerIdentity.load();
        assertEquals(DyeColor.CYAN, RotaPlayerIdentity.getPlayerColor());
        assertEquals(0x169C9C, RotaPlayerIdentity.getPlayerColorRGB());
    }

    @Test
    public void testCycleForwardAndBackward() {
        RotaPlayerIdentity.setPlayerColor(DyeColor.WHITE); // ordinal 0
        assertEquals(DyeColor.WHITE, RotaPlayerIdentity.getPlayerColor());

        RotaPlayerIdentity.cyclePlayerColor(true);
        assertEquals(DyeColor.ORANGE, RotaPlayerIdentity.getPlayerColor());

        RotaPlayerIdentity.cyclePlayerColor(false);
        assertEquals(DyeColor.WHITE, RotaPlayerIdentity.getPlayerColor());

        // Ciclo reverso a partir de WHITE deve ir para BLACK (último)
        RotaPlayerIdentity.cyclePlayerColor(false);
        assertEquals(DyeColor.BLACK, RotaPlayerIdentity.getPlayerColor());

        // Ciclo para frente a partir de BLACK deve voltar para WHITE
        RotaPlayerIdentity.cyclePlayerColor(true);
        assertEquals(DyeColor.WHITE, RotaPlayerIdentity.getPlayerColor());
    }

    @Test
    public void testConfigOptionGovernance() {
        RotaConfigOption opt = RotaPlayerIdentity.createConfigOption();
        assertNotNull(opt);
        assertEquals(RotaConfigOption.Type.CYCLE, opt.getType());
        assertFalse(opt.requiresAdmin(), "Opção cosmética de cliente não pode requerer administrador");
        assertTrue(opt.isClientOnly(), "Opção de cor de identidade deve ser marcada como clientOnly");
    }
}
