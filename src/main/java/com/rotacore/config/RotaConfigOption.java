/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.rotacore.config;

import com.rotacore.config.RotaIconPreview;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RotaConfigOption {
    private final Type type;
    private final Component title;
    private final String description;
    private String exampleTip;
    private final String defaultValueText;
    private boolean customTipExplicit = false;
    private boolean tipExplicitlyForced = false;
    private boolean showDefaultValue = false;
    private BooleanSupplier booleanGetter;
    private Consumer<Boolean> booleanSetter;
    private IntSupplier intGetter;
    private IntConsumer intSetter;
    private int intMin;
    private int intMax;
    private Function<Integer, String> intFormatter;
    private DoubleSupplier doubleGetter;
    private DoubleConsumer doubleSetter;
    private double doubleMin;
    private double doubleMax;
    private Function<Double, String> doubleFormatter;
    private Supplier<String> cycleGetter;
    private Runnable cycleAction;
    private Runnable actionCallback;
    private String actionButtonText;
    private boolean dangerAction = false;
    private Supplier<ItemStack> actionIconSupplier = null;
    private Supplier<String> stringGetter;
    private Consumer<String> stringSetter;
    private int multilineHeight = 60;
    private Runnable secondaryAction = null;
    private Component secondaryActionTooltip = Component.literal((String)"Configurar Par\u00e2metros...");
    private List<RotaIconPreview> iconPreviews = List.of();
    private ResourceLocation texturePreviewLocation = null;
    private int texturePreviewWidth = 128;
    private int texturePreviewHeight = 128;
    private Component texturePreviewCaption = null;
    private CustomInspectorRenderer customInspectorRenderer = null;
    private int customInspectorHeight = 160;
    private Supplier<ItemStack> itemStackIconSupplier;
    private ResourceLocation textureIcon;
    private Supplier<ResourceLocation> textureIconSupplier;
    private BooleanSupplier itemActiveGetter;
    private Consumer<Boolean> itemActiveSetter;
    private Runnable moveUpAction;
    private Runnable moveDownAction;
    private boolean showReorderButtons = false;
    private boolean requiresAdmin = false;
    private boolean clientOnly = false;
    private boolean active = true;

    private RotaConfigOption(Type type, Component title, String description, String exampleTip, String defaultValueText) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.exampleTip = exampleTip;
        this.defaultValueText = defaultValueText;
    }

    public static RotaConfigOption header(String title) {
        return new RotaConfigOption(Type.HEADER, (Component)Component.literal((String)title), "", "", "");
    }

    public static RotaConfigOption header(Component title) {
        return new RotaConfigOption(Type.HEADER, title, "", "", "");
    }

    public static RotaConfigOption booleanToggle(Component title, String description, String exampleTip, boolean defaultValue, BooleanSupplier getter, Consumer<Boolean> setter) {
        RotaConfigOption opt = new RotaConfigOption(Type.BOOLEAN, title, description, exampleTip, defaultValue ? "Ativado (SIM)" : "Desativado (N\u00c3O)");
        opt.booleanGetter = getter;
        opt.booleanSetter = setter;
        return opt;
    }

    public static RotaConfigOption intSlider(Component title, String description, String exampleTip, int defaultValue, int min, int max, Function<Integer, String> formatter, IntSupplier getter, IntConsumer setter) {
        RotaConfigOption opt = new RotaConfigOption(Type.INT_SLIDER, title, description, exampleTip, formatter != null ? formatter.apply(defaultValue) : String.valueOf(defaultValue));
        opt.intMin = min;
        opt.intMax = max;
        opt.intFormatter = formatter != null ? formatter : String::valueOf;
        opt.intGetter = getter;
        opt.intSetter = setter;
        return opt;
    }

    public static RotaConfigOption doubleSlider(Component title, String description, String exampleTip, double defaultValue, double min, double max, Function<Double, String> formatter, DoubleSupplier getter, DoubleConsumer setter) {
        RotaConfigOption opt = new RotaConfigOption(Type.DOUBLE_SLIDER, title, description, exampleTip, formatter != null ? formatter.apply(defaultValue) : String.valueOf(defaultValue));
        opt.doubleMin = min;
        opt.doubleMax = max;
        opt.doubleFormatter = formatter != null ? formatter : d -> String.format("%.2f", d);
        opt.doubleGetter = getter;
        opt.doubleSetter = setter;
        return opt;
    }

    public static RotaConfigOption action(Component title, String description, String exampleTip, String buttonText, Runnable action) {
        RotaConfigOption opt = new RotaConfigOption(Type.ACTION, title, description, exampleTip, "\u2014");
        opt.actionButtonText = buttonText;
        opt.actionCallback = action;
        return opt;
    }

    public static RotaConfigOption cycle(Component title, String description, String exampleTip, String defaultValueText, Supplier<String> getter, Runnable cycleAction) {
        RotaConfigOption opt = new RotaConfigOption(Type.CYCLE, title, description, exampleTip, defaultValueText);
        opt.cycleGetter = getter;
        opt.cycleAction = cycleAction;
        return opt;
    }

    public static RotaConfigOption booleanToggle(Component title, String description, boolean defaultValue, BooleanSupplier getter, Consumer<Boolean> setter) {
        return RotaConfigOption.booleanToggle(title, description, null, defaultValue, getter, setter);
    }

    public static RotaConfigOption intSlider(Component title, String description, int defaultValue, int min, int max, Function<Integer, String> formatter, IntSupplier getter, IntConsumer setter) {
        return RotaConfigOption.intSlider(title, description, null, defaultValue, min, max, formatter, getter, setter);
    }

    public static RotaConfigOption doubleSlider(Component title, String description, double defaultValue, double min, double max, Function<Double, String> formatter, DoubleSupplier getter, DoubleConsumer setter) {
        return RotaConfigOption.doubleSlider(title, description, null, defaultValue, min, max, formatter, getter, setter);
    }

    public static RotaConfigOption action(Component title, String description, String buttonText, Runnable action) {
        return RotaConfigOption.action(title, description, null, buttonText, action);
    }

    public static RotaConfigOption dangerAction(Component title, String description, String exampleTip, String buttonText, Runnable callback) {
        RotaConfigOption opt = new RotaConfigOption(Type.ACTION, title, description, exampleTip, "\u2014");
        opt.actionButtonText = buttonText;
        opt.actionCallback = callback;
        opt.dangerAction = true;
        return opt;
    }

    public static RotaConfigOption dangerAction(Component title, String description, String buttonText, Runnable callback) {
        return RotaConfigOption.dangerAction(title, description, null, buttonText, callback);
    }

    public static RotaConfigOption cycle(Component title, String description, String defaultValueText, Supplier<String> getter, Runnable cycleAction) {
        return RotaConfigOption.cycle(title, description, null, defaultValueText, getter, cycleAction);
    }

    public static RotaConfigOption stringInput(Component title, String description, String exampleTip, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        RotaConfigOption opt = new RotaConfigOption(Type.STRING_INPUT, title, description, exampleTip, defaultValue != null ? defaultValue : "");
        opt.stringGetter = getter;
        opt.stringSetter = setter;
        return opt;
    }

    public static RotaConfigOption stringInput(Component title, String description, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        return RotaConfigOption.stringInput(title, description, null, defaultValue, getter, setter);
    }

    public static RotaConfigOption multilineStringInput(Component title, String description, String exampleTip, String defaultValue, int height, Supplier<String> getter, Consumer<String> setter) {
        RotaConfigOption opt = new RotaConfigOption(Type.MULTILINE_STRING_INPUT, title, description, exampleTip, defaultValue != null ? defaultValue : "");
        opt.multilineHeight = Math.max(20, height);
        opt.stringGetter = getter;
        opt.stringSetter = setter;
        return opt;
    }

    public static RotaConfigOption multilineStringInput(Component title, String description, String defaultValue, int height, Supplier<String> getter, Consumer<String> setter) {
        return RotaConfigOption.multilineStringInput(title, description, null, defaultValue, height, getter, setter);
    }

    public static RotaConfigOption multilineStringInput(Component title, String description, String exampleTip, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        return RotaConfigOption.multilineStringInput(title, description, exampleTip, defaultValue, 60, getter, setter);
    }

    public static RotaConfigOption multilineStringInput(Component title, String description, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        return RotaConfigOption.multilineStringInput(title, description, null, defaultValue, 60, getter, setter);
    }

    public static RotaConfigOption itemEntry(Component title, String description, String exampleTip, ItemStack itemIcon, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown, Runnable editAction) {
        RotaConfigOption opt = new RotaConfigOption(Type.ITEM_ENTRY, title, description, exampleTip, "\u2014");
        if (itemIcon != null) {
            opt.itemStackIconSupplier = () -> itemIcon;
        }
        opt.itemActiveGetter = activeGetter;
        opt.itemActiveSetter = activeSetter;
        opt.moveUpAction = moveUp;
        opt.moveDownAction = moveDown;
        boolean bl = opt.showReorderButtons = moveUp != null || moveDown != null;
        if (editAction != null) {
            opt.withSecondaryAction(editAction, (Component)Component.literal((String)"Configurar..."));
        }
        return opt;
    }

    public static RotaConfigOption itemEntry(Component title, String description, Supplier<ItemStack> itemIconSupplier, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown, Runnable editAction) {
        RotaConfigOption opt = new RotaConfigOption(Type.ITEM_ENTRY, title, description, null, "\u2014");
        opt.itemStackIconSupplier = itemIconSupplier;
        opt.itemActiveGetter = activeGetter;
        opt.itemActiveSetter = activeSetter;
        opt.moveUpAction = moveUp;
        opt.moveDownAction = moveDown;
        boolean bl = opt.showReorderButtons = moveUp != null || moveDown != null;
        if (editAction != null) {
            opt.withSecondaryAction(editAction, (Component)Component.literal((String)"Configurar..."));
        }
        return opt;
    }

    public static RotaConfigOption itemEntry(Component title, String description, ItemStack itemIcon, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown, Runnable editAction) {
        return RotaConfigOption.itemEntry(title, description, null, itemIcon, activeGetter, activeSetter, moveUp, moveDown, editAction);
    }

    public static RotaConfigOption itemEntry(Component title, String description, ItemStack itemIcon, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown) {
        return RotaConfigOption.itemEntry(title, description, null, itemIcon, activeGetter, activeSetter, moveUp, moveDown, null);
    }

    public static RotaConfigOption itemEntry(Component title, String description, ResourceLocation textureIcon, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown, Runnable editAction) {
        RotaConfigOption opt = new RotaConfigOption(Type.ITEM_ENTRY, title, description, null, "\u2014");
        opt.textureIcon = textureIcon;
        opt.itemActiveGetter = activeGetter;
        opt.itemActiveSetter = activeSetter;
        opt.moveUpAction = moveUp;
        opt.moveDownAction = moveDown;
        boolean bl = opt.showReorderButtons = moveUp != null || moveDown != null;
        if (editAction != null) {
            opt.withSecondaryAction(editAction, (Component)Component.literal((String)"Configurar..."));
        }
        return opt;
    }

    public static RotaConfigOption itemEntry(Component title, String description, ResourceLocation textureIcon, BooleanSupplier activeGetter, Consumer<Boolean> activeSetter, Runnable moveUp, Runnable moveDown) {
        return RotaConfigOption.itemEntry(title, description, textureIcon, activeGetter, activeSetter, moveUp, moveDown, null);
    }

    public Type getType() {
        return this.type;
    }

    public Component getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getExampleTip() {
        return this.exampleTip;
    }

    public String getDefaultValueText() {
        return this.defaultValueText;
    }

    public boolean hasTip() {
        if (this.exampleTip == null || this.exampleTip.trim().isEmpty()) {
            return false;
        }
        if (this.customTipExplicit) {
            return true;
        }
        return !RotaConfigOption.isGenericTip(this.exampleTip);
    }

    public boolean hasCustomTip() {
        return this.hasTip();
    }

    public static boolean isGenericTip(String tip) {
        if (tip == null || tip.trim().isEmpty()) {
            return true;
        }
        String lower = tip.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("ativado:") || lower.startsWith("ativado =") || lower.startsWith("ativado -") || lower.startsWith("desativado:") || lower.startsWith("desativado =") || lower.startsWith("desativado -") || lower.startsWith("padr\u00e3o:") || lower.startsWith("padrao:") || lower.startsWith("padr\u00e3o =") || lower.startsWith("padrao =") || lower.startsWith("padr\u00e3o recomendado") || lower.startsWith("padrao recomendado");
    }

    public RotaConfigOption withCustomTip(String tip) {
        this.exampleTip = tip;
        this.customTipExplicit = tip != null && !tip.trim().isEmpty();
        return this;
    }

    public RotaConfigOption withCustomTip(boolean customTipExplicit) {
        this.customTipExplicit = customTipExplicit;
        return this;
    }

    public boolean isCustomTipExplicit() {
        return this.customTipExplicit;
    }

    public RotaConfigOption withForceTip(boolean force) {
        this.tipExplicitlyForced = force;
        return this;
    }

    public boolean isTipExplicitlyForced() {
        return this.tipExplicitlyForced;
    }

    public RotaConfigOption withShowDefaultValue(boolean show) {
        this.showDefaultValue = show;
        return this;
    }

    public boolean shouldShowDefaultValue() {
        return this.showDefaultValue;
    }

    public boolean getBooleanValue() {
        return this.booleanGetter != null && this.booleanGetter.getAsBoolean();
    }

    public void setBooleanValue(boolean v) {
        if (this.booleanSetter != null) {
            this.booleanSetter.accept(v);
        }
    }

    public int getIntValue() {
        return this.intGetter != null ? this.intGetter.getAsInt() : this.intMin;
    }

    public void setIntValue(int v) {
        if (this.intSetter != null) {
            this.intSetter.accept(v);
        }
    }

    public int getIntMin() {
        return this.intMin;
    }

    public int getIntMax() {
        return this.intMax;
    }

    public Function<Integer, String> getIntFormatter() {
        return this.intFormatter;
    }

    public double getDoubleValue() {
        return this.doubleGetter != null ? this.doubleGetter.getAsDouble() : this.doubleMin;
    }

    public void setDoubleValue(double v) {
        if (this.doubleSetter != null) {
            this.doubleSetter.accept(v);
        }
    }

    public double getDoubleMin() {
        return this.doubleMin;
    }

    public double getDoubleMax() {
        return this.doubleMax;
    }

    public Function<Double, String> getDoubleFormatter() {
        return this.doubleFormatter;
    }

    public String getCycleText() {
        return this.cycleGetter != null ? this.cycleGetter.get() : "";
    }

    public void triggerCycle() {
        if (this.cycleAction != null) {
            this.cycleAction.run();
        }
    }

    public String getActionButtonText() {
        return this.actionButtonText != null ? this.actionButtonText : "Abrir";
    }

    public void triggerAction() {
        if (this.actionCallback != null) {
            this.actionCallback.run();
        }
    }

    public String getStringValue() {
        return this.stringGetter != null ? this.stringGetter.get() : "";
    }

    public void setStringValue(String value) {
        if (this.stringSetter != null) {
            this.stringSetter.accept(value);
        }
    }

    public Supplier<String> getStringGetter() {
        return this.stringGetter;
    }

    public Consumer<String> getStringSetter() {
        return this.stringSetter;
    }

    public int getMultilineHeight() {
        return this.multilineHeight;
    }

    public RotaConfigOption withMultilineHeight(int multilineHeight) {
        this.multilineHeight = Math.max(20, multilineHeight);
        return this;
    }

    public void setMultilineHeight(int multilineHeight) {
        this.multilineHeight = Math.max(20, multilineHeight);
    }

    public RotaConfigOption withAction(Runnable action) {
        this.actionCallback = action;
        return this;
    }

    public Runnable getActionCallback() {
        return this.actionCallback;
    }

    public boolean isDangerAction() {
        return this.dangerAction;
    }

    public Supplier<ItemStack> getActionIconSupplier() {
        return this.actionIconSupplier;
    }

    public RotaConfigOption withActionIcon(Supplier<ItemStack> iconSupplier) {
        this.actionIconSupplier = iconSupplier;
        return this;
    }

    public RotaConfigOption withActionIcon(ItemStack stack) {
        this.actionIconSupplier = () -> stack != null ? stack : ItemStack.EMPTY;
        return this;
    }

    public RotaConfigOption withSecondaryAction(Runnable action, Component tooltip) {
        this.secondaryAction = action;
        this.secondaryActionTooltip = tooltip != null ? tooltip : Component.literal((String)"Configurar Par\u00e2metros...");
        return this;
    }

    public RotaConfigOption withSecondaryAction(Runnable action) {
        return this.withSecondaryAction(action, (Component)Component.literal((String)"Configurar Par\u00e2metros..."));
    }

    public boolean hasSecondaryAction() {
        return this.secondaryAction != null;
    }

    public Runnable getSecondaryAction() {
        return this.secondaryAction;
    }

    public Component getSecondaryActionTooltip() {
        return this.secondaryActionTooltip;
    }

    public RotaConfigOption withIconPreviews(List<RotaIconPreview> previews) {
        this.iconPreviews = previews != null ? List.copyOf(previews) : List.of();
        return this;
    }

    public List<RotaIconPreview> getIconPreviews() {
        return this.iconPreviews;
    }

    public boolean hasIconPreviews() {
        return this.iconPreviews != null && !this.iconPreviews.isEmpty();
    }

    public RotaConfigOption withTexturePreview(ResourceLocation texture, int width, int height, Component caption) {
        this.texturePreviewLocation = texture;
        this.texturePreviewWidth = width;
        this.texturePreviewHeight = height;
        this.texturePreviewCaption = caption;
        return this;
    }

    public RotaConfigOption withTexturePreview(ResourceLocation texture, int width, int height) {
        return this.withTexturePreview(texture, width, height, null);
    }

    public boolean hasTexturePreview() {
        return this.texturePreviewLocation != null;
    }

    public ResourceLocation getTexturePreviewLocation() {
        return this.texturePreviewLocation;
    }

    public int getTexturePreviewWidth() {
        return this.texturePreviewWidth;
    }

    public int getTexturePreviewHeight() {
        return this.texturePreviewHeight;
    }

    public Component getTexturePreviewCaption() {
        return this.texturePreviewCaption;
    }

    public CustomInspectorRenderer getCustomInspectorRenderer() {
        return this.customInspectorRenderer;
    }

    public boolean hasCustomInspectorRenderer() {
        return this.customInspectorRenderer != null;
    }

    public int getCustomInspectorHeight() {
        return this.customInspectorHeight;
    }

    public RotaConfigOption withCustomInspector(CustomInspectorRenderer renderer) {
        this.customInspectorRenderer = renderer;
        return this;
    }

    public RotaConfigOption withCustomInspector(CustomInspectorRenderer renderer, int height) {
        this.customInspectorRenderer = renderer;
        this.customInspectorHeight = height;
        return this;
    }

    public RotaConfigOption requiresAdmin(boolean requiresAdmin) {
        this.requiresAdmin = requiresAdmin;
        return this;
    }

    public boolean requiresAdmin() {
        return this.requiresAdmin;
    }

    public RotaConfigOption clientOnly(boolean clientOnly) {
        this.clientOnly = clientOnly;
        return this;
    }

    public boolean isClientOnly() {
        return this.clientOnly;
    }

    public boolean clientOnly() {
        return this.clientOnly;
    }

    public RotaConfigOption active(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean canPlayerEdit(Minecraft mc) {
        if (!this.active) {
            return false;
        }
        if (mc == null) {
            return true;
        }
        boolean isSingleplayer = mc.getSingleplayerServer() != null;
        boolean hasAdmin = mc.player != null && mc.player.hasPermissions(2);
        return this.canPlayerEdit(isSingleplayer, hasAdmin);
    }

    public boolean canPlayerEdit(boolean isSingleplayer, boolean hasAdminPermissions) {
        if (!this.active) {
            return false;
        }
        if (isSingleplayer) {
            return true;
        }
        if (!this.requiresAdmin) {
            return true;
        }
        return hasAdminPermissions;
    }

    public ItemStack getItemStackIcon() {
        if (this.itemStackIconSupplier != null) {
            ItemStack stack = this.itemStackIconSupplier.get();
            return stack != null ? stack : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    public boolean hasItemStackIcon() {
        if (this.itemStackIconSupplier == null) {
            return false;
        }
        ItemStack stack = this.itemStackIconSupplier.get();
        return stack != null && !stack.isEmpty();
    }

    public RotaConfigOption withItemStack(ItemStack stack) {
        this.itemStackIconSupplier = () -> stack != null ? stack : ItemStack.EMPTY;
        return this;
    }

    public RotaConfigOption withItemStack(Supplier<ItemStack> supplier) {
        this.itemStackIconSupplier = supplier;
        return this;
    }

    public ResourceLocation getTextureIcon() {
        if (this.textureIconSupplier != null) {
            return this.textureIconSupplier.get();
        }
        return this.textureIcon;
    }

    public boolean hasTextureIcon() {
        return this.getTextureIcon() != null;
    }

    public RotaConfigOption withTextureIcon(ResourceLocation texture) {
        this.textureIcon = texture;
        return this;
    }

    public RotaConfigOption withTextureIcon(Supplier<ResourceLocation> supplier) {
        this.textureIconSupplier = supplier;
        return this;
    }

    public boolean hasItemToggle() {
        return this.itemActiveGetter != null && this.itemActiveSetter != null;
    }

    public boolean isItemActive() {
        return this.itemActiveGetter != null && this.itemActiveGetter.getAsBoolean();
    }

    public void setItemActive(boolean active) {
        if (this.itemActiveSetter != null) {
            this.itemActiveSetter.accept(active);
        }
    }

    public BooleanSupplier getItemActiveGetter() {
        return this.itemActiveGetter;
    }

    public Consumer<Boolean> getItemActiveSetter() {
        return this.itemActiveSetter;
    }

    public RotaConfigOption withItemToggle(BooleanSupplier getter, Consumer<Boolean> setter) {
        this.itemActiveGetter = getter;
        this.itemActiveSetter = setter;
        return this;
    }

    public boolean hasReorderActions() {
        return this.moveUpAction != null || this.moveDownAction != null;
    }

    public boolean isShowReorderButtons() {
        return this.showReorderButtons || this.hasReorderActions();
    }

    public RotaConfigOption withShowReorderButtons(boolean show) {
        this.showReorderButtons = show;
        return this;
    }

    public Runnable getMoveUpAction() {
        return this.moveUpAction;
    }

    public Runnable getMoveDownAction() {
        return this.moveDownAction;
    }

    public void moveUp() {
        if (this.moveUpAction != null) {
            this.moveUpAction.run();
        }
    }

    public void moveDown() {
        if (this.moveDownAction != null) {
            this.moveDownAction.run();
        }
    }

    public RotaConfigOption withReorderActions(Runnable moveUp, Runnable moveDown) {
        this.moveUpAction = moveUp;
        this.moveDownAction = moveDown;
        this.showReorderButtons = moveUp != null || moveDown != null;
        return this;
    }

    @FunctionalInterface
    public static interface CustomInspectorRenderer {
        public void render(GuiGraphics var1, int var2, int var3, int var4, int var5, float var6);
    }

    public static enum Type {
        HEADER,
        BOOLEAN,
        INT_SLIDER,
        DOUBLE_SLIDER,
        CYCLE,
        ACTION,
        STRING_INPUT,
        MULTILINE_STRING_INPUT,
        ITEM_ENTRY;

    }
}

