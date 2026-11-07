package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista vertical rolável com suporte a filtragem em tempo real de configurações.
 */
public class RotaConfigListWidget extends ContainerObjectSelectionList<RotaConfigEntry> {

    private final RotaCategoryScreen parentScreen;
    private final List<RotaConfigOption> allOptions = new ArrayList<>();
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
        rebuildEntries();
    }

    public void setFilter(String query) {
        this.currentFilter = (query == null) ? "" : query.trim().toLowerCase();
        rebuildEntries();
    }

    private void rebuildEntries() {
        double savedScroll = this.getScrollAmount();
        this.clearEntries();
        for (RotaConfigOption opt : allOptions) {
            if (currentFilter.isEmpty()) {
                this.addEntry(new RotaConfigEntry(opt, parentScreen, 120));
            } else {
                if (opt.getType() == RotaConfigOption.Type.HEADER) {
                    continue;
                }
                String title = opt.getTitle().getString().toLowerCase();
                String desc = opt.getDescription() != null ? opt.getDescription().toLowerCase() : "";
                if (title.contains(currentFilter) || desc.contains(currentFilter)) {
                    this.addEntry(new RotaConfigEntry(opt, parentScreen, 120));
                }
            }
        }
        this.setScrollAmount(savedScroll);
    }

    @Override
    public int getRowWidth() {
        return this.width - 24;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 8;
    }

    public int getEntryHeight(int index) {
        if (index >= 0 && index < this.getItemCount()) {
            return this.getEntry(index).getEntryHeight();
        }
        return this.itemHeight;
    }

    @Override
    protected int getRowTop(int index) {
        int top = this.getY() + 4 - (int) this.getScrollAmount() + this.headerHeight;
        for (int i = 0; i < index && i < this.getItemCount(); i++) {
            top += getEntryHeight(i);
        }
        return top;
    }

    @Override
    protected int getRowBottom(int index) {
        return this.getRowTop(index) + this.getEntryHeight(index);
    }

    @Override
    protected int getMaxPosition() {
        int total = this.headerHeight + 4;
        for (int i = 0; i < this.getItemCount(); i++) {
            total += getEntryHeight(i);
        }
        return total;
    }

    @Override
    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int count = this.getItemCount();
        for (int i = 0; i < count; i++) {
            int rowTop = this.getRowTop(i);
            int entryH = this.getEntryHeight(i);
            int rowBottom = rowTop + entryH;
            if (rowBottom >= this.getY() && rowTop <= this.getBottom()) {
                this.renderItem(graphics, mouseX, mouseY, partialTick, i, rowLeft, rowTop, rowWidth, entryH);
            }
        }
    }

    @Override
    protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        RotaConfigEntry entry = this.getEntry(index);
        boolean isHovered = this.isMouseOver(mouseX, mouseY) && getDynamicEntryAtPosition(mouseX, mouseY) == entry;
        entry.render(graphics, index, top, left, width, height, mouseX, mouseY, isHovered, partialTick);
    }

    public RotaConfigEntry getDynamicEntryAtPosition(double mouseX, double mouseY) {
        int rowLeft = this.getRowLeft();
        int rowRight = rowLeft + this.getRowWidth();
        if (mouseX < rowLeft || mouseX > rowRight || mouseY < this.getY() || mouseY > this.getBottom()) {
            return null;
        }
        for (int i = 0; i < this.getItemCount(); i++) {
            int top = this.getRowTop(i);
            int bottom = this.getRowBottom(i);
            if (mouseY >= top && mouseY <= bottom) {
                return this.getEntry(i);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            RotaConfigEntry entry = this.getDynamicEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    RotaConfigEntry focused = this.getFocused();
                    if (focused != entry && focused != null) {
                        focused.setFocused(null);
                    }
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
