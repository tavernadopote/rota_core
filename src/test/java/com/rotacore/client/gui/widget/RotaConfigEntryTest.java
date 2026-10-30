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
}
