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
import java.util.function.Consumer;

/**
 * Modal universal do Rota Core para seleção visual de ícones de itens e blocos do Minecraft.
 * Renderiza painel central vanilla de 220x200px com borda cinza #C6C6C6 sobre fundo escuro semi-transparente,
 * com busca em tempo real, grade de slots 18x18px e paginação limpa.
 */
public class ItemCatalogModalWidget extends Screen {

    public static final int PANEL_WIDTH = 220;
    public static final int PANEL_HEIGHT = 200;
    public static final int COLS = 10;
    public static final int ROWS = 6;
    public static final int ITEMS_PER_PAGE = COLS * ROWS; // 60 slots

    private final Screen parentScreen;
    private final Consumer<ItemStack> onSelect;

    private final List<ItemStack> allItems = new ArrayList<>();
    private final List<ItemStack> filteredItems = new ArrayList<>();

    private EditBox searchBox;
    private RotaButton prevButton;
    private RotaButton nextButton;
    private RotaButton cancelButton;

    private int currentPage = 0;

    public ItemCatalogModalWidget(Screen parentScreen, Consumer<ItemStack> onSelect) {
        super(Component.literal("Selecionar Ícone do Catálogo"));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;

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

        this.currentPage = 0;
        updateNavigationButtons();
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // 1. Campo de Busca no topo
        this.searchBox = new EditBox(this.font, panelX + 10, panelY + 22, PANEL_WIDTH - 20, 18, Component.literal("Buscar item..."));
        this.searchBox.setHint(Component.literal("§8Buscar por nome ou @mod..."));
        this.searchBox.setResponder(this::filterItems);
        this.searchBox.setFocused(true);
        this.addRenderableWidget(this.searchBox);

        // 2. Botões de Navegação no rodapé
        int btnY = panelY + 172;
        this.prevButton = new RotaButton(panelX + 10, btnY, 60, 20, Component.literal("Anterior"), btn -> previousPage());
        this.cancelButton = new RotaButton(panelX + 80, btnY, 60, 20, Component.literal("Cancelar"), btn -> close());
        this.nextButton = new RotaButton(panelX + 150, btnY, 60, 20, Component.literal("Próxima"), btn -> nextPage());

        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.cancelButton);
        this.addRenderableWidget(this.nextButton);

        updateNavigationButtons();
    }

    public void updateNavigationButtons() {
        int totalPages = getTotalPages();
        if (prevButton != null) {
            prevButton.active = currentPage > 0;
        }
        if (nextButton != null) {
            nextButton.active = currentPage < totalPages - 1;
        }
    }

    public void nextPage() {
        if (currentPage < getTotalPages() - 1) {
            currentPage++;
            updateNavigationButtons();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateNavigationButtons();
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE));
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void selectItem(ItemStack stack) {
        if (onSelect != null && stack != null && !stack.isEmpty()) {
            onSelect.accept(stack.copy());
        }
        close();
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

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        // 3. Painel central vanilla (220x200px) com borda cinza #C6C6C6
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF222222);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.renderOutline(panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 0xFF3A3A3A);

        // 4. Título "Selecionar Ícone do Catálogo"
        graphics.drawCenteredString(this.font, Component.literal("Selecionar Ícone do Catálogo"), panelX + PANEL_WIDTH / 2, panelY + 8, 0xFFFFAA00);

        // 5. Renderizar widgets padrão (EditBox e botões)
        super.render(graphics, mouseX, mouseY, partialTick);

        // 6. Grade de Slots 18x18px (10 colunas x 6 linhas)
        int gridX = panelX + (PANEL_WIDTH - COLS * 18) / 2;
        int gridY = panelY + 44;
        ItemStack hoveredStack = null;

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredItems.size());

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int slotX = gridX + c * 18;
                int slotY = gridY + r * 18;

                // Slot escuro vanilla 18x18px com contorno
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
                graphics.renderOutline(slotX, slotY, 18, 18, 0xFF555555);

                int itemIndex = startIndex + (r * COLS + c);
                if (itemIndex < endIndex) {
                    ItemStack stack = filteredItems.get(itemIndex);
                    boolean isHovered = mouseX >= slotX && mouseX < slotX + 18 &&
                                        mouseY >= slotY && mouseY < slotY + 18;

                    if (isHovered) {
                        graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x44FFFFFF);
                        hoveredStack = stack;
                    }

                    graphics.renderFakeItem(stack, slotX + 1, slotY + 1);
                }
            }
        }

        // 7. Tooltip flutuante do item sob o cursor
        if (hoveredStack != null) {
            graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;
        int gridX = panelX + (PANEL_WIDTH - COLS * 18) / 2;
        int gridY = panelY + 44;

        if (mouseX >= gridX && mouseX < gridX + COLS * 18 &&
            mouseY >= gridY && mouseY < gridY + ROWS * 18) {
            int c = (int) (mouseX - gridX) / 18;
            int r = (int) (mouseY - gridY) / 18;
            int itemIndex = currentPage * ITEMS_PER_PAGE + (r * COLS + c);
            if (itemIndex >= 0 && itemIndex < filteredItems.size()) {
                selectItem(filteredItems.get(itemIndex));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
