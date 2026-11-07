package com.rotacore.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RotaModuleCategoriesScreenTest {

    @Test
    public void testGridStartYCalculation() {
        // Altura padrão de tela 300px
        int startY300 = RotaModuleCategoriesScreen.calculateGridStartY(300);
        // (300 - 34 - (3 * 22 + 2 * 6)) / 2 - 10 = (266 - 78) / 2 - 10 = 188 / 2 - 10 = 94 - 10 = 84
        assertEquals(84, startY300);

        // Altura baixa de tela (ex: 120px) -> clamp em 46
        int startYLow = RotaModuleCategoriesScreen.calculateGridStartY(120);
        assertEquals(46, startYLow);
    }

    @Test
    public void testDescriptionBoxDoesNotCollideWithGridOrDoneButton() {
        int screenHeight = 300;
        int boxHeight = 28;
        int numCategories = 6;

        int startY = RotaModuleCategoriesScreen.calculateGridStartY(screenHeight);
        int numRows = (numCategories + 1) / 2;
        int gridBottom = startY + numRows * RotaModuleCategoriesScreen.BTN_HEIGHT + (numRows - 1) * RotaModuleCategoriesScreen.GAP_Y;
        int doneY = screenHeight - 34;

        int boxY = RotaModuleCategoriesScreen.calculateDescriptionBoxY(screenHeight, boxHeight, numCategories);

        // A caixa de descrição deve estar estritamente abaixo da grade de botões
        assertTrue(boxY >= gridBottom + 4, "A caixa de descricao deve ficar abaixo da grade de botoes");

        // A caixa de descrição deve terminar acima do botão Concluído
        assertTrue(boxY + boxHeight <= doneY, "A caixa de descricao deve terminar acima do botao Concluido");
    }

    @Test
    public void testGridDimensionsFor6Categories() {
        int totalWidth = (RotaModuleCategoriesScreen.BTN_WIDTH * 2) + RotaModuleCategoriesScreen.GAP_X;
        assertEquals(434, totalWidth);

        int totalHeight3Rows = 3 * RotaModuleCategoriesScreen.BTN_HEIGHT + 2 * RotaModuleCategoriesScreen.GAP_Y;
        assertEquals(78, totalHeight3Rows);
    }
}
