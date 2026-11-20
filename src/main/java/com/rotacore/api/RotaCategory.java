/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.rotacore.api;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.config.RotaConfigOption;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RotaCategory {
    private final String id;
    private final Component title;
    private final String description;
    private final Supplier<List<RotaConfigOption>> optionsSupplier;
    private final Consumer<String> resetCategoryAction;

    public RotaCategory(String id, Component title, String description, Supplier<List<RotaConfigOption>> optionsSupplier) {
        this(id, title, description, optionsSupplier, null);
    }

    public RotaCategory(String id, Component title, String description, Supplier<List<RotaConfigOption>> optionsSupplier, Consumer<String> resetCategoryAction) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.optionsSupplier = optionsSupplier;
        this.resetCategoryAction = resetCategoryAction;
    }

    public String getId() {
        return this.id;
    }

    public Component getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public List<RotaConfigOption> getOptions() {
        return this.optionsSupplier != null ? this.optionsSupplier.get() : List.of();
    }

    public void resetDefaults() {
        if (this.resetCategoryAction != null) {
            this.resetCategoryAction.accept(this.id);
        }
    }

    public Screen createScreen(Screen parentScreen, Runnable onSaveAction) {
        return new RotaCategoryScreen(parentScreen, this, onSaveAction);
    }
}

