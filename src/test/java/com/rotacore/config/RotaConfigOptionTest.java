package com.rotacore.config;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RotaConfigOptionTest {

    @Test
    public void testIconPreviews() {
        ResourceLocation icon1 = ResourceLocation.fromNamespaceAndPath("rota_map", "textures/gui/markers/icon_village.png");
        ResourceLocation icon2 = ResourceLocation.fromNamespaceAndPath("rota_map", "textures/gui/markers/icon_outpost.png");

        RotaIconPreview p1 = RotaIconPreview.of(icon1, "Vila");
        RotaIconPreview p2 = RotaIconPreview.of(icon2, Component.literal("Posto"));

        assertEquals("Vila", p1.label().getString());
        assertEquals("Posto", p2.label().getString());

        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Tema Teste"),
                "Descricao do tema",
                "Dica do tema",
                "Usar",
                () -> {}
        );

        assertFalse(opt.hasIconPreviews());
        assertEquals(0, opt.getIconPreviews().size());

        opt.withIconPreviews(List.of(p1, p2));

        assertTrue(opt.hasIconPreviews());
        assertEquals(2, opt.getIconPreviews().size());
        assertEquals(icon1, opt.getIconPreviews().get(0).texture());
        assertEquals(icon2, opt.getIconPreviews().get(1).texture());
    }
}
