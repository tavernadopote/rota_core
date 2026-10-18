package com.rotacore.client;

import com.rotacore.client.gui.RotaHubScreen;
import com.rotacore.client.gui.widget.RotaButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Ouvintes de eventos do lado do cliente para a integração do Hub da Rota Mods.
 */
public class RotaClientEvents {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof PauseScreen) {
            int btnW = 90;
            int btnH = 20;
            int x = screen.width - btnW - 10;
            int y = 10;

            RotaButton hubBtn = new RotaButton(
                    x, y, btnW, btnH,
                    Component.literal("§6★ Rota Mods"),
                    btn -> Minecraft.getInstance().setScreen(new RotaHubScreen(screen))
            );
            hubBtn.setAccent(true);
            event.addListener(hubBtn);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (RotaKeyBindings.OPEN_HUB.consumeClick()) {
            mc.setScreen(new RotaHubScreen(mc.screen));
        }
    }
}
