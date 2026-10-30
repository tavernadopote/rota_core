package com.rotacore.client.gui;

import com.rotacore.api.RotaCategory;
import com.rotacore.config.RotaConfigOption;
import com.rotacore.config.RotaIconPreview;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RotaCategoryScreenTest {

    @Test
    public void testMaxScrollCalculation() {
        // Quando o conteudo cabe perfeitamente ou e menor que o viewport visivel
        assertEquals(0.0, RotaCategoryScreen.calculateMaxScroll(100, 200), 0.001);
        assertEquals(0.0, RotaCategoryScreen.calculateMaxScroll(200, 200), 0.001);

        // Quando o conteudo excede a altura util
        assertEquals(50.0, RotaCategoryScreen.calculateMaxScroll(250, 200), 0.001);
        assertEquals(180.0, RotaCategoryScreen.calculateMaxScroll(380, 200), 0.001);
    }

    @Test
    public void testClampScroll() {
        double maxScroll = 120.0;

        // Limite inferior (clamp em zero)
        assertEquals(0.0, RotaCategoryScreen.clampScroll(-25.0, maxScroll), 0.001);
        assertEquals(0.0, RotaCategoryScreen.clampScroll(0.0, maxScroll), 0.001);

        // Valor intermediario valido
        assertEquals(64.5, RotaCategoryScreen.clampScroll(64.5, maxScroll), 0.001);

        // Limite superior (clamp em maxScroll)
        assertEquals(120.0, RotaCategoryScreen.clampScroll(120.0, maxScroll), 0.001);
        assertEquals(120.0, RotaCategoryScreen.clampScroll(200.0, maxScroll), 0.001);
    }

    @Test
    public void testApplyScrollDelta() {
        double maxScroll = 100.0;
        double currentScroll = 50.0;

        // Rolar para baixo (scrollY = -1.0) -> incrementa scroll em 16px
        double scrolledDown = RotaCategoryScreen.applyScrollDelta(currentScroll, -1.0, maxScroll);
        assertEquals(66.0, scrolledDown, 0.001);

        // Rolar para cima (scrollY = 1.0) -> decrementa scroll em 16px
        double scrolledUp = RotaCategoryScreen.applyScrollDelta(currentScroll, 1.0, maxScroll);
        assertEquals(34.0, scrolledUp, 0.001);

        // Tentativa de rolar alem do limite superior
        double overshootDown = RotaCategoryScreen.applyScrollDelta(95.0, -2.0, maxScroll);
        assertEquals(100.0, overshootDown, 0.001);

        // Tentativa de rolar alem do limite inferior
        double overshootUp = RotaCategoryScreen.applyScrollDelta(10.0, 2.0, maxScroll);
        assertEquals(0.0, overshootUp, 0.001);
    }

    @Test
    public void testScrollResetOnFocusedOptionChange() {
        RotaConfigOption opt1 = RotaConfigOption.booleanToggle(
                Component.literal("Opcao 1"),
                "Descricao 1",
                "Dica 1",
                true,
                () -> true,
                v -> {}
        );

        RotaConfigOption opt2 = RotaConfigOption.booleanToggle(
                Component.literal("Opcao 2"),
                "Descricao 2",
                "Dica 2",
                false,
                () -> false,
                v -> {}
        );

        RotaCategory category = new RotaCategory("test_cat", Component.literal("Categoria Teste"), "Desc", () -> List.of(opt1, opt2));
        RotaCategoryScreen screen = new RotaCategoryScreen(null, category);

        // Seleciona a primeira opcao e define deslocamento de scroll
        screen.setFocusedOption(opt1);
        screen.setInspectorScrollAmount(80.0);
        assertEquals(80.0, screen.getInspectorScrollAmount(), 0.001);

        // Reatribuir a mesma opcao deve preservar o deslocamento atual
        screen.setFocusedOption(opt1);
        assertEquals(80.0, screen.getInspectorScrollAmount(), 0.001);

        // Trocar para uma opcao diferente deve reiniciar o scroll para zero
        screen.setFocusedOption(opt2);
        assertEquals(0.0, screen.getInspectorScrollAmount(), 0.001);
    }

    @Test
    public void testContentHeightCalculationProgression() {
        RotaConfigOption optBase = RotaConfigOption.booleanToggle(
                Component.literal("Opcao Base"),
                "Descricao simples e concisa",
                null,
                true,
                () -> true,
                v -> {}
        );

        RotaCategory category = new RotaCategory("test_cat", Component.literal("Categoria Teste"), "Desc", () -> List.of(optBase));
        RotaCategoryScreen screen = new RotaCategoryScreen(null, category);

        int baseHeight = screen.calculateInspectorContentHeight(optBase, 200);
        assertTrue(baseHeight > 0, "Altura base deve ser positiva");

        // Adiciona dica pratica -> altura deve aumentar em pelo menos 24px
        RotaConfigOption optWithTip = RotaConfigOption.booleanToggle(
                Component.literal("Opcao com Dica"),
                "Descricao simples e concisa",
                "Exemplo ou explicacao pratica recomendada",
                true,
                () -> true,
                v -> {}
        );
        int tipHeight = screen.calculateInspectorContentHeight(optWithTip, 200);
        assertTrue(tipHeight >= baseHeight + 24, "Opcao com dica deve ter altura superior a base");

        // Opcao com textura grafica anexada
        ResourceLocation previewLoc = ResourceLocation.fromNamespaceAndPath("rota_map", "textures/gui/preview/test.png");
        RotaConfigOption optWithTexture = RotaConfigOption.action(
                Component.literal("Opcao com Textura"),
                "Descricao",
                null,
                "Acao",
                () -> {}
        ).withTexturePreview(previewLoc, 128, 64, Component.literal("Legenda"));

        int textureHeight = screen.calculateInspectorContentHeight(optWithTexture, 200);
        assertTrue(textureHeight > baseHeight, "Opcao com card de textura deve ter altura superior a base");

        // Opcao com galeria de icones
        RotaConfigOption optWithIcons = RotaConfigOption.action(
                Component.literal("Opcao com Icones"),
                "Descricao",
                null,
                "Acao",
                () -> {}
        ).withIconPreviews(List.of(
                RotaIconPreview.of(previewLoc, "Icone 1"),
                RotaIconPreview.of(previewLoc, "Icone 2"),
                RotaIconPreview.of(previewLoc, "Icone 3")
        ));

        int iconsHeight = screen.calculateInspectorContentHeight(optWithIcons, 200);
        assertTrue(iconsHeight >= baseHeight + 25, "Opcao com galeria de icones deve incluir gridHeight + 25");
    }
}
