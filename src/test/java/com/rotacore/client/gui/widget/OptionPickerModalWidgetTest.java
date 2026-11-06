package com.rotacore.client.gui.widget;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OptionPickerModalWidgetTest {

    @Test
    public void testFilterAndSelection() {
        OptionPickerModalWidget.PickerOption opt1 = new OptionPickerModalWidget.PickerOption("chapter_1", Component.literal("Capítulo 1: Sobrevivência"));
        OptionPickerModalWidget.PickerOption opt2 = new OptionPickerModalWidget.PickerOption("chapter_2", Component.literal("Capítulo 2: Pecuária"));
        OptionPickerModalWidget.PickerOption opt3 = new OptionPickerModalWidget.PickerOption("chapter_3", Component.literal("Capítulo 3: Automação"));

        OptionPickerModalWidget.PickerOption[] selected = new OptionPickerModalWidget.PickerOption[]{null};

        OptionPickerModalWidget modal = new OptionPickerModalWidget(
                null,
                Component.literal("Selecionar Capítulo"),
                List.of(opt1, opt2, opt3),
                res -> selected[0] = res
        );

        assertEquals(3, modal.getAllOptions().size());
        assertEquals(3, modal.getFilteredOptions().size());

        // Filtragem por texto
        modal.filterOptions("pecuária");
        assertEquals(1, modal.getFilteredOptions().size());
        assertEquals("chapter_2", modal.getFilteredOptions().get(0).id());

        // Filtragem sem correspondência
        modal.filterOptions("inexistente");
        assertEquals(0, modal.getFilteredOptions().size());

        // Limpeza de filtro
        modal.filterOptions("");
        assertEquals(3, modal.getFilteredOptions().size());

        // Seleção de opção
        modal.selectOption(opt2);
        assertNotNull(selected[0]);
        assertEquals("chapter_2", selected[0].id());
        assertEquals("Capítulo 2: Pecuária", selected[0].label().getString());
    }
}
