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

    public void setFocusedOption(RotaConfigOption option) {
        if (option != null && option.getType() != RotaConfigOption.Type.HEADER) {
            this.focusedOption = option;
        }
    }

    @Override
    protected void init() {
        int listWidth = (int) (this.width * 0.67);
        int topY = 36;
        int bottomHeight = 36;
        int listHeight = this.height - topY - bottomHeight;

        // 1. Lista de Controles à Esquerda
        this.listWidget = new RotaConfigListWidget(this.minecraft, listWidth, listHeight, topY, 28, this);
        List<RotaConfigOption> options = this.category.getOptions();
        this.listWidget.setOptions(options);

        if (!options.isEmpty()) {
            for (RotaConfigOption opt : options) {
                if (opt.getType() != RotaConfigOption.Type.HEADER) {
                    this.focusedOption = opt;
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
        int inspectorWidth = this.width - inspectorX - 12;

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fundo padrão
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título da Categoria alinhado à direita do botão Voltar (sem sobreposição)
        int titleX = 105;
        int listWidth = (int) (this.width * 0.65);
        int maxTitleWidth = listWidth - titleX - 8;
        String titleStr = this.category.getTitle().getString();
        if (this.font.width(titleStr) > maxTitleWidth) {
            titleStr = this.font.plainSubstrByWidth(titleStr, maxTitleWidth - 8) + "...";
        }
        graphics.drawString(this.font, titleStr, titleX, 14, 0xFFFFAA00, false);

        // Divisória Vertical entre a Lista e o Painel Inspector
        int topY = 36;
        int bottomY = this.height - 36;
        graphics.fill(listWidth + 2, topY, listWidth + 3, bottomY, 0xFF444444);

        // Renderiza o Painel Inspector à Direita (~35% da Tela)
        renderInspectorPanel(graphics, listWidth + 8, topY, this.width - listWidth - 18, bottomY - topY, mouseX, mouseY);
    }

    private void renderInspectorPanel(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        // Fundo escuro com borda suave
        graphics.fill(x, y, x + width, y + height, 0xCC111111);
        graphics.renderOutline(x, y, width, height, 0xFF555555);

        int innerX = x + 12;
        int innerY = y + 10;
        int innerWidth = width - 24;

        if (focusedOption == null) {
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

        // Título da Opção em Destaque (com quebra se for longo)
        List<FormattedCharSequence> titleLines = this.font.split(
                Component.literal("§6§l" + focusedOption.getTitle().getString()),
                innerWidth
        );
        int curY = innerY;
        for (FormattedCharSequence tLine : titleLines) {
            graphics.drawString(this.font, tLine, innerX, curY, 0xFFFFAA00, false);
            curY += 12;
        }
        graphics.fill(innerX, curY + 2, innerX + innerWidth, curY + 3, 0xFF555555);

        // Aviso de Bloqueio por Servidor se o jogador não puder editar
        boolean canEdit = focusedOption.canPlayerEdit(this.minecraft);
        int textY = curY + 8;
        if (!canEdit) {
            graphics.fill(innerX - 2, textY - 2, innerX + innerWidth + 2, textY + 14, 0x44CC2222);
            graphics.renderOutline(innerX - 2, textY - 2, innerWidth + 4, 16, 0xFFAA3333);
            graphics.drawString(this.font, "§c[Bloqueado pelo Servidor - Requer Admin]", innerX + 2, textY + 2, 0xFFFF5555, false);
            textY += 20;
        }

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

        // Bloco de Dicas / Exemplos Práticos
        if (focusedOption.getExampleTip() != null && !focusedOption.getExampleTip().isEmpty()) {
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

        // Valor Padrão (com quebra para não vazar da caixa)
        if (focusedOption.getDefaultValueText() != null && !focusedOption.getDefaultValueText().isEmpty()) {
            List<FormattedCharSequence> defaultLines = this.font.split(
                    Component.literal("§7Padrão recomendado: §a" + focusedOption.getDefaultValueText()),
                    innerWidth
            );
            for (FormattedCharSequence line : defaultLines) {
                graphics.drawString(this.font, line, innerX, textY, 0xFFAAAAAA, false);
                textY += 11;
            }
        }

        // Galeria de Pre-visualizacao de Icones (quando a opcao fornece previews)
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
            int maxY = y + height - 8;

            com.rotacore.config.RotaIconPreview hoveredPreview = null;

            for (int i = 0; i < previews.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int slotX = innerX + col * (slotSize + gap);
                int slotY = textY + row * (slotSize + gap);

                if (slotY + slotSize > maxY) {
                    break;
                }

                boolean isHovered = mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize;

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

            if (hoveredPreview != null && hoveredPreview.label() != null && !hoveredPreview.label().getString().isEmpty()) {
                graphics.renderTooltip(this.font, hoveredPreview.label(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        saveAndClose();
    }
}
