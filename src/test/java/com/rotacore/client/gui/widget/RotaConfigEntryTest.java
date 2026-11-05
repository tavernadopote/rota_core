package com.rotacore.client.gui.widget;

import com.rotacore.config.RotaConfigOption;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RotaConfigEntryTest {

    @Test
    public void testEntryWithoutSecondaryAction() {
        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Ação Normal"),
                "Descrição",
                "Dica",
                "Executar",
                () -> {}
        );

        RotaConfigEntry entry = new RotaConfigEntry(opt, null, 120);

        assertNotNull(entry.getControlWidget());
        assertEquals(120, entry.getControlWidget().getWidth());
        assertNull(entry.getSecondaryButton());
        assertEquals(1, entry.children().size());
        assertEquals(1, entry.narratables().size());
    }

    @Test
    public void testEntryWithSecondaryAction() {
        boolean[] mainActionRan = new boolean[]{false};
        boolean[] secondaryActionRan = new boolean[]{false};

        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Tema Selecionável"),
                "Descrição",
                "Dica",
                "Usar Tema",
                () -> mainActionRan[0] = true
        ).withSecondaryAction(
                () -> secondaryActionRan[0] = true,
                Component.literal("Personalizar parâmetros do tema...")
        );

        RotaConfigEntry entry = new RotaConfigEntry(opt, null, 120);

        // O botão principal é encurtado em 24px (120 - 24 = 96)
        assertNotNull(entry.getControlWidget());
        assertEquals(96, entry.getControlWidget().getWidth());

        // O botão de engrenagem é instanciado com 20x20 e símbolo de engrenagem
        RotaButton gearButton = entry.getSecondaryButton();
        assertNotNull(gearButton);
        assertEquals(20, gearButton.getWidth());
        assertEquals(20, gearButton.getHeight());
        assertEquals("⚙", gearButton.getMessage().getString());

        // Ambos os widgets são registrados como filhos e narratáveis
        assertTrue(entry.children().contains(gearButton));
        assertTrue(entry.narratables().contains(gearButton));
        assertTrue(entry.children().contains(entry.getControlWidget()));
        assertTrue(entry.narratables().contains(entry.getControlWidget()));

        // Disparo do clique na engrenagem executa a ação secundária
        gearButton.onPress();
        assertTrue(secondaryActionRan[0]);
        assertFalse(mainActionRan[0]);
    }

    @Test
    public void testEntryWithSecondaryActionOnSwitchAndSliders() {
        RotaConfigOption boolOpt = RotaConfigOption.booleanToggle(
                Component.literal("Interruptor"),
                "Desc",
                "Dica",
                true,
                () -> true,
                v -> {}
        ).withSecondaryAction(() -> {});

        RotaConfigEntry boolEntry = new RotaConfigEntry(boolOpt, null, 120);
        assertEquals(96, boolEntry.getControlWidget().getWidth());
        assertNotNull(boolEntry.getSecondaryButton());

        RotaConfigOption sliderOpt = RotaConfigOption.intSlider(
                Component.literal("Slider"),
                "Desc",
                "Dica",
                10, 0, 100,
                String::valueOf,
                () -> 10,
                v -> {}
        ).withSecondaryAction(() -> {});

        RotaConfigEntry sliderEntry = new RotaConfigEntry(sliderOpt, null, 120);
        assertEquals(96, sliderEntry.getControlWidget().getWidth());
        assertNotNull(sliderEntry.getSecondaryButton());
    }

    @Test
    public void testItemEntryWidgetsAndInteractions() {
        boolean[] active = new boolean[]{true};
        boolean[] movedUp = new boolean[]{false};
        boolean[] movedDown = new boolean[]{false};
        boolean[] edited = new boolean[]{false};

        net.minecraft.resources.ResourceLocation tex = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/compass.png");

        RotaConfigOption opt = RotaConfigOption.itemEntry(
                Component.literal("Missão de Reconhecimento"),
                "Explorar 500 blocos ao norte",
                tex,
                () -> active[0],
                v -> active[0] = v,
                () -> movedUp[0] = true,
                () -> movedDown[0] = true,
                () -> edited[0] = true
        );

        RotaConfigEntry entry = new RotaConfigEntry(opt, null, 120);

        assertNotNull(entry.getUpButton(), "Botão para cima deve estar presente");
        assertNotNull(entry.getDownButton(), "Botão para baixo deve estar presente");
        assertNotNull(entry.getItemToggle(), "Switch de ativação deve estar presente");
        assertNotNull(entry.getSecondaryButton(), "Botão de engrenagem deve estar presente");

        assertEquals(16, entry.getUpButton().getWidth());
        assertEquals(16, entry.getDownButton().getWidth());
        assertEquals(70, entry.getItemToggle().getWidth());
        assertEquals(20, entry.getSecondaryButton().getWidth());

        assertTrue(entry.children().contains(entry.getUpButton()));
        assertTrue(entry.children().contains(entry.getDownButton()));
        assertTrue(entry.children().contains(entry.getItemToggle()));
        assertTrue(entry.children().contains(entry.getSecondaryButton()));

        entry.getUpButton().onPress();
        assertTrue(movedUp[0]);

        entry.getDownButton().onPress();
        assertTrue(movedDown[0]);

        entry.getItemToggle().onPress();
        assertFalse(active[0]);

        entry.getSecondaryButton().onPress();
        assertTrue(edited[0]);
    }
}
