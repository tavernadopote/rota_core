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

    @Test
    public void testFluentPermissionFlags() {
        RotaConfigOption opt = RotaConfigOption.booleanToggle(
                Component.literal("Configuração Teste"),
                "Descrição",
                "Dica",
                true,
                () -> true,
                v -> {}
        );

        // Valores padrão
        assertFalse(opt.requiresAdmin());
        assertFalse(opt.isClientOnly());
        assertFalse(opt.clientOnly());

        // Encadeamento fluente requiresAdmin
        RotaConfigOption returnedOpt = opt.requiresAdmin(true);
        assertSame(opt, returnedOpt);
        assertTrue(opt.requiresAdmin());

        // Encadeamento fluente clientOnly
        returnedOpt = opt.clientOnly(true);
        assertSame(opt, returnedOpt);
        assertTrue(opt.isClientOnly());
        assertTrue(opt.clientOnly());

        opt.requiresAdmin(false);
        assertFalse(opt.requiresAdmin());
    }

    @Test
    public void testCanPlayerEditLocalVsMultiplayer() {
        RotaConfigOption clientOpt = RotaConfigOption.booleanToggle(
                Component.literal("Opção Cliente"),
                "Cosmético",
                "Dica",
                false,
                () -> false,
                v -> {}
        ); // requiresAdmin = false por padrão

        // Opções cosméticas/locais do cliente são sempre editáveis
        assertTrue(clientOpt.canPlayerEdit(true, false), "Singleplayer sem admin deve permitir edição de opção do cliente");
        assertTrue(clientOpt.canPlayerEdit(true, true), "Singleplayer com admin deve permitir edição de opção do cliente");
        assertTrue(clientOpt.canPlayerEdit(false, false), "Multiplayer sem admin deve permitir edição de opção do cliente");
        assertTrue(clientOpt.canPlayerEdit(false, true), "Multiplayer com admin deve permitir edição de opção do cliente");

        RotaConfigOption adminOpt = RotaConfigOption.booleanToggle(
                Component.literal("Opção Servidor"),
                "Impacta o mundo",
                "Dica",
                true,
                () -> true,
                v -> {}
        ).requiresAdmin(true);

        // Singleplayer: o jogador é o dono do mundo com autoridade plena, sempre permitido
        assertTrue(adminOpt.canPlayerEdit(true, false), "Singleplayer deve sempre permitir edição mesmo se requiresAdmin for true");
        assertTrue(adminOpt.canPlayerEdit(true, true), "Singleplayer com admin deve permitir edição");

        // Multiplayer: bloqueado para jogadores comuns, permitido apenas para operadores/admins
        assertFalse(adminOpt.canPlayerEdit(false, false), "Multiplayer sem privilégio admin deve bloquear a edição");
        assertTrue(adminOpt.canPlayerEdit(false, true), "Multiplayer com privilégio admin deve permitir a edição");
    }

    @Test
    public void testCanPlayerEditWithMinecraftNull() {
        RotaConfigOption adminOpt = RotaConfigOption.header("Cabeçalho").requiresAdmin(true);
        // Fallback seguro quando Minecraft instance for nula
        assertTrue(adminOpt.canPlayerEdit((net.minecraft.client.Minecraft) null));
    }
}
