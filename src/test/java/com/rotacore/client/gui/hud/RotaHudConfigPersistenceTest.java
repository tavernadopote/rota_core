package com.rotacore.client.gui.hud;

import com.rotacore.config.RotaCoreConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RotaHudConfigPersistenceTest {

    @Test
    public void testSetAndResetHudPosition() {
        RotaCoreConfig config = new RotaCoreConfig();

        config.setHudElementPosition("rota_map:minimap", 150, 220);
        RotaCoreConfig.HudElementConfig elemCfg = config.getHudElementConfig("rota_map:minimap");

        assertNotNull(elemCfg);
        assertTrue(elemCfg.customPosition);
        assertEquals(150, elemCfg.customX);
        assertEquals(220, elemCfg.customY);

        config.resetHudElementPosition("rota_map:minimap");
        assertFalse(elemCfg.customPosition);
        assertEquals(0, elemCfg.customX);
        assertEquals(0, elemCfg.customY);
    }

    @Test
    public void testResetAllHudPositions() {
        RotaCoreConfig config = new RotaCoreConfig();
        config.setHudElementPosition("hud1", 10, 20);
        config.setHudElementPosition("hud2", 30, 40);

        assertEquals(2, config.hudElements.size());

        config.resetAllHudPositions();
        assertEquals(0, config.hudElements.size());
    }

    @Test
    public void testValidationSanitizesNulls() {
        RotaCoreConfig config = new RotaCoreConfig();
        config.hudElements = null;
        config.validate();

        assertNotNull(config.hudElements);

        config.hudElements.put("corrupted", null);
        config.validate();
        assertNotNull(config.hudElements.get("corrupted"));
    }
}
