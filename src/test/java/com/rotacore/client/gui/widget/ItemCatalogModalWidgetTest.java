package com.rotacore.client.gui.widget;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ItemCatalogModalWidgetTest {

    @Test
    public void testQuantityModeInitialization() {
        AtomicReference<ItemStack> chosenStack = new AtomicReference<>();
        AtomicInteger chosenQty = new AtomicInteger(0);

        ItemCatalogModalWidget modal = new ItemCatalogModalWidget(
                null,
                16,
                (stack, qty) -> {
                    chosenStack.set(stack);
                    chosenQty.set(qty);
                }
        );

        assertTrue(modal.isQuantityMode());
        assertEquals(16, modal.getSelectedQuantity());
        assertEquals(236, modal.getPanelHeight());

        // Alteração de quantidade
        modal.setSelectedQuantity(32);
        assertEquals(32, modal.getSelectedQuantity());

        modal.setSelectedQuantity(0);
        assertEquals(1, modal.getSelectedQuantity(), "Quantidade mínima deve ser 1");
    }

    @Test
    public void testStandardModeInitialization() {
        ItemCatalogModalWidget modal = new ItemCatalogModalWidget(null, stack -> {});

        assertFalse(modal.isQuantityMode());
        assertEquals(190, modal.getPanelHeight());
        assertEquals(1, modal.getSelectedQuantity());
    }

    @Test
    public void testScrollCalculations() {
        ItemCatalogModalWidget modal = new ItemCatalogModalWidget(null, stack -> {});

        // Estado inicial vazio
        assertEquals(0, modal.getTotalRows());
        assertEquals(0.0, modal.getMaxScroll());

        modal.setScrollOffset(50.0);
        assertEquals(0.0, modal.getScrollOffset(), "Sem linhas além do visível, scroll deve ser clampado em 0.0");
    }
}
