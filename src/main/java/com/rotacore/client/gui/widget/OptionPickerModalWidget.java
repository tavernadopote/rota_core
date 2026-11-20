/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.widget.RotaButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class OptionPickerModalWidget
extends Screen {
    public static final int PANEL_WIDTH = 220;
    public static final int PANEL_HEIGHT = 200;
    public static final int ITEM_ROW_HEIGHT = 20;
    private final Screen parentScreen;
    private final Consumer<PickerOption> onSelect;
    private final List<PickerOption> allOptions = new ArrayList<PickerOption>();
    private final List<PickerOption> filteredOptions = new ArrayList<PickerOption>();
    private EditBox searchBox;
    private RotaButton cancelButton;
    private double scrollAmount = 0.0;

    public OptionPickerModalWidget(Screen parentScreen, Component title, List<PickerOption> options, Consumer<PickerOption> onSelect) {
        super((Component)(title != null ? title : Component.literal((String)"Selecionar Op\u00e7\u00e3o")));
        this.parentScreen = parentScreen;
        this.onSelect = onSelect;
        if (options != null) {
            this.allOptions.addAll(options);
        }
        this.filterOptions("");
    }

    public static void open(Screen parent, Component title, List<PickerOption> options, Consumer<PickerOption> onSelect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen((Screen)new OptionPickerModalWidget(parent, title, options, onSelect));
        }
    }

    public void filterOptions(String query) {
        String q;
        this.filteredOptions.clear();
        String string = q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            this.filteredOptions.addAll(this.allOptions);
        } else {
            for (PickerOption opt : this.allOptions) {
                String labelStr = opt.label().getString().toLowerCase(Locale.ROOT);
                String idStr = opt.id().toLowerCase(Locale.ROOT);
                if (!labelStr.contains(q) && !idStr.contains(q)) continue;
                this.filteredOptions.add(opt);
            }
        }
        this.scrollAmount = 0.0;
    }

    protected void init() {
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - 200) / 2;
        this.searchBox = new EditBox(this.font, panelX + 10, panelY + 22, 200, 18, (Component)Component.literal((String)"Buscar..."));
        this.searchBox.setHint((Component)Component.literal((String)"\u00a78Buscar op\u00e7\u00e3o..."));
        this.searchBox.setResponder(this::filterOptions);
        this.searchBox.setFocused(true);
        this.addRenderableWidget((GuiEventListener)this.searchBox);
        int btnW = 80;
        int btnY = panelY + 172;
        this.cancelButton = new RotaButton(panelX + (220 - btnW) / 2, btnY, btnW, 20, (Component)Component.literal((String)"Cancelar"), btn -> this.close());
        this.addRenderableWidget((GuiEventListener)this.cancelButton);
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
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - 200) / 2;
        graphics.fill(panelX, panelY, panelX + 220, panelY + 200, -14540254);
        graphics.renderOutline(panelX, panelY, 220, 200, -3750202);
        graphics.renderOutline(panelX + 1, panelY + 1, 218, 198, -12961222);
        graphics.drawCenteredString(this.font, this.title, panelX + 110, panelY + 8, -22016);
        super.render(graphics, mouseX, mouseY, partialTick);
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = 200;
        int listHeight = 124;
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, -15198184);
        graphics.renderOutline(listX, listY, listWidth, listHeight, -13158601);
        int totalContentHeight = this.filteredOptions.size() * 20;
        double maxScroll = Math.max(0.0, (double)(totalContentHeight - listHeight));
        this.scrollAmount = Math.max(0.0, Math.min(maxScroll, this.scrollAmount));
        graphics.enableScissor(listX + 1, listY + 1, listX + listWidth - 1, listY + listHeight - 1);
        int startY = listY + 2 - (int)this.scrollAmount;
        for (int i = 0; i < this.filteredOptions.size(); ++i) {
            boolean isHovered;
            PickerOption opt = this.filteredOptions.get(i);
            int rowY = startY + i * 20;
            if (rowY + 20 < listY || rowY > listY + listHeight) continue;
            boolean bl = isHovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= rowY && mouseY < rowY + 20 && mouseY >= listY && mouseY <= listY + listHeight;
            if (isHovered) {
                graphics.fill(listX + 2, rowY, listX + listWidth - 2, rowY + 20, 872393216);
            }
            ItemStack iconStack = opt.iconSupplier() != null ? opt.iconSupplier().get() : ItemStack.EMPTY;
            boolean hasIcon = iconStack != null && !iconStack.isEmpty();
            int textLeft = listX + 6;
            if (hasIcon) {
                int iconX = listX + 4;
                int iconY = rowY + 1;
                graphics.fill(iconX, iconY, iconX + 18, iconY + 18, -14013910);
                graphics.renderOutline(iconX, iconY, 18, 18, -12303292);
                graphics.renderFakeItem(iconStack, iconX + 1, iconY + 1);
                textLeft = iconX + 22;
            }
            int maxTextW = listWidth - (textLeft - listX) - 6;
            Object labelStr = opt.label().getString();
            if (this.font.width((String)labelStr) > maxTextW) {
                labelStr = this.font.plainSubstrByWidth((String)labelStr, maxTextW - 6) + "...";
            }
            int textColor = isHovered ? -1 : -2236963;
            int textY = rowY + 5;
            graphics.drawString(this.font, (String)labelStr, textLeft, textY, textColor, false);
        }
        graphics.disableScissor();
        if (maxScroll > 0.0) {
            int scrollbarX = listX + listWidth - 4;
            int thumbH = Math.max(12, (int)((float)listHeight / (float)totalContentHeight * (float)listHeight));
            int thumbY = listY + (int)((double)((float)this.scrollAmount) / maxScroll * (double)(listHeight - thumbH));
            graphics.fill(scrollbarX, listY, scrollbarX + 3, listY + listHeight, 0x44000000);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbH, -5592406);
        }
        if (this.filteredOptions.isEmpty()) {
            graphics.drawCenteredString(this.font, "\u00a77Nenhuma op\u00e7\u00e3o encontrada.", listX + listWidth / 2, listY + listHeight / 2 - 4, -7829368);
        }
        graphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relativeY;
        int clickedIndex;
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - 200) / 2;
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = 200;
        int listHeight = 124;
        if (button == 0 && mouseX >= (double)listX && mouseX <= (double)(listX + listWidth) && mouseY >= (double)listY && mouseY <= (double)(listY + listHeight) && (clickedIndex = (relativeY = (int)(mouseY - (double)listY + this.scrollAmount - 2.0)) / 20) >= 0 && clickedIndex < this.filteredOptions.size()) {
            this.selectOption(this.filteredOptions.get(clickedIndex));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalContentHeight;
        double maxScroll;
        int panelX = (this.width - 220) / 2;
        int panelY = (this.height - 200) / 2;
        int listX = panelX + 10;
        int listY = panelY + 44;
        int listWidth = 200;
        int listHeight = 124;
        if (mouseX >= (double)listX && mouseX <= (double)(listX + listWidth) && mouseY >= (double)listY && mouseY <= (double)(listY + listHeight) && (maxScroll = Math.max(0.0, (double)((totalContentHeight = this.filteredOptions.size() * 20) - listHeight))) > 0.0) {
            this.scrollAmount = Math.max(0.0, Math.min(maxScroll, this.scrollAmount - scrollY * 16.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void selectOption(PickerOption option) {
        if (this.onSelect != null && option != null) {
            this.onSelect.accept(option);
        }
        this.close();
    }

    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<PickerOption> getAllOptions() {
        return this.allOptions;
    }

    public List<PickerOption> getFilteredOptions() {
        return this.filteredOptions;
    }

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
}

