/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.client.gui.hud;

import com.rotacore.client.gui.hud.RotaHudAnchor;
import com.rotacore.client.gui.hud.RotaViewportShape;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface IRotaHudElement {
    public String getId();

    public Component getDisplayName();

    public RotaHudAnchor getDefaultAnchor();

    public RotaViewportShape getShape();

    public int getWidth();

    public int getHeight();

    public boolean isVisible();

    public void render(GuiGraphics var1, int var2, int var3, int var4, int var5, float var6);

    default public int getMarginX() {
        return 6;
    }

    default public int getMarginY() {
        return 6;
    }

    default public int getPriority() {
        return 0;
    }

    default public boolean isMovable() {
        return true;
    }

    default public boolean isPreviewMode() {
        return false;
    }

    default public void setPreviewMode(boolean preview) {
    }
}

