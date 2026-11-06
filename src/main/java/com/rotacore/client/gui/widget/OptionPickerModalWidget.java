package com.rotacore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Seletor Modal Universal de Opções do Rota Core.
 * Renderiza uma janela modal Screen sobreposta com painel central vanilla de 220x200px,
 * borda cinza #C6C6C6, campo de busca em tempo real, lista rolável com ícones e rótulos,
 * e suporte a seleção direta por clique.
 */
public class OptionPickerModalWidget extends Screen {

    public static final int PANEL_WIDTH = 220;
    public static final int PANEL_HEIGHT = 200;
    public static final int ITEM_ROW_HEIGHT = 20;

    public record PickerOption(String id, Component label, Supplier<ItemStack> iconSupplier, Object customData) {
        public PickerOption(String id, Component label) {
            this(id, label, () -> ItemStack.EMPTY, null);
        }

        public PickerOption(String id, Component label, Supplier<ItemStack> iconSupplier) {
            this(id, label, iconSupplier, null);
        }

        public PickerOption(String id, Component label, ItemStack staticStack) {
            this(id, label, () -> staticStack != null ? staticStack : ItemStack.EMPTY, null);
        }

        public PickerOption(String id, Component label, ItemStack staticStack, Object customData) {
            this(id, label, () -> staticStack != null ? staticStack : ItemStack.EMPTY, customData);
        }
    }

    private final Screen parentScreen;
    private final Consumer<PickerOption> onSelect;
    private final List<PickerOption> allOptions = new ArrayList<>();
    private final List<PickerOption> filteredOptions = new ArrayList<>();

    private EditBox searchBox;
    private RotaButton cancelButton;

    private double scrollAmount = 0.0;

    public OptionPickerModalWidget(Screen parentScreen, Component title, List<PickerOption> options, Consumer<PickerOption> onSelect) {
        super(title != null ? title : Component.literal("Selecionar Opção"));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;
        if (options != null) {
            this.allOptions.addAll(options);
        }
        filterOptions("");
    }

    public static void open(Screen parent, Component title, List<PickerOption> options, Consumer<PickerOption> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new OptionPickerModalWidget(parent, title, options, onSelect));
        }
    }

    public void filterOptions(String query) {
        this.filteredOptions.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";

        if (q.isEmpty()) {
            this.filteredOptions.addAll(this.allOptions);
        } else {
            for (PickerOption opt : this.allOptions) {
                String labelStr = opt.label().getString().toLowerCase(Locale.ROOT);
                String idStr = opt.id().toLowerCase(Locale.ROOT);
                if (labelStr.contains(q) || idStr.contains(q)) {
                    this.filteredOptions.add(opt);
                }
            }
        }
        this.scrollAmount = 0.0;
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // 1. Campo de busca no topo
        this.searchBox = new EditBox(this.font, panelX + 10, panelY + 22, PANEL_WIDTH - 20, 18, Component.literal("Buscar..."));
        this.searchBox.setHint(Component.literal("§8Buscar opção..."));
        this.searchBox.setResponder(this::filterOptions);
        this.searchBox.setFocused(true);
        this.addRenderableWidget(this.searchBox);

        // 2. Botão Cancelar no rodapé
        int btnW = 80;
        int btnY = panelY + 172;
        this.cancelButton = new RotaButton(panelX + (PANEL_WIDTH - btnW) / 2, btnY, btnW, 20, Component.literal("Cancelar"), btn -> close());
        this.addRenderableWidget(this.cancelButton);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // NO-OP absoluto: impede que o Minecraft aplique shaders de blur ou sujeira sobre os painéis
    }

    @Override
    protected void renderMenuBackground(GuiGraphics g) {
        // NO-OP absoluto
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Renderiza a tela anterior em segundo plano de forma nítida
        if (this.parentScreen != null) {
            this.parentScreen.render(graphics, -1, -1, partialTick);
        }

        // 2. Escurecimento translúcido uniforme
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // 3. Painel central vanilla (220x200px) com contornos refinados
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF222222);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.renderOutline(panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 0xFF3A3A3A);

        // 4. Título da Janela Modal
        graphics.drawCenteredString(this.font, this.title, panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFAA00);

        // 5. Renderiza widgets filhos (SearchBox e CancelButton)
        super.render(graphics, mouseX, mouseY, partialTick);

        // 6. Lista Rolável de Opções
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = PANEL_WIDTH - 20;
        int listHeight = 124;

        // Moldura sutil da área de listagem
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF181818);
        graphics.renderOutline(listX, listY, listWidth, listHeight, 0xFF373737);

        int totalContentHeight = filteredOptions.size() * ITEM_ROW_HEIGHT;
        double maxScroll = Math.max(0.0, totalContentHeight - listHeight);
        this.scrollAmount = Math.max(0.0, Math.min(maxScroll, this.scrollAmount));

        graphics.enableScissor(listX + 1, listY + 1, listX + listWidth - 1, listY + listHeight - 1);

        int startY = listY + 2 - (int) this.scrollAmount;

        for (int i = 0; i < filteredOptions.size(); i++) {
            PickerOption opt = filteredOptions.get(i);
            int rowY = startY + i * ITEM_ROW_HEIGHT;

            // Pula renderização de linhas fora do campo visível
            if (rowY + ITEM_ROW_HEIGHT < listY || rowY > listY + listHeight) {
                continue;
            }

            boolean isHovered = mouseX >= listX && mouseX <= listX + listWidth
                    && mouseY >= rowY && mouseY < rowY + ITEM_ROW_HEIGHT
                    && mouseY >= listY && mouseY <= listY + listHeight;

            if (isHovered) {
                graphics.fill(listX + 2, rowY, listX + listWidth - 2, rowY + ITEM_ROW_HEIGHT, 0x33FFAA00);
            }

            ItemStack iconStack = (opt.iconSupplier() != null) ? opt.iconSupplier().get() : ItemStack.EMPTY;
            boolean hasIcon = iconStack != null && !iconStack.isEmpty();

            int textLeft = listX + 6;
            if (hasIcon) {
                int iconX = listX + 4;
                int iconY = rowY + 1;
                graphics.fill(iconX, iconY, iconX + 18, iconY + 18, 0xFF2A2A2A);
                graphics.renderOutline(iconX, iconY, 18, 18, 0xFF444444);
                graphics.renderFakeItem(iconStack, iconX + 1, iconY + 1);
                textLeft = iconX + 22;
            }

            int maxTextW = listWidth - (textLeft - listX) - 6;
            String labelStr = opt.label().getString();
            if (this.font.width(labelStr) > maxTextW) {
                labelStr = this.font.plainSubstrByWidth(labelStr, maxTextW - 6) + "...";
            }

            int textColor = isHovered ? 0xFFFFFFFF : 0xFFDDDDDD;
            int textY = rowY + (ITEM_ROW_HEIGHT - 9) / 2;
            graphics.drawString(this.font, labelStr, textLeft, textY, textColor, false);
        }

        graphics.disableScissor();

        // Barra de rolagem visual
        if (maxScroll > 0) {
            int scrollbarX = listX + listWidth - 4;
            int thumbH = Math.max(12, (int) ((float) listHeight / totalContentHeight * listHeight));
            int thumbY = listY + (int) ((float) this.scrollAmount / maxScroll * (listHeight - thumbH));
            graphics.fill(scrollbarX, listY, scrollbarX + 3, listY + listHeight, 0x44000000);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbH, 0xFFAAAAAA);
        }

        // Mensagem caso nenhuma opção coincida com o filtro
        if (filteredOptions.isEmpty()) {
            graphics.drawCenteredString(this.font, "§7Nenhuma opção encontrada.", listX + listWidth / 2, listY + listHeight / 2 - 4, 0xFF888888);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = PANEL_WIDTH - 20;
        int listHeight = 124;

        if (button == 0 && mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
            int relativeY = (int) (mouseY - listY + this.scrollAmount - 2);
            int clickedIndex = relativeY / ITEM_ROW_HEIGHT;
            if (clickedIndex >= 0 && clickedIndex < filteredOptions.size()) {
                selectOption(filteredOptions.get(clickedIndex));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = PANEL_WIDTH - 20;
        int listHeight = 124;

        if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
            int totalContentHeight = filteredOptions.size() * ITEM_ROW_HEIGHT;
            double maxScroll = Math.max(0.0, totalContentHeight - listHeight);
            if (maxScroll > 0) {
                this.scrollAmount = Math.max(0.0, Math.min(maxScroll, this.scrollAmount - scrollY * 16.0));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void selectOption(PickerOption option) {
        if (this.onSelect != null && option != null) {
            this.onSelect.accept(option);
        }
        close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<PickerOption> getAllOptions() {
        return allOptions;
    }

    public List<PickerOption> getFilteredOptions() {
        return filteredOptions;
    }
}
