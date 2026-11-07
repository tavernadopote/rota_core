package com.rotacore.client.gui;

import com.rotacore.api.RotaCategory;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaConfigListWidget;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Tela de configuração dividida em duas colunas:
 * Lado esquerdo (~67%): Lista rolável de controles com sliders, toggles e ações.
 * Lado direito (~33%): Painel fixo (Inspector) com legenda detalhada, dicas e busca.
 */
public class RotaCategoryScreen extends Screen {

    private final Screen parentHub;
    private final RotaCategory category;
    private final Runnable onSaveAction;

    private RotaConfigListWidget listWidget;
    private EditBox searchBox;
    private RotaConfigOption focusedOption;
    private double inspectorScrollAmount = 0.0;

    public RotaCategoryScreen(Screen parentHub, RotaCategory category) {
        this(parentHub, category, null);
    }

    public RotaCategoryScreen(Screen parentHub, RotaCategory category, Runnable onSaveAction) {
        super(category.getTitle());
        this.parentHub = parentHub;
        this.category = category;
        this.onSaveAction = onSaveAction;
    }

    public Screen getParentHub() {
        return parentHub;
    }

    public RotaConfigOption getFocusedOption() {
        return this.focusedOption;
    }

    public double getInspectorScrollAmount() {
        return this.inspectorScrollAmount;
    }

    public void setInspectorScrollAmount(double scrollAmount) {
        this.inspectorScrollAmount = Math.max(0.0, scrollAmount);
    }

    public void setFocusedOption(RotaConfigOption option) {
        if (option != null && option.getType() != RotaConfigOption.Type.HEADER) {
            if (this.focusedOption != option) {
                this.inspectorScrollAmount = 0.0;
            }
            this.focusedOption = option;
        }
    }

    public static double calculateMaxScroll(int totalContentHeight, int visibleHeight) {
        return Math.max(0.0, totalContentHeight - visibleHeight);
    }

    public static double clampScroll(double scrollAmount, double maxScroll) {
        return Math.max(0.0, Math.min(maxScroll, scrollAmount));
    }

    public static double applyScrollDelta(double currentScroll, double scrollY, double maxScroll) {
        return Math.max(0.0, Math.min(maxScroll, currentScroll - scrollY * 16.0));
    }

    public static boolean shouldRenderTip(RotaConfigOption option) {
        if (option == null || !option.hasTip()) {
            return false;
        }
        // Prioriza o espaco vertical para que cards de pre-visualizacao de imagem caibam perfeitamente sem scroll
        if (option.hasTexturePreview() && !option.isTipExplicitlyForced()) {
            return false;
        }
        return true;
    }

    public static boolean shouldRenderDefaultValue(RotaConfigOption option) {
        if (option == null) {
            return false;
        }
        if (option.hasTexturePreview()) {
            return false;
        }
        if (!option.shouldShowDefaultValue()) {
            return false;
        }
        String defaultVal = option.getDefaultValueText();
        return defaultVal != null && !defaultVal.trim().isEmpty() && !defaultVal.equals("—");
    }

    @Override
    protected void init() {
        int listWidth = (int) (this.width * 0.65);
        int topY = 36;
        int bottomHeight = 36;
        int listHeight = this.height - topY - bottomHeight;

        // 1. Lista de Controles à Esquerda
        double savedScroll = (this.listWidget != null) ? this.listWidget.getScrollAmount() : 0.0;
        this.listWidget = new RotaConfigListWidget(this.minecraft, listWidth, listHeight, topY, 28, this);
        List<RotaConfigOption> options = this.category.getOptions();
        this.listWidget.setOptions(options);
        if (savedScroll > 0) {
            this.listWidget.setScrollAmount(savedScroll);
        }

        if (!options.isEmpty()) {
            for (RotaConfigOption opt : options) {
                if (opt.getType() != RotaConfigOption.Type.HEADER) {
                    this.focusedOption = opt;
                    this.inspectorScrollAmount = 0.0;
                    break;
                }
            }
        }
        this.addRenderableWidget(this.listWidget);

        // 2. Botão Voltar no Canto Superior Esquerdo
        this.addRenderableWidget(
                new RotaButton(12, 8, 85, 20, Component.literal("← Voltar"), btn -> {
                    saveAndClose();
                })
        );

        // 3. Botão Restaurar Categoria no Rodapé Esquerdo
        this.addRenderableWidget(
                new RotaButton(12, this.height - 28, 130, 20, Component.literal("Restaurar Padrões"), btn -> {
                    this.category.resetDefaults();
                    this.listWidget.setOptions(this.category.getOptions());
                })
        );

        // 4. Botão Salvar no Rodapé Central
        this.addRenderableWidget(
                new RotaButton(150, this.height - 28, 120, 20, Component.literal("Salvar Alterações"), btn -> {
                    saveAndClose();
                })
        );

        // 5. Caixa de Busca no Rodapé Direito (Abaixo do Painel Inspector)
        int inspectorX = listWidth + 8;
        int inspectorWidth = this.width - inspectorX - 10;

        this.searchBox = new EditBox(this.font, inspectorX, this.height - 28, inspectorWidth, 20, Component.literal("Buscar..."));
        this.searchBox.setHint(Component.literal("§8Buscar opção..."));
        this.searchBox.setResponder(query -> this.listWidget.setFilter(query));
        this.addRenderableWidget(this.searchBox);
    }

    private void saveAndClose() {
        if (onSaveAction != null) {
            onSaveAction.run();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentHub);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.renderTransparentBackground(graphics);
        } else {
            this.renderMenuBackground(graphics);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int listWidth = (int) (this.width * 0.65);
        int topY = 36;
        int bottomY = this.height - 36;

        // Barra superior sólida e barra inferior sólida com divisórias horizontais
        graphics.fill(0, 0, this.width, topY, 0xF0101010);
        graphics.fill(0, bottomY, this.width, this.height, 0xF0101010);
        graphics.fill(0, topY - 1, this.width, topY, 0xFF333333);
        graphics.fill(0, bottomY, this.width, bottomY + 1, 0xFF333333);

        // Painel esquerdo da lista: preenchimento escuro limpo impedindo vazamento de fundo
        graphics.fill(8, topY, listWidth, bottomY, 0xEE141414);
        graphics.renderOutline(8, topY, listWidth - 8, bottomY - topY, 0xFF2A2A2A);

        // Fundo padrão e renderização dos widgets (botões, listWidget, searchBox)
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título da Categoria alinhado à direita do botão Voltar (sem sobreposição)
        int titleX = 105;
        int maxTitleWidth = listWidth - titleX - 8;
        String titleStr = this.category.getTitle().getString();
        if (this.font.width(titleStr) > maxTitleWidth) {
            titleStr = this.font.plainSubstrByWidth(titleStr, maxTitleWidth - 8) + "...";
        }
        graphics.drawString(this.font, titleStr, titleX, 14, 0xFFFFAA00, false);

        // Divisória Vertical entre a Lista e o Painel Inspector
        graphics.fill(listWidth + 2, topY, listWidth + 3, bottomY, 0xFF444444);

        // Renderiza o Painel Inspector à Direita (~35% da Tela)
        renderInspectorPanel(graphics, listWidth + 8, topY, this.width - listWidth - 18, bottomY - topY, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int listWidth = (int) (this.width * 0.65);
        int inspectorX = listWidth + 8;
        int topY = 36;
        int bottomY = this.height - 36;
        int inspectorWidth = this.width - inspectorX - 10;
        int inspectorHeight = bottomY - topY;

        if (mouseX >= inspectorX && mouseX <= inspectorX + inspectorWidth && mouseY >= topY && mouseY <= bottomY) {
            if (this.focusedOption != null) {
                int innerWidth = inspectorWidth - 24;
                int totalContentHeight = calculateInspectorContentHeight(this.focusedOption, innerWidth);
                int visibleHeight = inspectorHeight - 20;
                double maxScroll = calculateMaxScroll(totalContentHeight, visibleHeight);
                if (maxScroll > 0) {
                    this.inspectorScrollAmount = applyScrollDelta(this.inspectorScrollAmount, scrollY, maxScroll);
                    return true;
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public int calculateInspectorContentHeight(RotaConfigOption option, int innerWidth) {
        if (option == null) {
            return 0;
        }
        int total = 0;

        boolean hasCustom = option.hasCustomInspectorRenderer();

        if (!hasCustom) {
            // Título e espaçamento
            int titleLinesCount = (this.font != null)
                    ? this.font.split(Component.literal("§6§l" + option.getTitle().getString()), innerWidth).size()
                    : 1;
            total += titleLinesCount * 12 + 10;
        }

        // Bloqueio de servidor (se houver)
        boolean canEdit = option.canPlayerEdit(this.minecraft);
        if (!canEdit) {
            total += 20;
        }

        if (!hasCustom) {
            // Descrição
            String desc = option.getDescription();
            if (desc != null && !desc.isEmpty()) {
                int descLinesCount = (this.font != null)
                        ? this.font.split(Component.literal("§f" + desc), innerWidth).size()
                        : Math.max(1, (int) Math.ceil((double) desc.length() * 6 / Math.max(1, innerWidth)));
                total += descLinesCount * 11 + 8;
            }

            // Dicas / Exemplos (omitido por padrao quando redundante ou em opcoes com preview)
            if (shouldRenderTip(option)) {
                String tip = option.getExampleTip();
                int tipLinesCount = (this.font != null)
                        ? this.font.split(Component.literal("§b" + tip), innerWidth).size()
                        : Math.max(1, (int) Math.ceil((double) tip.length() * 6 / Math.max(1, innerWidth)));
                total += tipLinesCount * 11 + 24;
            }

            // Padrao recomendado (omitido por padrao quando a opcao ja exibe seu estado nativamente)
            if (shouldRenderDefaultValue(option)) {
                String defaultVal = option.getDefaultValueText();
                String fullDefault = "§7Padrão recomendado: §a" + defaultVal;
                int defLinesCount = (this.font != null)
                        ? this.font.split(Component.literal(fullDefault), innerWidth).size()
                        : 1;
                total += defLinesCount * 11 + 8;
            }
        }

        // Card de textura
        if (option.hasTexturePreview()) {
            int pad = 4;
            int optW = option.getTexturePreviewWidth();
            int optH = option.getTexturePreviewHeight();
            int maxPreviewW = Math.max(16, innerWidth - pad * 2);
            int previewH = optH;
            if (optW > maxPreviewW) {
                float scale = (float) maxPreviewW / (float) optW;
                previewH = Math.max(16, (int) (optH * scale));
            }
            boolean hasCaption = option.getTexturePreviewCaption() != null;
            int fontHeight = (this.font != null) ? this.font.lineHeight : 9;
            int captionSpacing = hasCaption ? (4 + fontHeight) : 0;
            int cardH = pad + previewH + pad + captionSpacing;
            total += cardH + 16;
        }

        // Galeria de ícones
        if (option.hasIconPreviews()) {
            int slotSize = 22;
            int gap = 3;
            int cols = Math.max(1, (innerWidth + gap) / (slotSize + gap));
            int count = option.getIconPreviews().size();
            int rows = (count + cols - 1) / cols;
            int gridHeight = rows * (slotSize + gap);
            total += gridHeight + 25;
        }

        // Inspector Customizado Rico (Visualizador 3D, Atributos, etc.)
        if (hasCustom) {
            total += option.getCustomInspectorHeight() + 16;
        }

        return total;
    }

    private void renderInspectorPanel(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
        // Fundo escuro com borda suave na caixa completa (opaco)
        graphics.fill(x, y, x + width, y + height, 0xFF181818);
        graphics.renderOutline(x, y, width, height, 0xFF555555);

        int innerX = x + 12;
        int innerWidth = width - 24;

        if (focusedOption == null) {
            int innerY = y + 10;
            graphics.drawString(this.font, "§6Ajuda & Descrição", innerX, innerY, 0xFFFFAA00, false);
            graphics.fill(innerX, innerY + 12, innerX + innerWidth, innerY + 13, 0xFF444444);
            List<FormattedCharSequence> lines = this.font.split(
                    Component.literal("§7Passe o mouse ou clique sobre qualquer configuração à esquerda para ver a explicação detalhada, valores padrão e dicas de funcionamento."),
                    innerWidth
            );
            int textY = innerY + 20;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(this.font, line, innerX, textY, 0xFFCCCCCC, false);
                textY += 11;
            }
            return;
        }

        int totalContentHeight = calculateInspectorContentHeight(focusedOption, innerWidth);
        int visibleHeight = height - 20;
        double maxScroll = calculateMaxScroll(totalContentHeight, visibleHeight);
        this.inspectorScrollAmount = clampScroll(this.inspectorScrollAmount, maxScroll);

        // Ativa o Scissor para confinar estritamente o conteúdo dentro do painel
        graphics.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);

        int curY = y + 10 - (int) this.inspectorScrollAmount;
        boolean hasCustom = focusedOption.hasCustomInspectorRenderer();

        if (!hasCustom) {
            // Título da Opção em Destaque (com quebra se for longo)
            List<FormattedCharSequence> titleLines = this.font.split(
                    Component.literal("§6§l" + focusedOption.getTitle().getString()),
                    innerWidth
            );
            for (FormattedCharSequence tLine : titleLines) {
                graphics.drawString(this.font, tLine, innerX, curY, 0xFFFFAA00, false);
                curY += 12;
            }
            graphics.fill(innerX, curY + 2, innerX + innerWidth, curY + 3, 0xFF555555);
            curY += 8;
        }

        // Aviso de Bloqueio por Servidor se o jogador não puder editar
        boolean canEdit = focusedOption.canPlayerEdit(this.minecraft);
        int textY = curY;
        if (!canEdit) {
            graphics.fill(innerX - 2, textY - 2, innerX + innerWidth + 2, textY + 14, 0x44CC2222);
            graphics.renderOutline(innerX - 2, textY - 2, innerWidth + 4, 16, 0xFFAA3333);
            graphics.drawString(this.font, "§c[Bloqueado pelo Servidor - Requer Admin]", innerX + 2, textY + 2, 0xFFFF5555, false);
            textY += 20;
        }

        if (!hasCustom) {
            // Descrição Detalhada com Quebra Automática de Linha
            String desc = focusedOption.getDescription();
            if (desc != null && !desc.isEmpty()) {
                List<FormattedCharSequence> descLines = this.font.split(
                        Component.literal("§f" + desc),
                        innerWidth
                );
                for (FormattedCharSequence line : descLines) {
                    graphics.drawString(this.font, line, innerX, textY, 0xFFEEEEEE, false);
                    textY += 11;
                }
            }

            textY += 8;

            // Bloco de Dicas / Exemplos Praticos (renderizado apenas se houver dica customizada relevante)
            if (shouldRenderTip(focusedOption)) {
                graphics.fill(innerX - 4, textY - 2, innerX + innerWidth + 4, textY + 1, 0xFF333333);
                textY += 4;
                graphics.drawString(this.font, "§e§lDica / Exemplo:", innerX, textY, 0xFFFFAA00, false);
                textY += 12;

                List<FormattedCharSequence> tipLines = this.font.split(
                        Component.literal("§b" + focusedOption.getExampleTip()),
                        innerWidth
                );
                for (FormattedCharSequence line : tipLines) {
                    graphics.drawString(this.font, line, innerX, textY, 0xFF88DDFF, false);
                    textY += 11;
                }
                textY += 8;
            }

            // Valor Padrao (apenas quando expressamente solicitado)
            if (shouldRenderDefaultValue(focusedOption)) {
                List<FormattedCharSequence> defaultLines = this.font.split(
                        Component.literal("§7Padrão recomendado: §a" + focusedOption.getDefaultValueText()),
                        innerWidth
                );
                for (FormattedCharSequence line : defaultLines) {
                    graphics.drawString(this.font, line, innerX, textY, 0xFFAAAAAA, false);
                    textY += 11;
                }
                textY += 8;
            }
        }

        // Inspector Customizado Rico (ex: Visualizador 3D, Graficos, Atributos de Criaturas, etc.)
        if (hasCustom) {
            int customH = focusedOption.getCustomInspectorHeight();
            try {
                focusedOption.getCustomInspectorRenderer().render(graphics, innerX, textY, innerWidth, customH, partialTick);
            } catch (Throwable ignored) {
            }
            textY += customH + 8;
        }

        // Card de Pré-visualização de Textura do Mapa (ex: relevo hipsométrico de temas)
        if (focusedOption.hasTexturePreview()) {
            textY += 6;
            graphics.fill(innerX, textY, innerX + innerWidth, textY + 1, 0xFF444444);
            textY += 8;

            int pad = 4;
            int optW = focusedOption.getTexturePreviewWidth();
            int optH = focusedOption.getTexturePreviewHeight();

            // Ajusta dimensões da pré-visualização respeitando a largura útil do Inspector
            int maxPreviewW = Math.max(16, innerWidth - pad * 2);
            int previewW = optW;
            int previewH = optH;
            if (previewW > maxPreviewW) {
                float scale = (float) maxPreviewW / (float) previewW;
                previewW = maxPreviewW;
                previewH = Math.max(16, (int) (optH * scale));
            }

            int cardW = previewW + pad * 2;
            int cardX = innerX + Math.max(0, (innerWidth - cardW) / 2);
            int cardY = textY;

            boolean hasCaption = focusedOption.getTexturePreviewCaption() != null;
            int fontHeight = (this.font != null) ? this.font.lineHeight : 9;
            int captionSpacing = hasCaption ? (4 + fontHeight) : 0;
            int cardH = pad + previewH + pad + captionSpacing;

            // Moldura escura e contorno elegante
            graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xFF0A0C10);
            graphics.renderOutline(cardX, cardY, cardW, cardH, 0xFF444444);

            // Renderização da textura gráfica
            try {
                graphics.blit(
                        focusedOption.getTexturePreviewLocation(),
                        cardX + pad,
                        cardY + pad,
                        previewW,
                        previewH,
                        0.0f,
                        0.0f,
                        optW,
                        optH,
                        optW,
                        optH
                );
            } catch (Throwable ignored) {
            }

            // Legenda centralizada abaixo da imagem
            if (hasCaption) {
                int captionY = cardY + pad + previewH + 4;
                graphics.drawCenteredString(
                        this.font,
                        focusedOption.getTexturePreviewCaption(),
                        cardX + cardW / 2,
                        captionY,
                        0xFFAAAAAA
                );
            }

            textY = cardY + cardH + 2;
        }

        // Galeria de Pre-visualizacao de Icones
        com.rotacore.config.RotaIconPreview hoveredPreview = null;
        if (focusedOption.hasIconPreviews()) {
            java.util.List<com.rotacore.config.RotaIconPreview> previews = focusedOption.getIconPreviews();
            textY += 6;
            graphics.fill(innerX, textY, innerX + innerWidth, textY + 1, 0xFF444444);
            textY += 6;

            graphics.drawString(this.font, "§6§lÍcones Incluídos §7(" + previews.size() + "):", innerX, textY, 0xFFFFAA00, false);
            textY += 13;

            int slotSize = 22;
            int gap = 3;
            int cols = Math.max(1, (innerWidth + gap) / (slotSize + gap));

            for (int i = 0; i < previews.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int slotX = innerX + col * (slotSize + gap);
                int slotY = textY + row * (slotSize + gap);

                boolean isHovered = mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize
                        && mouseY >= (y + 1) && mouseY <= (y + height - 1);

                // Fundo e contorno do slot
                graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xEE1E1E1E);
                graphics.renderOutline(slotX, slotY, slotSize, slotSize, isHovered ? 0xFFFFAA00 : 0xFF444444);

                com.rotacore.config.RotaIconPreview prev = previews.get(i);
                int iconOffset = (slotSize - 16) / 2;
                try {
                    graphics.blit(prev.texture(), slotX + iconOffset, slotY + iconOffset, 16, 16, 0.0f, 0.0f, 1, 1, 1, 1);
                } catch (Throwable ignored) {
                }

                if (isHovered) {
                    hoveredPreview = prev;
                }
            }
        }

        // Desativa o Scissor
        graphics.disableScissor();

        // Barra de Rolagem Visual
        if (maxScroll > 0) {
            int barX = x + width - 5;
            int trackH = height - 4;
            int thumbH = Math.max(16, (int) ((float) visibleHeight / (float) totalContentHeight * trackH));
            int thumbY = y + 2 + (int) ((float) this.inspectorScrollAmount / (float) maxScroll * (trackH - thumbH));
            graphics.fill(barX, y + 2, barX + 3, y + 2 + trackH, 0x44000000);
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFFAAAAAA);
        }

        if (hoveredPreview != null && hoveredPreview.label() != null && !hoveredPreview.label().getString().isEmpty()) {
            graphics.renderTooltip(this.font, hoveredPreview.label(), mouseX, mouseY);
        }
    }

    public void refreshOptions() {
        if (this.listWidget != null) {
            double savedScroll = this.listWidget.getScrollAmount();
            List<RotaConfigOption> options = this.category.getOptions();
            this.listWidget.setOptions(options);
            this.listWidget.setScrollAmount(savedScroll);
        }
    }

    public RotaConfigListWidget getListWidget() {
        return this.listWidget;
    }

    @Override
    public void onClose() {
        saveAndClose();
    }
}
