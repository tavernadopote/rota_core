/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 */
package com.rotacore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class RotaKeyBindings {
    public static final String KEY_CATEGORY = "key.categories.rota_core";
    public static final String KEY_OPEN_HUB = "key.rota_core.open_hub";
    public static final String KEY_OPEN_HUD_EDITOR = "key.rota_core.open_hud_editor";
    public static final KeyMapping OPEN_HUB = new KeyMapping("key.rota_core.open_hub", InputConstants.Type.KEYSYM, 79, "key.categories.rota_core");
    public static final KeyMapping OPEN_HUD_EDITOR = new KeyMapping("key.rota_core.open_hud_editor", InputConstants.UNKNOWN.getValue(), "key.categories.rota_core");

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HUB);
        event.register(OPEN_HUD_EDITOR);
    }
}

