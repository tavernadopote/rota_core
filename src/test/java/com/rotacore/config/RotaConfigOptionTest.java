package com.rotacore.config;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

    @Test
    public void testTexturePreview() {
        ResourceLocation themeTexture = ResourceLocation.fromNamespaceAndPath("rota_map", "textures/gui/preview/theme_satellite.png");
        Component caption = Component.literal("Perfil Geográfico Sintético");

        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Tema Satélite"),
                "Descrição do tema",
                "Dica do tema",
                "Selecionar",
                () -> {}
        );

        assertFalse(opt.hasTexturePreview());
        assertNull(opt.getTexturePreviewLocation());
        assertEquals(128, opt.getTexturePreviewWidth());
        assertEquals(128, opt.getTexturePreviewHeight());
        assertNull(opt.getTexturePreviewCaption());

        RotaConfigOption returnedOpt = opt.withTexturePreview(themeTexture, 128, 128, caption);
        assertSame(opt, returnedOpt);
        assertTrue(opt.hasTexturePreview());
        assertEquals(themeTexture, opt.getTexturePreviewLocation());
        assertEquals(128, opt.getTexturePreviewWidth());
        assertEquals(128, opt.getTexturePreviewHeight());
        assertEquals("Perfil Geográfico Sintético", opt.getTexturePreviewCaption().getString());

        // Teste sem legenda
        RotaConfigOption optWithoutCaption = RotaConfigOption.action(
                Component.literal("Tema Sem Legenda"),
                "Descrição",
                "Dica",
                "Ativar",
                () -> {}
        ).withTexturePreview(themeTexture, 64, 48);

        assertTrue(optWithoutCaption.hasTexturePreview());
        assertEquals(themeTexture, optWithoutCaption.getTexturePreviewLocation());
        assertEquals(64, optWithoutCaption.getTexturePreviewWidth());
        assertEquals(48, optWithoutCaption.getTexturePreviewHeight());
        assertNull(optWithoutCaption.getTexturePreviewCaption());
    }

    @Test
    public void testSecondaryActionFluentAndGetters() {
        boolean[] actionExecuted = new boolean[]{false};
        boolean[] secondaryExecuted = new boolean[]{false};

        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Tema Pergaminho"),
                "Descrição do tema",
                "Dica do tema",
                "Usar Tema",
                () -> actionExecuted[0] = true
        );

        // Inicialmente sem ação secundária
        assertFalse(opt.hasSecondaryAction());
        assertNull(opt.getSecondaryAction());
        assertNotNull(opt.getSecondaryActionTooltip());
        assertEquals("Configurar Parâmetros...", opt.getSecondaryActionTooltip().getString());

        // Configuração com ação secundária e tooltip padrão
        RotaConfigOption returnedOpt = opt.withSecondaryAction(() -> secondaryExecuted[0] = true);
        assertSame(opt, returnedOpt);
        assertTrue(opt.hasSecondaryAction());
        assertNotNull(opt.getSecondaryAction());
        assertEquals("Configurar Parâmetros...", opt.getSecondaryActionTooltip().getString());

        // Execução da ação secundária
        opt.getSecondaryAction().run();
        assertTrue(secondaryExecuted[0]);
        assertFalse(actionExecuted[0]);

        // Execução da ação principal
        opt.triggerAction();
        assertTrue(actionExecuted[0]);

        // Configuração com ação secundária e tooltip customizado
        Component customTooltip = Component.literal("Personalizar cores e relevo do tema...");
        opt.withSecondaryAction(() -> {}, customTooltip);
        assertTrue(opt.hasSecondaryAction());
        assertEquals("Personalizar cores e relevo do tema...", opt.getSecondaryActionTooltip().getString());
    }

    @Test
    public void testTipGovernanceAndGenericFiltering() {
        // Opcao sem dica (null ou vazia)
        RotaConfigOption optNoTip = RotaConfigOption.booleanToggle(
                Component.literal("Sem Dica"),
                "Descricao",
                true,
                () -> true,
                v -> {}
        );
        assertFalse(optNoTip.hasTip());
        assertFalse(optNoTip.hasCustomTip());

        // Dicas genericas que refraseiam o controle sao filtradas
        RotaConfigOption optGeneric1 = RotaConfigOption.booleanToggle(
                Component.literal("Generico 1"),
                "Descricao",
                "Ativado: liga recurso. Desativado: desliga recurso.",
                true,
                () -> true,
                v -> {}
        );
        assertFalse(optGeneric1.hasTip());

        RotaConfigOption optGeneric2 = RotaConfigOption.intSlider(
                Component.literal("Generico 2"),
                "Descricao",
                "Padrao: 3 blocos.",
                3,
                1,
                10,
                null,
                () -> 3,
                v -> {}
        );
        assertFalse(optGeneric2.hasTip());

        // Dica explicita relevante do desenvolvedor
        RotaConfigOption optCustom = RotaConfigOption.booleanToggle(
                Component.literal("Custom"),
                "Descricao",
                "Utilize esta opcao para reduzir o impacto grafico em servidores remotos.",
                true,
                () -> true,
                v -> {}
        );
        assertTrue(optCustom.hasTip());
        assertTrue(optCustom.hasCustomTip());

        // Override explicito via withCustomTip
        RotaConfigOption optForced = RotaConfigOption.booleanToggle(
                Component.literal("Forcado"),
                "Descricao",
                "Ativado: modo avancado",
                true,
                () -> true,
                v -> {}
        ).withCustomTip("Ativado: modo avancado");
        assertTrue(optForced.hasTip());
        assertTrue(optForced.isCustomTipExplicit());

        // Governanca de exibicao do valor padrao
        assertFalse(optCustom.shouldShowDefaultValue());
        optCustom.withShowDefaultValue(true);
        assertTrue(optCustom.shouldShowDefaultValue());
    }

    @Test
    public void testConvenienceFactoryOverloads() {
        RotaConfigOption boolOpt = RotaConfigOption.booleanToggle(
                Component.literal("Toggle"),
                "Desc",
                false,
                () -> false,
                v -> {}
        );
        assertEquals(RotaConfigOption.Type.BOOLEAN, boolOpt.getType());
        assertNull(boolOpt.getExampleTip());
        assertFalse(boolOpt.hasTip());

        RotaConfigOption intOpt = RotaConfigOption.intSlider(
                Component.literal("Slider"),
                "Desc",
                5,
                1,
                10,
                String::valueOf,
                () -> 5,
                v -> {}
        );
        assertEquals(RotaConfigOption.Type.INT_SLIDER, intOpt.getType());
        assertNull(intOpt.getExampleTip());

        RotaConfigOption doubleOpt = RotaConfigOption.doubleSlider(
                Component.literal("Double"),
                "Desc",
                2.5,
                1.0,
                5.0,
                null,
                () -> 2.5,
                v -> {}
        );
        assertEquals(RotaConfigOption.Type.DOUBLE_SLIDER, doubleOpt.getType());
        assertNull(doubleOpt.getExampleTip());

        RotaConfigOption actOpt = RotaConfigOption.action(
                Component.literal("Action"),
                "Desc",
                "Executar",
                () -> {}
        );
        assertEquals(RotaConfigOption.Type.ACTION, actOpt.getType());
        assertNull(actOpt.getExampleTip());

        RotaConfigOption cycleOpt = RotaConfigOption.cycle(
                Component.literal("Cycle"),
                "Desc",
                "Normal",
                () -> "Normal",
                () -> {}
        );
        assertEquals(RotaConfigOption.Type.CYCLE, cycleOpt.getType());
        assertNull(cycleOpt.getExampleTip());
    }

    @Test
    public void testItemEntryOption() {
        boolean[] active = new boolean[]{true};
        boolean[] movedUp = new boolean[]{false};
        boolean[] movedDown = new boolean[]{false};
        boolean[] edited = new boolean[]{false};

        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/book.png");

        RotaConfigOption opt = RotaConfigOption.itemEntry(
                Component.literal("Capítulo 1"),
                "Descrição do capítulo",
                tex,
                () -> active[0],
                v -> active[0] = v,
                () -> movedUp[0] = true,
                () -> movedDown[0] = true,
                () -> edited[0] = true
        );

        assertEquals(RotaConfigOption.Type.ITEM_ENTRY, opt.getType());
        assertEquals("Capítulo 1", opt.getTitle().getString());
        assertEquals("Descrição do capítulo", opt.getDescription());
        assertTrue(opt.hasTextureIcon());
        assertEquals(tex, opt.getTextureIcon());
        assertFalse(opt.hasItemStackIcon());

        // Toggle
        assertTrue(opt.hasItemToggle());
        assertTrue(opt.isItemActive());
        opt.setItemActive(false);
        assertFalse(active[0]);
        assertFalse(opt.isItemActive());

        // Reordenação
        assertTrue(opt.hasReorderActions());
        assertTrue(opt.isShowReorderButtons());
        opt.moveUp();
        assertTrue(movedUp[0]);
        opt.moveDown();
        assertTrue(movedDown[0]);

        // Ação Secundária (⚙)
        assertTrue(opt.hasSecondaryAction());
        opt.getSecondaryAction().run();
        assertTrue(edited[0]);
    }

    @Test
    public void testStringInputOption() {
        String[] text = new String[]{"Texto Inicial"};
        RotaConfigOption opt = RotaConfigOption.stringInput(
                Component.literal("Título da Missão"),
                "Digite o nome da missão",
                "Ex: Jornada Inicial",
                "Texto Inicial",
                () -> text[0],
                val -> text[0] = val
        );

        assertEquals(RotaConfigOption.Type.STRING_INPUT, opt.getType());
        assertEquals("Título da Missão", opt.getTitle().getString());
        assertEquals("Digite o nome da missão", opt.getDescription());
        assertEquals("Ex: Jornada Inicial", opt.getExampleTip());
        assertEquals("Texto Inicial", opt.getStringValue());

        opt.setStringValue("Missão dos Titãs");
        assertEquals("Missão dos Titãs", text[0]);
        assertEquals("Missão dos Titãs", opt.getStringValue());
    }

    @Test
    public void testMultilineStringInputOption() {
        String[] story = new String[]{"Era uma vez um aventureiro..."};
        RotaConfigOption opt = RotaConfigOption.multilineStringInput(
                Component.literal("História Narrativa"),
                "Digite a crônica completa da missão",
                "Ex: Descreva a lenda antiga...",
                "Era uma vez...",
                80,
                () -> story[0],
                val -> story[0] = val
        );

        assertEquals(RotaConfigOption.Type.MULTILINE_STRING_INPUT, opt.getType());
        assertEquals("História Narrativa", opt.getTitle().getString());
        assertEquals("Digite a crônica completa da missão", opt.getDescription());
        assertEquals("Ex: Descreva a lenda antiga...", opt.getExampleTip());
        assertEquals(80, opt.getMultilineHeight());
        assertEquals("Era uma vez um aventureiro...", opt.getStringValue());

        opt.setStringValue("Capítulo 1: O início da jornada na floresta.");
        assertEquals("Capítulo 1: O início da jornada na floresta.", story[0]);
        assertEquals("Capítulo 1: O início da jornada na floresta.", opt.getStringValue());

        // Teste de fábrica sobrecarregada com altura padrão (60px)
        RotaConfigOption defaultOpt = RotaConfigOption.multilineStringInput(
                Component.literal("Crônica"),
                "Descrição",
                "Texto Padrão",
                () -> "Conteúdo",
                v -> {}
        );
        assertEquals(60, defaultOpt.getMultilineHeight());

        defaultOpt.withMultilineHeight(100);
        assertEquals(100, defaultOpt.getMultilineHeight());
    }

    @Test
    public void testDangerAction() {
        boolean[] ran = new boolean[]{false};
        RotaConfigOption opt = RotaConfigOption.dangerAction(
                Component.literal("Excluir Missão"),
                "Ação destrutiva e irreversível",
                "Dica de perigo",
                "Excluir",
                () -> ran[0] = true
        );

        assertEquals(RotaConfigOption.Type.ACTION, opt.getType());
        assertTrue(opt.isDangerAction());
        assertEquals("Excluir Missão", opt.getTitle().getString());
        assertEquals("Excluir", opt.getActionButtonText());

        opt.triggerAction();
        assertTrue(ran[0]);

        // Teste de sobrecarga simplificada
        RotaConfigOption simpleOpt = RotaConfigOption.dangerAction(
                Component.literal("Excluir Capítulo"),
                "Remove o capítulo",
                "Apagar",
                () -> {}
        );
        assertTrue(simpleOpt.isDangerAction());
        assertEquals("Apagar", simpleOpt.getActionButtonText());
    }

    @Test
    public void testActionIconSupplier() {
        RotaConfigOption opt = RotaConfigOption.action(
                Component.literal("Ícone da Missão"),
                "Seletor de ícone",
                "Alterar...",
                () -> {}
        );

        assertNull(opt.getActionIconSupplier());

        opt.withActionIcon(() -> null);
        assertNotNull(opt.getActionIconSupplier());
        assertNull(opt.getActionIconSupplier().get());
    }
}
