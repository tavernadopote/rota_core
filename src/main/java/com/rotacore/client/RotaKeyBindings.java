package com.rotacore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Teclas de atalho da suíte Rota Mods.
 */
public class RotaKeyBindings {

    public static final String KEY_CATEGORY = "key.categories.rota_core";
    public static final String KEY_OPEN_HUB = "key.rota_core.open_hub";

    public static final KeyMapping OPEN_HUB = new KeyMapping(
            KEY_OPEN_HUB,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
    }
}
