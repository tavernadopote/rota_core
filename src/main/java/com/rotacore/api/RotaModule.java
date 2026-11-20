/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.api;

import com.rotacore.api.RotaCategory;
import com.rotacore.client.gui.RotaModuleCategoriesScreen;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface RotaModule {
    public String getId();

    public Component getDisplayName();

    public String getVersion();

    public Component getDescription();

    default public List<RotaCategory> getCategories() {
        return List.of();
    }

    default public Screen createConfigScreen(Screen parent) {
        return new RotaModuleCategoriesScreen(parent, this, this.getCategories(), null);
    }

    default public void renderDashboard(GuiGraphics graphics, int x, int y, int width, int height, Font font) {
        graphics.drawString(font, this.getDisplayName(), x + 10, y + 10, -22016);
        graphics.drawString(font, (Component)Component.translatable((String)"gui.rota_core.module_version", (Object[])new Object[]{this.getVersion()}), x + 10, y + 25, -5592406);
        graphics.drawString(font, this.getDescription(), x + 10, y + 40, -2236963);
    }

    default public int getOrder() {
        return 100;
    }
}

