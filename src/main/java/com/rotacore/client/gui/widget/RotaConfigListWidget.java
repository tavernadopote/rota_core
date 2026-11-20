/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList
 *  net.minecraft.client.gui.components.events.GuiEventListener
 */
package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.client.gui.widget.RotaConfigEntry;
import com.rotacore.config.RotaConfigOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class RotaConfigListWidget
extends ContainerObjectSelectionList<RotaConfigEntry> {
    private final RotaCategoryScreen parentScreen;
    private final List<RotaConfigOption> allOptions = new ArrayList<RotaConfigOption>();
    private String currentFilter = "";

    public RotaConfigListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight, RotaCategoryScreen parentScreen) {
        super(minecraft, width, height, y, itemHeight);
        this.parentScreen = parentScreen;
    }

    public void setOptions(List<RotaConfigOption> options) {
        this.allOptions.clear();
        if (options != null) {
            this.allOptions.addAll(options);
        }
        this.rebuildEntries();
    }

    public void setFilter(String query) {
        this.currentFilter = query == null ? "" : query.trim().toLowerCase();
        this.rebuildEntries();
    }

    private void rebuildEntries() {
        double savedScroll = this.getScrollAmount();
        this.clearEntries();
        for (RotaConfigOption opt : this.allOptions) {
            String desc;
            if (this.currentFilter.isEmpty()) {
                this.addEntry((AbstractSelectionList.Entry)new RotaConfigEntry(opt, this.parentScreen, 120));
                continue;
            }
            if (opt.getType() == RotaConfigOption.Type.HEADER) continue;
            String title = opt.getTitle().getString().toLowerCase();
            String string = desc = opt.getDescription() != null ? opt.getDescription().toLowerCase() : "";
            if (!title.contains(this.currentFilter) && !desc.contains(this.currentFilter)) continue;
            this.addEntry((AbstractSelectionList.Entry)new RotaConfigEntry(opt, this.parentScreen, 120));
        }
        this.setScrollAmount(savedScroll);
    }

    public int getRowWidth() {
        return this.width - 24;
    }

    protected int getScrollbarPosition() {
        return this.width - 8;
    }

    public int getEntryHeight(int index) {
        if (index >= 0 && index < this.getItemCount()) {
            return ((RotaConfigEntry)this.getEntry(index)).getEntryHeight();
        }
        return this.itemHeight;
    }

    protected int getRowTop(int index) {
        int top = this.getY() + 4 - (int)this.getScrollAmount() + this.headerHeight;
        for (int i = 0; i < index && i < this.getItemCount(); ++i) {
            top += this.getEntryHeight(i);
        }
        return top;
    }

    protected int getRowBottom(int index) {
        return this.getRowTop(index) + this.getEntryHeight(index);
    }

    protected int getMaxPosition() {
        int total = this.headerHeight + 4;
        for (int i = 0; i < this.getItemCount(); ++i) {
            total += this.getEntryHeight(i);
        }
        return total;
    }

    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int count = this.getItemCount();
        for (int i = 0; i < count; ++i) {
            int entryH;
            int rowTop = this.getRowTop(i);
            int rowBottom = rowTop + (entryH = this.getEntryHeight(i));
            if (rowBottom < this.getY() || rowTop > this.getBottom()) continue;
            this.renderItem(graphics, mouseX, mouseY, partialTick, i, rowLeft, rowTop, rowWidth, entryH);
        }
    }

    protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        RotaConfigEntry entry = (RotaConfigEntry)this.getEntry(index);
        boolean isHovered = this.isMouseOver(mouseX, mouseY) && this.getDynamicEntryAtPosition(mouseX, mouseY) == entry;
        entry.render(graphics, index, top, left, width, height, mouseX, mouseY, isHovered, partialTick);
    }

    public RotaConfigEntry getDynamicEntryAtPosition(double mouseX, double mouseY) {
        int rowLeft = this.getRowLeft();
        int rowRight = rowLeft + this.getRowWidth();
        if (mouseX < (double)rowLeft || mouseX > (double)rowRight || mouseY < (double)this.getY() || mouseY > (double)this.getBottom()) {
            return null;
        }
        for (int i = 0; i < this.getItemCount(); ++i) {
            int top = this.getRowTop(i);
            int bottom = this.getRowBottom(i);
            if (!(mouseY >= (double)top) || !(mouseY <= (double)bottom)) continue;
            return (RotaConfigEntry)this.getEntry(i);
        }
        return null;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RotaConfigEntry entry;
        if (this.isMouseOver(mouseX, mouseY) && (entry = this.getDynamicEntryAtPosition(mouseX, mouseY)) != null && entry.mouseClicked(mouseX, mouseY, button)) {
            RotaConfigEntry focused = (RotaConfigEntry)this.getFocused();
            if (focused != entry && focused != null) {
                focused.setFocused(null);
            }
            this.setFocused((GuiEventListener)entry);
            this.setDragging(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}

