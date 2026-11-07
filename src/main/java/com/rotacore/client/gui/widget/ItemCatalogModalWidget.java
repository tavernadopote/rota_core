package com.rotacore.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Modal universal do Rota Core para seleção visual de itens e blocos do Minecraft.
 * Suporta rolagem contínua por roda do mouse, barra de rolagem lateral interativa,
 * busca em tempo real (por nome ou @mod) e seletor tátil de quantidade opcional.
 */
public class ItemCatalogModalWidget extends Screen {

    public static final int PANEL_WIDTH = 220;
    public static final int COLS = 10;
    public static final int ROWS = 6;
    public static final int VISIBLE_SLOTS = COLS * ROWS; // 60 slots

    private final Screen parentScreen;
    private final Consumer<ItemStack> onSelect;
    private final BiConsumer<ItemStack, Integer> onSelectWithQuantity;
    private final boolean isQuantityMode;

    private final List<ItemStack> allItems = new ArrayList<>();
    private final List<ItemStack> filteredItems = new ArrayList<>();

    private EditBox searchBox;
    private RotaButton cancelButton;
    private RotaButton confirmButton;

    // Controles de quantidade
    private RotaButton btnMinus;
    private RotaButton btnPlus;
    private RotaButton btnQuick1;
    private RotaButton btnQuick16;
    private RotaButton btnQuick32;
    private RotaButton btnQuick64;

    private double scrollOffset = 0.0;
    private boolean isDraggingScrollbar = false;
    private double dragYOffset = 0.0;

    private int selectedQuantity = 1;
    private ItemStack selectedStack = null;

    public ItemCatalogModalWidget(Screen parentScreen, Consumer<ItemStack> onSelect) {
        super(Component.literal("Selecionar Ícone do Catálogo"));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;
        this.onSelectWithQuantity = null;
        this.isQuantityMode = false;
        this.selectedQuantity = 1;

        loadRegisteredItems();
    }

    public ItemCatalogModalWidget(Screen parentScreen, int initialCount, BiConsumer<ItemStack, Integer> onSelectWithQuantity) {
        super(Component.literal("Selecionar Item e Quantidade"));
        this.parentScreen = parentScreen;
        this.onSelect = null;
        this.onSelectWithQuantity = onSelectWithQuantity;
        this.isQuantityMode = true;
        this.selectedQuantity = Math.max(1, initialCount);

        loadRegisteredItems();
    }

    public ItemCatalogModalWidget(Consumer<ItemStack> onSelect) {
        this(Minecraft.getInstance() != null ? Minecraft.getInstance().screen : null, onSelect);
    }

    public static void open(Screen parent, Consumer<ItemStack> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ItemCatalogModalWidget(parent, onSelect));
        }
    }

    public static void openWithQuantity(Screen parent, int initialCount, BiConsumer<ItemStack, Integer> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new ItemCatalogModalWidget(parent, initialCount, onSelect));
        }
    }

    public int getPanelHeight() {
        return isQuantityMode ? 236 : 190;
    }

    public void loadRegisteredItems() {
        this.allItems.clear();
        try {
            if (BuiltInRegistries.ITEM != null) {
                for (Item item : BuiltInRegistries.ITEM) {
                    if (item == null || item == Items.AIR) continue;
                    ItemStack stack = new ItemStack(item);
                    if (!stack.isEmpty()) {
                        this.allItems.add(stack);
                    }
                }
            }
        } catch (Throwable ignored) {
            // Suporte seguro a ambiente de testes headless
        }
        filterItems("");
    }

    public void setItems(List<ItemStack> items) {
        this.allItems.clear();
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null && !item.isEmpty()) {
                    this.allItems.add(item);
                }
            }
        }
        filterItems(searchBox != null ? searchBox.getValue() : "");
    }

    public void filterItems(String query) {
        this.filteredItems.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";

        if (q.isEmpty()) {
            this.filteredItems.addAll(this.allItems);
        } else if (q.startsWith("@")) {
            String modQuery = q.substring(1).trim();
            for (ItemStack stack : this.allItems) {
                try {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (key != null && key.getNamespace().toLowerCase(Locale.ROOT).contains(modQuery)) {
                        this.filteredItems.add(stack);
                    }
                } catch (Throwable ignored) {}
            }
        } else {
            for (ItemStack stack : this.allItems) {
                try {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                    if (name.contains(q) || (key != null && key.getPath().toLowerCase(Locale.ROOT).contains(q))) {
                        this.filteredItems.add(stack);
                    }
                } catch (Throwable ignored) {}
            }
        }

        this.scrollOffset = 0.0;
    }

    public int getTotalRows() {
        return (int) Math.ceil((double) filteredItems.size() / COLS);
    }

    public double getMaxScroll() {
        return Math.max(0.0, (getTotalRows() - ROWS) * 18.0);
    }

    @Override
    protected void init() {
        int panelHeight = getPanelHeight();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // 1. Campo de Busca no topo
        this.searchBox = new EditBox(this.font, panelX + 10, panelY + 22, PANEL_WIDTH - 20, 18, Component.literal("Buscar item..."));
        this.searchBox.setHint(Component.literal("§8Buscar por nome ou @mod..."));
        this.searchBox.setResponder(this::filterItems);
        this.searchBox.setFocused(true);
        this.addRenderableWidget(this.searchBox);

        if (isQuantityMode) {
            // Controles de Quantidade no rodapé
            int ctrlY = panelY + 178;

            // [-] [ N ] [+]
            this.btnMinus = new RotaButton(panelX + 16, ctrlY, 18, 18, Component.literal("-"), btn -> {
                if (selectedQuantity > 1) {
                    selectedQuantity--;
                    updateQuantityState();
                }
            });
            this.btnPlus = new RotaButton(panelX + 70, ctrlY, 18, 18, Component.literal("+"), btn -> {
                int max = (selectedStack != null && !selectedStack.isEmpty()) ? selectedStack.getMaxStackSize() : 64;
                if (selectedQuantity < max) {
                    selectedQuantity++;
                    updateQuantityState();
                }
            });

            // Botões rápidos: [1] [16] [32] [64]
            this.btnQuick1 = new RotaButton(panelX + 94, ctrlY, 22, 18, Component.literal("1"), btn -> {
                selectedQuantity = 1;
                updateQuantityState();
            });
            this.btnQuick16 = new RotaButton(panelX + 119, ctrlY, 26, 18, Component.literal("16"), btn -> {
                selectedQuantity = 16;
                updateQuantityState();
            });
            this.btnQuick32 = new RotaButton(panelX + 148, ctrlY, 26, 18, Component.literal("32"), btn -> {
                selectedQuantity = 32;
                updateQuantityState();
            });
            this.btnQuick64 = new RotaButton(panelX + 177, ctrlY, 26, 18, Component.literal("64"), btn -> {
                selectedQuantity = 64;
                updateQuantityState();
            });

            this.addRenderableWidget(this.btnMinus);
            this.addRenderableWidget(this.btnPlus);
            this.addRenderableWidget(this.btnQuick1);
            this.addRenderableWidget(this.btnQuick16);
            this.addRenderableWidget(this.btnQuick32);
            this.addRenderableWidget(this.btnQuick64);

            // Botões Finais de Ação: Cancelar e Confirmar
            int actionY = panelY + 204;
            this.cancelButton = new RotaButton(panelX + 24, actionY, 80, 20, Component.literal("Cancelar"), btn -> close());
            this.confirmButton = new RotaButton(panelX + 114, actionY, 82, 20, Component.literal("Confirmar"), btn -> confirmSelection());
            this.confirmButton.setAccent(true);
            this.confirmButton.active = (selectedStack != null && !selectedStack.isEmpty());

            this.addRenderableWidget(this.cancelButton);
            this.addRenderableWidget(this.confirmButton);
        } else {
            // Modo simples: Apenas botão Cancelar centralizado no rodapé
            int btnY = panelY + panelHeight - 28;
            this.cancelButton = new RotaButton(panelX + (PANEL_WIDTH - 80) / 2, btnY, 80, 20, Component.literal("Cancelar"), btn -> close());
            this.addRenderableWidget(this.cancelButton);
        }
    }

    private void updateQuantityState() {
        if (selectedStack != null && !selectedStack.isEmpty()) {
            selectedStack.setCount(selectedQuantity);
            if (confirmButton != null) {
                confirmButton.active = true;
            }
        }
    }

    private void confirmSelection() {
        if (onSelectWithQuantity != null && selectedStack != null && !selectedStack.isEmpty()) {
            ItemStack result = selectedStack.copy();
            result.setCount(selectedQuantity);
            onSelectWithQuantity.accept(result, selectedQuantity);
        }
        close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void selectItem(ItemStack stack) {
        if (isQuantityMode) {
            this.selectedStack = (stack != null && !stack.isEmpty()) ? stack.copy() : null;
            if (this.selectedStack != null) {
                int max = this.selectedStack.getMaxStackSize();
                if (this.selectedQuantity > max) {
                    this.selectedQuantity = max;
                }
                this.selectedStack.setCount(this.selectedQuantity);
            }
            if (confirmButton != null) {
                confirmButton.active = (this.selectedStack != null);
            }
        } else {
            if (onSelect != null && stack != null && !stack.isEmpty()) {
                onSelect.accept(stack.copy());
            }
            close();
        }
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
        // 1. Renderiza a tela de trás estática se disponível
        if (this.parentScreen != null) {
            this.parentScreen.render(graphics, -1, -1, partialTick);
        }

        // Elevação Z=400: isola a janela modal para que nenhum componente da tela de fundo sobreponha o catálogo
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);

        // 2. Fundo escurecido semi-transparente cobrindo a tela inteira
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        int panelHeight = getPanelHeight();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // 3. Painel central vanilla com borda cinza #C6C6C6
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, 0xFF222222);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, panelHeight, 0xFFC6C6C6);
        graphics.renderOutline(panelX + 1, panelY + 1, PANEL_WIDTH - 2, panelHeight - 2, 0xFF3A3A3A);

        // 4. Título da Janela Modal
        Component titleComp = isQuantityMode ? Component.literal("Selecionar Item e Quantidade") : Component.literal("Selecionar Ícone do Catálogo");
        graphics.drawCenteredString(this.font, titleComp, panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFAA00);

        // 5. Renderizar widgets padrão (EditBox e botões)
        super.render(graphics, mouseX, mouseY, partialTick);

        // 6. Grade de Slots 18x18px (10 colunas x 6 linhas visíveis = 180x108px)
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = COLS * 18;
        int gridHeight = ROWS * 18;

        double maxScroll = getMaxScroll();
        this.scrollOffset = Math.max(0.0, Math.min(maxScroll, this.scrollOffset));

        int totalRows = getTotalRows();
        int startRow = (int) (this.scrollOffset / 18.0);
        int endRow = Math.min(totalRows, startRow + ROWS + (this.scrollOffset % 18.0 > 0 ? 1 : 0));

        ItemStack hoveredStack = null;

        // Scissor para confinar a rolagem vertical de itens estritamente dentro da grade
        graphics.enableScissor(gridX, gridY, gridX + gridWidth, gridY + gridHeight);

        for (int r = startRow; r < endRow; r++) {
            int slotY = gridY + (r * 18) - (int) this.scrollOffset;
            for (int c = 0; c < COLS; c++) {
                int slotX = gridX + c * 18;

                // Slot escuro vanilla 18x18px com contorno
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
                graphics.renderOutline(slotX, slotY, 18, 18, 0xFF555555);

                int itemIndex = r * COLS + c;
                if (itemIndex < filteredItems.size()) {
                    ItemStack stack = filteredItems.get(itemIndex);
                    boolean isHovered = mouseX >= slotX && mouseX < slotX + 18 &&
                                        mouseY >= slotY && mouseY < slotY + 18 &&
                                        mouseY >= gridY && mouseY < gridY + gridHeight;

                    if (isHovered) {
                        graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x44FFFFFF);
                        hoveredStack = stack;
                    }

                    graphics.renderFakeItem(stack, slotX + 1, slotY + 1);
                }
            }
        }

        graphics.disableScissor();

        // 7. Barra de rolagem vertical vanilla na lateral direita da grade
        int trackX = gridX + gridWidth + 3;
        int trackY = gridY;
        int trackW = 4;
        int trackH = gridHeight;

        graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0x44000000);
        if (maxScroll > 0) {
            int thumbH = Math.max(14, (int) ((float) trackH / (totalRows * 18.0) * trackH));
            int thumbY = trackY + (int) ((this.scrollOffset / maxScroll) * (trackH - thumbH));
            graphics.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, 0xFFAAAAAA);
        }

        // 8. Seletor de Quantidade: Detalhes do item selecionado
        if (isQuantityMode) {
            int detailY = panelY + 156;
            int slotBoxX = panelX + 16;

            graphics.fill(slotBoxX, detailY, slotBoxX + 18, detailY + 18, 0xFF2A2A2A);
            graphics.renderOutline(slotBoxX, detailY, 18, 18, 0xFF555555);

            if (selectedStack != null && !selectedStack.isEmpty()) {
                graphics.renderFakeItem(selectedStack, slotBoxX + 1, detailY + 1);
                String itemName = selectedStack.getHoverName().getString();
                int maxNameWidth = PANEL_WIDTH - 58;
                if (this.font.width(itemName) > maxNameWidth) {
                    itemName = this.font.plainSubstrByWidth(itemName, maxNameWidth - 6) + "...";
                }
                graphics.drawString(this.font, itemName, panelX + 38, detailY + 5, 0xFFFFAA00, false);
            } else {
                graphics.drawString(this.font, "§7Clique em um item da grade...", panelX + 38, detailY + 5, 0xFF888888, false);
            }

            // Caixa central de exibição da quantidade [ N ]
            int countBoxX = panelX + 36;
            int countBoxY = panelY + 178;
            int countBoxW = 32;
            int countBoxH = 18;
            graphics.fill(countBoxX, countBoxY, countBoxX + countBoxW, countBoxY + countBoxH, 0xFF181818);
            graphics.renderOutline(countBoxX, countBoxY, countBoxW, countBoxH, 0xFF444444);
            graphics.drawCenteredString(this.font, String.valueOf(selectedQuantity), countBoxX + countBoxW / 2, countBoxY + 5, 0xFFFFFFFF);
        }

        // 9. Tooltip flutuante do item sob o cursor (fora do scissor)
        if (hoveredStack != null) {
            graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelHeight = getPanelHeight();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = COLS * 18;
        int gridHeight = ROWS * 18;

        // Clique na grade de itens
        if (mouseX >= gridX && mouseX < gridX + gridWidth &&
            mouseY >= gridY && mouseY < gridY + gridHeight) {
            int col = (int) (mouseX - gridX) / 18;
            int row = (int) (mouseY - gridY + this.scrollOffset) / 18;
            int itemIndex = row * COLS + col;
            if (itemIndex >= 0 && itemIndex < filteredItems.size()) {
                selectItem(filteredItems.get(itemIndex));
                return true;
            }
        }

        // Clique na barra de rolagem lateral
        int trackX = gridX + gridWidth + 3;
        int trackY = gridY;
        int trackW = 6;
        int trackH = gridHeight;
        double maxScroll = getMaxScroll();

        if (button == 0 && maxScroll > 0 &&
            mouseX >= trackX - 2 && mouseX <= trackX + trackW + 2 &&
            mouseY >= trackY && mouseY <= trackY + trackH) {
            int totalRows = getTotalRows();
            int thumbH = Math.max(14, (int) ((float) trackH / (totalRows * 18.0) * trackH));
            int thumbY = trackY + (int) ((this.scrollOffset / maxScroll) * (trackH - thumbH));

            if (mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                this.dragYOffset = mouseY - thumbY;
            } else {
                this.dragYOffset = thumbH / 2.0;
                double relativeY = mouseY - trackY - dragYOffset;
                double progress = Math.max(0.0, Math.min(1.0, relativeY / (trackH - thumbH)));
                this.scrollOffset = progress * maxScroll;
            }
            this.isDraggingScrollbar = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDraggingScrollbar && button == 0) {
            int panelHeight = getPanelHeight();
            int panelY = (this.height - panelHeight) / 2;
            int gridY = panelY + 44;
            int trackY = gridY;
            int trackH = ROWS * 18;
            double maxScroll = getMaxScroll();

            if (maxScroll > 0) {
                int totalRows = getTotalRows();
                int thumbH = Math.max(14, (int) ((float) trackH / (totalRows * 18.0) * trackH));
                double relativeY = mouseY - trackY - this.dragYOffset;
                double progress = Math.max(0.0, Math.min(1.0, relativeY / (trackH - thumbH)));
                this.scrollOffset = progress * maxScroll;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int panelHeight = getPanelHeight();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = COLS * 18 + 12;
        int gridHeight = ROWS * 18;

        if (mouseX >= gridX && mouseX <= gridX + gridWidth && mouseY >= gridY && mouseY <= gridY + gridHeight) {
            double maxScroll = getMaxScroll();
            if (maxScroll > 0) {
                this.scrollOffset = Math.max(0.0, Math.min(maxScroll, this.scrollOffset - scrollY * 18.0));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    public double getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(double scrollOffset) {
        this.scrollOffset = Math.max(0.0, Math.min(getMaxScroll(), scrollOffset));
    }

    public int getSelectedQuantity() {
        return selectedQuantity;
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = Math.max(1, selectedQuantity);
    }

    public ItemStack getSelectedStack() {
        return selectedStack;
    }

    public boolean isQuantityMode() {
        return isQuantityMode;
    }
}
