package com.rotacore.api;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Representa uma categoria navegável de configurações de um módulo da Rota Mods.
 */
public class RotaCategory {

    private final String id;
    private final Component title;
    private final String description;
    private final Supplier<List<RotaConfigOption>> optionsSupplier;
    private final Consumer<String> resetCategoryAction;

    public RotaCategory(
            String id,
            Component title,
            String description,
            Supplier<List<RotaConfigOption>> optionsSupplier
    ) {
        this(id, title, description, optionsSupplier, null);
    }

    public RotaCategory(
            String id,
            Component title,
            String description,
            Supplier<List<RotaConfigOption>> optionsSupplier,
            Consumer<String> resetCategoryAction
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.optionsSupplier = optionsSupplier;
        this.resetCategoryAction = resetCategoryAction;
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<RotaConfigOption> getOptions() {
        return optionsSupplier != null ? optionsSupplier.get() : List.of();
    }

    public void resetDefaults() {
        if (resetCategoryAction != null) {
            resetCategoryAction.accept(this.id);
        }
    }

    /**
     * Cria a tela da categoria com a lista de controles e o painel inspector lateral.
     */
    public Screen createScreen(Screen parentScreen, Runnable onSaveAction) {
        return new RotaCategoryScreen(parentScreen, this, onSaveAction);
    }
}
