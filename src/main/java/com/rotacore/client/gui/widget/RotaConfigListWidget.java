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
        this.setScrollAmount(0);
    }

    @Override
    public int getRowWidth() {
        return this.width - 24;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 8;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
