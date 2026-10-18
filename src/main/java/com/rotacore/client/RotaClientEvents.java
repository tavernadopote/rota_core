package com.rotacore.client;

import com.rotacore.RotaCore;
import com.rotacore.api.RotaCoreRegistry;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.RotaHubScreen;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.config.RotaCoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Ouvintes de eventos do lado do cliente para a integração do Hub e menus oficiais.
 */
public class RotaClientEvents {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        // 1. Injeção no Menu de Pausa (ESC)
        if (screen instanceof PauseScreen) {
            handlePauseScreenInit(screen, event);
        }

        // 2. Injeção na Tela Oficial de Opções (OptionsScreen)
        if (screen instanceof OptionsScreen optionsScreen) {
            handleOptionsScreenInit(optionsScreen, event);
        }
    }

    private static void handlePauseScreenInit(Screen screen, ScreenEvent.Init.Post event) {
        RotaCoreConfig config = RotaCore.getConfig();
        if (!config.showPauseMenuButton) return;

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

    private static void handleOptionsScreenInit(OptionsScreen optionsScreen, ScreenEvent.Init.Post event) {
        RotaCoreConfig config = RotaCore.getConfig();
        List<RotaModule> modules = RotaCoreRegistry.getAllModules();

        // Identifica quais módulos estão ativos para aparecer na tela de Opções
        List<RotaModule> activeMods = new ArrayList<>();
        for (RotaModule mod : modules) {
            if (config.isModuleEnabledInOptions(mod.getId())) {
                activeMods.add(mod);
            }
        }

        boolean showHub = config.showHubInOptionsScreen;
        if (activeMods.isEmpty() && !showHub) {
            return;
        }

        // Encontra o Y da última linha do grid de botões do Minecraft (abaixo de Telemetria / Créditos)
        int maxGridY = -1;
        Button doneBtn = null;

        for (GuiEventListener child : optionsScreen.children()) {
            if (child instanceof Button btn) {
                if (btn.getWidth() <= 160) {
                    maxGridY = Math.max(maxGridY, btn.getY());
                } else if (btn.getWidth() >= 190) {
                    doneBtn = btn;
                }
            }
        }

        if (maxGridY == -1) {
            maxGridY = optionsScreen.height / 2 + 48;
        }

        // Monta os botões a serem adicionados
        List<Button> buttonsToAdd = new ArrayList<>();

        for (RotaModule mod : activeMods) {
            String title = mod.getDisplayName().getString();
            if (!title.endsWith("...")) {
                title += "...";
            }
            Button modBtn = Button.builder(
                    Component.literal(title),
                    btn -> Minecraft.getInstance().setScreen(mod.createConfigScreen(optionsScreen))
            ).bounds(0, 0, 150, 20).build();

            buttonsToAdd.add(modBtn);
        }

        if (showHub) {
            Button hubBtn = Button.builder(
                    Component.literal("Rota Mods Hub..."),
                    btn -> Minecraft.getInstance().setScreen(new RotaHubScreen(optionsScreen))
            ).bounds(0, 0, 150, 20).build();

            buttonsToAdd.add(hubBtn);
        }

        int centerX = optionsScreen.width / 2;
        int startRowY = maxGridY + 24;

        if (buttonsToAdd.size() == 1) {
            Button single = buttonsToAdd.get(0);
            single.setX(centerX - 75);
            single.setY(startRowY);
            event.addListener(single);
        } else {
            for (int i = 0; i < buttonsToAdd.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                int x = (col == 0) ? (centerX - 155) : (centerX + 5);
                int y = startRowY + row * 24;

                Button btn = buttonsToAdd.get(i);
                btn.setX(x);
                btn.setY(y);
                event.addListener(btn);
            }
        }

        // Ajusta o botão Concluído se houver risco de sobreposição
        int totalRows = (buttonsToAdd.size() + 1) / 2;
        int lastRowBottom = startRowY + totalRows * 24;
        if (doneBtn != null && doneBtn.getY() < lastRowBottom + 4) {
            int newDoneY = Math.min(optionsScreen.height - 26, lastRowBottom + 6);
            doneBtn.setY(newDoneY);
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
