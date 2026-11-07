package com.rotacore.client.gui.widget;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ConfirmModalWidgetTest {

    @Test
    public void testConfirmModalInstantiationAndExecution() {
        AtomicBoolean confirmed = new AtomicBoolean(false);
        Component title = Component.literal("Excluir Item");
        Component msg = Component.literal("Deseja realmente excluir este elemento?");
        Component confirmBtn = Component.literal("Excluir Definitivamente");

        ConfirmModalWidget modal = new ConfirmModalWidget(
                null,
                title,
                msg,
                confirmBtn,
                () -> confirmed.set(true)
        );

        assertEquals("Excluir Item", modal.getTitle().getString());
        assertEquals("Deseja realmente excluir este elemento?", modal.getMessage().getString());
        assertEquals("Excluir Definitivamente", modal.getConfirmText().getString());
        assertFalse(confirmed.get());

        // Execução da ação confirmada
        modal.getOnConfirm().run();
        assertTrue(confirmed.get());
    }

    @Test
    public void testDefaultValues() {
        AtomicBoolean confirmed = new AtomicBoolean(false);
        ConfirmModalWidget modal = new ConfirmModalWidget(null, null, null, null, () -> confirmed.set(true));

        assertEquals("§c§lConfirmar Ação", modal.getTitle().getString());
        assertEquals("Tem certeza de que deseja executar esta ação?", modal.getMessage().getString());
        assertEquals("Confirmar", modal.getConfirmText().getString());
        assertNotNull(modal.getOnConfirm());
    }
}
