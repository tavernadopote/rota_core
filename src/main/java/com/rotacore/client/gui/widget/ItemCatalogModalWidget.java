/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.widget.RotaButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ItemCatalogModalWidget
extends Screen {
    public static final int PANEL_WIDTH = 220;
    public static final int COLS = 10;
    public static final int ROWS = 6;
    public static final int VISIBLE_SLOTS = 60;
    private final Screen parentScreen;
    private final Consumer<ItemStack> onSelect;
    private final BiConsumer<ItemStack, Integer> onSelectWithQuantity;
    private final boolean isQuantityMode;
    private final List<ItemStack> allItems = new ArrayList<ItemStack>();
    private final List<ItemStack> filteredItems = new ArrayList<ItemStack>();
    private EditBox searchBox;
    private RotaButton cancelButton;
    private RotaButton confirmButton;
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
        super((Component)Component.literal((String)"Selecionar \u00cdcone do Cat\u00e1logo"));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;
        this.onSelectWithQuantity = null;
        this.isQuantityMode = false;
        this.selectedQuantity = 1;
        this.loadRegisteredItems();
    }

    public ItemCatalogModalWidget(Screen parentScreen, int initialCount, BiConsumer<ItemStack, Integer> onSelectWithQuantity) {
        super((Component)Component.literal((String)"Selecionar Item e Quantidade"));
        this.parentScreen = parentScreen;
        this.onSelect = null;
        this.onSelectWithQuantity = onSelectWithQuantity;
        this.isQuantityMode = true;
        this.selectedQuantity = Math.max(1, initialCount);
        this.loadRegisteredItems();
    }

    public ItemCatalogModalWidget(Consumer<ItemStack> onSelect) {
        this(Minecraft.getInstance() != null ? Minecraft.getInstance().screen : null, onSelect);
    }

    public static void open(Screen parent, Consumer<ItemStack> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen((Screen)new ItemCatalogModalWidget(parent, onSelect));
        }
    }

    public static void openWithQuantity(Screen parent, int initialCount, BiConsumer<ItemStack, Integer> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen((Screen)new ItemCatalogModalWidget(parent, initialCount, onSelect));
        }
    }

    public int getPanelHeight() {
        return this.isQuantityMode ? 236 : 190;
    }

    public void loadRegisteredItems() {
        this.allItems.clear();
        try {
            if (BuiltInRegistries.ITEM != null) {
                for (Item item : BuiltInRegistries.ITEM) {
                    ItemStack stack;
                    if (item == null || item == Items.AIR || (stack = new ItemStack((ItemLike)item)).isEmpty()) continue;
                    this.allItems.add(stack);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.filterItems("");
    }

    public void setItems(List<ItemStack> items) {
        this.allItems.clear();
        if (items != null) {
            for (ItemStack item : items) {
                if (item == null || item.isEmpty()) continue;
                this.allItems.add(item);
            }
        }
        this.filterItems(this.searchBox != null ? this.searchBox.getValue() : "");
    }

    public void filterItems(String query) {
        String q;
        this.filteredItems.clear();
        String string = q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            this.filteredItems.addAll(this.allItems);
        } else if (q.startsWith("@")) {
            String modQuery = q.substring(1).trim();
            for (ItemStack stack : this.allItems) {
                try {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)stack.getItem());
                    if (key == null || !key.getNamespace().toLowerCase(Locale.ROOT).contains(modQuery)) continue;
                    this.filteredItems.add(stack);
                }
                catch (Throwable key) {}
            }
        } else {
            for (ItemStack stack : this.allItems) {
                try {
                    ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)stack.getItem());
                    String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                    if (!name.contains(q) && (key == null || !key.getPath().toLowerCase(Locale.ROOT).contains(q))) continue;
                    this.filteredItems.add(stack);
                }
                catch (Throwable throwable) {}
            }
        }
        this.scrollOffset = 0.0;
    }

    public int getTotalRows() {
        return (int)Math.ceil((double)this.filteredItems.size() / 10.0);
    }

    public double getMaxScroll() {
        return Math.max(0.0, (double)(this.getTotalRows() - 6) * 18.0);
    }

    protected void init() {
        int panelHeight = this.getPanelHeight();
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - panelHeight) / 2;
        this.searchBox = new EditBox(this.font, panelX + 10, panelY + 22, 200, 18, (Component)Component.literal((String)"Buscar item..."));
        this.searchBox.setHint((Component)Component.literal((String)"\u00a78Buscar por nome ou @mod..."));
        this.searchBox.setResponder(this::filterItems);
        this.searchBox.setFocused(true);
        this.addRenderableWidget((GuiEventListener)this.searchBox);
        if (this.isQuantityMode) {
            int ctrlY = panelY + 178;
            this.btnMinus = new RotaButton(panelX + 16, ctrlY, 18, 18, (Component)Component.literal((String)"-"), btn -> {
                if (this.selectedQuantity > 1) {
                    --this.selectedQuantity;
                    this.updateQuantityState();
                }
            });
            this.btnPlus = new RotaButton(panelX + 70, ctrlY, 18, 18, (Component)Component.literal((String)"+"), btn -> {
                int max;
                int n = max = this.selectedStack != null && !this.selectedStack.isEmpty() ? this.selectedStack.getMaxStackSize() : 64;
                if (this.selectedQuantity < max) {
                    ++this.selectedQuantity;
                    this.updateQuantityState();
                }
            });
            this.btnQuick1 = new RotaButton(panelX + 94, ctrlY, 22, 18, (Component)Component.literal((String)"1"), btn -> {
                this.selectedQuantity = 1;
                this.updateQuantityState();
            });
            this.btnQuick16 = new RotaButton(panelX + 119, ctrlY, 26, 18, (Component)Component.literal((String)"16"), btn -> {
                this.selectedQuantity = 16;
                this.updateQuantityState();
            });
            this.btnQuick32 = new RotaButton(panelX + 148, ctrlY, 26, 18, (Component)Component.literal((String)"32"), btn -> {
                this.selectedQuantity = 32;
                this.updateQuantityState();
            });
            this.btnQuick64 = new RotaButton(panelX + 177, ctrlY, 26, 18, (Component)Component.literal((String)"64"), btn -> {
                this.selectedQuantity = 64;
                this.updateQuantityState();
            });
            this.addRenderableWidget((GuiEventListener)this.btnMinus);
            this.addRenderableWidget((GuiEventListener)this.btnPlus);
            this.addRenderableWidget((GuiEventListener)this.btnQuick1);
            this.addRenderableWidget((GuiEventListener)this.btnQuick16);
            this.addRenderableWidget((GuiEventListener)this.btnQuick32);
            this.addRenderableWidget((GuiEventListener)this.btnQuick64);
            int actionY = panelY + 204;
            this.cancelButton = new RotaButton(panelX + 24, actionY, 80, 20, (Component)Component.literal((String)"Cancelar"), btn -> this.close());
            this.confirmButton = new RotaButton(panelX + 114, actionY, 82, 20, (Component)Component.literal((String)"Confirmar"), btn -> this.confirmSelection());
            this.confirmButton.setAccent(true);
            this.confirmButton.active = this.selectedStack != null && !this.selectedStack.isEmpty();
            this.addRenderableWidget((GuiEventListener)this.cancelButton);
            this.addRenderableWidget((GuiEventListener)this.confirmButton);
        } else {
            int btnY = panelY + panelHeight - 28;
            this.cancelButton = new RotaButton(panelX + 70, btnY, 80, 20, (Component)Component.literal((String)"Cancelar"), btn -> this.close());
            this.addRenderableWidget((GuiEventListener)this.cancelButton);
        }
    }

    private void updateQuantityState() {
        if (this.selectedStack != null && !this.selectedStack.isEmpty()) {
            this.selectedStack.setCount(this.selectedQuantity);
            if (this.confirmButton != null) {
                this.confirmButton.active = true;
            }
        }
    }

    private void confirmSelection() {
        if (this.onSelectWithQuantity != null && this.selectedStack != null && !this.selectedStack.isEmpty()) {
            ItemStack result = this.selectedStack.copy();
            result.setCount(this.selectedQuantity);
            this.onSelectWithQuantity.accept(result, this.selectedQuantity);
        }
        this.close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void selectItem(ItemStack stack) {
        if (this.isQuantityMode) {
            ItemStack itemStack = this.selectedStack = stack != null && !stack.isEmpty() ? stack.copy() : null;
            if (this.selectedStack != null) {
                int max = this.selectedStack.getMaxStackSize();
                if (this.selectedQuantity > max) {
                    this.selectedQuantity = max;
                }
                this.selectedStack.setCount(this.selectedQuantity);
            }
            if (this.confirmButton != null) {
                this.confirmButton.active = this.selectedStack != null;
            }
        } else {
            if (this.onSelect != null && stack != null && !stack.isEmpty()) {
                this.onSelect.accept(stack.copy());
            }
            this.close();
        }
    }

    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    protected void renderMenuBackground(GuiGraphics g) {
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.parentScreen != null) {
            this.parentScreen.render(graphics, -1, -1, partialTick);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0f, 0.0f, 400.0f);
        graphics.fill(0, 0, this.width, this.height, -2013265920);
        int panelHeight = this.getPanelHeight();
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - panelHeight) / 2;
        graphics.fill(panelX, panelY, panelX + 220, panelY + panelHeight, -14540254);
        graphics.renderOutline(panelX, panelY, 220, panelHeight, -3750202);
        graphics.renderOutline(panelX + 1, panelY + 1, 218, panelHeight - 2, -12961222);
        MutableComponent titleComp = this.isQuantityMode ? Component.literal((String)"Selecionar Item e Quantidade") : Component.literal((String)"Selecionar \u00cdcone do Cat\u00e1logo");
        graphics.drawCenteredString(this.font, (Component)titleComp, panelX + 110, panelY + 8, -22016);
        super.render(graphics, mouseX, mouseY, partialTick);
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = 180;
        int gridHeight = 108;
        double maxScroll = this.getMaxScroll();
        this.scrollOffset = Math.max(0.0, Math.min(maxScroll, this.scrollOffset));
        int totalRows = this.getTotalRows();
        int startRow = (int)(this.scrollOffset / 18.0);
        int endRow = Math.min(totalRows, startRow + 6 + (this.scrollOffset % 18.0 > 0.0 ? 1 : 0));
        ItemStack hoveredStack = null;
        graphics.enableScissor(gridX, gridY, gridX + gridWidth, gridY + gridHeight);
        for (int r = startRow; r < endRow; ++r) {
            int slotY = gridY + r * 18 - (int)this.scrollOffset;
            for (int c = 0; c < 10; ++c) {
                boolean isHovered;
                int slotX = gridX + c * 18;
                graphics.fill(slotX, slotY, slotX + 18, slotY + 18, -13158601);
                graphics.renderOutline(slotX, slotY, 18, 18, -11184811);
                int itemIndex = r * 10 + c;
                if (itemIndex >= this.filteredItems.size()) continue;
                ItemStack stack = this.filteredItems.get(itemIndex);
                boolean bl = isHovered = mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18 && mouseY >= gridY && mouseY < gridY + gridHeight;
                if (isHovered) {
                    graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x44FFFFFF);
                    hoveredStack = stack;
                }
                graphics.renderFakeItem(stack, slotX + 1, slotY + 1);
            }
        }
        graphics.disableScissor();
        int trackX = gridX + gridWidth + 3;
        int trackY = gridY;
        int trackW = 4;
        int trackH = gridHeight;
        graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0x44000000);
        if (maxScroll > 0.0) {
            int thumbH = Math.max(14, (int)((double)trackH / ((double)totalRows * 18.0) * (double)trackH));
            int thumbY = trackY + (int)(this.scrollOffset / maxScroll * (double)(trackH - thumbH));
            graphics.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, -5592406);
        }
        if (this.isQuantityMode) {
            int detailY = panelY + 156;
            int slotBoxX = panelX + 16;
            graphics.fill(slotBoxX, detailY, slotBoxX + 18, detailY + 18, -14013910);
            graphics.renderOutline(slotBoxX, detailY, 18, 18, -11184811);
            if (this.selectedStack != null && !this.selectedStack.isEmpty()) {
                graphics.renderFakeItem(this.selectedStack, slotBoxX + 1, detailY + 1);
                Object itemName = this.selectedStack.getHoverName().getString();
                int maxNameWidth = 162;
                if (this.font.width((String)itemName) > maxNameWidth) {
                    itemName = this.font.plainSubstrByWidth((String)itemName, maxNameWidth - 6) + "...";
                }
                graphics.drawString(this.font, (String)itemName, panelX + 38, detailY + 5, -22016, false);
            } else {
                graphics.drawString(this.font, "\u00a77Clique em um item da grade...", panelX + 38, detailY + 5, -7829368, false);
            }
            int countBoxX = panelX + 36;
            int countBoxY = panelY + 178;
            int countBoxW = 32;
            int countBoxH = 18;
            graphics.fill(countBoxX, countBoxY, countBoxX + countBoxW, countBoxY + countBoxH, -15198184);
            graphics.renderOutline(countBoxX, countBoxY, countBoxW, countBoxH, -12303292);
            graphics.drawCenteredString(this.font, String.valueOf(this.selectedQuantity), countBoxX + countBoxW / 2, countBoxY + 5, -1);
        }
        if (hoveredStack != null) {
            graphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int col;
        int row;
        int itemIndex;
        int panelHeight = this.getPanelHeight();
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = 180;
        int gridHeight = 108;
        if (mouseX >= (double)gridX && mouseX < (double)(gridX + gridWidth) && mouseY >= (double)gridY && mouseY < (double)(gridY + gridHeight) && (itemIndex = (row = (int)(mouseY - (double)gridY + this.scrollOffset) / 18) * 10 + (col = (int)(mouseX - (double)gridX) / 18)) >= 0 && itemIndex < this.filteredItems.size()) {
            this.selectItem(this.filteredItems.get(itemIndex));
            return true;
        }
        int trackX = gridX + gridWidth + 3;
        int trackY = gridY;
        int trackW = 6;
        int trackH = gridHeight;
        double maxScroll = this.getMaxScroll();
        if (button == 0 && maxScroll > 0.0 && mouseX >= (double)(trackX - 2) && mouseX <= (double)(trackX + trackW + 2) && mouseY >= (double)trackY && mouseY <= (double)(trackY + trackH)) {
            int totalRows = this.getTotalRows();
            int thumbH = Math.max(14, (int)((double)trackH / ((double)totalRows * 18.0) * (double)trackH));
            int thumbY = trackY + (int)(this.scrollOffset / maxScroll * (double)(trackH - thumbH));
            if (mouseY >= (double)thumbY && mouseY <= (double)(thumbY + thumbH)) {
                this.dragYOffset = mouseY - (double)thumbY;
            } else {
                this.dragYOffset = (double)thumbH / 2.0;
                double relativeY = mouseY - (double)trackY - this.dragYOffset;
                double progress = Math.max(0.0, Math.min(1.0, relativeY / (double)(trackH - thumbH)));
                this.scrollOffset = progress * maxScroll;
            }
            this.isDraggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDraggingScrollbar && button == 0) {
            int gridY;
            int panelHeight = this.getPanelHeight();
            int panelY = (this.height - panelHeight) / 2;
            int trackY = gridY = panelY + 44;
            int trackH = 108;
            double maxScroll = this.getMaxScroll();
            if (maxScroll > 0.0) {
                int totalRows = this.getTotalRows();
                int thumbH = Math.max(14, (int)((double)trackH / ((double)totalRows * 18.0) * (double)trackH));
                double relativeY = mouseY - (double)trackY - this.dragYOffset;
                double progress = Math.max(0.0, Math.min(1.0, relativeY / (double)(trackH - thumbH)));
                this.scrollOffset = progress * maxScroll;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double maxScroll;
        int panelHeight = this.getPanelHeight();
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - panelHeight) / 2;
        int gridX = panelX + 16;
        int gridY = panelY + 44;
        int gridWidth = 192;
        int gridHeight = 108;
        if (mouseX >= (double)gridX && mouseX <= (double)(gridX + gridWidth) && mouseY >= (double)gridY && mouseY <= (double)(gridY + gridHeight) && (maxScroll = this.getMaxScroll()) > 0.0) {
            this.scrollOffset = Math.max(0.0, Math.min(maxScroll, this.scrollOffset - scrollY * 18.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<ItemStack> getFilteredItems() {
        return this.filteredItems;
    }

    public double getScrollOffset() {
        return this.scrollOffset;
    }

    public void setScrollOffset(double scrollOffset) {
        this.scrollOffset = Math.max(0.0, Math.min(this.getMaxScroll(), scrollOffset));
    }

    public int getSelectedQuantity() {
        return this.selectedQuantity;
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = Math.max(1, selectedQuantity);
    }

    public ItemStack getSelectedStack() {
        return this.selectedStack;
    }

    public boolean isQuantityMode() {
        return this.isQuantityMode;
    }
}

