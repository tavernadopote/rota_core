package com.rotacore.config;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.*;

/**
 * Representa um item ou configuração dentro de uma categoria de qualquer mod da Rota Mods.
 */
public class RotaConfigOption {

    public enum Type {
        HEADER,
        BOOLEAN,
        INT_SLIDER,
        DOUBLE_SLIDER,
        CYCLE,
        ACTION
    }

    private final Type type;
    private final Component title;
    private final String description;
    private String exampleTip;
    private final String defaultValueText;
    private boolean customTipExplicit = false;
    private boolean tipExplicitlyForced = false;
    private boolean showDefaultValue = false;

    // Callbacks para os controles
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

    private Runnable secondaryAction = null;
    private Component secondaryActionTooltip = Component.literal("Configurar Parâmetros...");

    private java.util.List<RotaIconPreview> iconPreviews = java.util.List.of();

    private ResourceLocation texturePreviewLocation = null;
    private int texturePreviewWidth = 128;
    private int texturePreviewHeight = 128;
    private Component texturePreviewCaption = null;

    private boolean requiresAdmin = false;
    private boolean clientOnly = false;

    private RotaConfigOption(Type type, Component title, String description, String exampleTip, String defaultValueText) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.exampleTip = exampleTip;
        this.defaultValueText = defaultValueText;
    }

    public static RotaConfigOption header(String title) {
        return new RotaConfigOption(Type.HEADER, Component.literal(title), "", "", "");
    }

    public static RotaConfigOption header(Component title) {
        return new RotaConfigOption(Type.HEADER, title, "", "", "");
    }

    public static RotaConfigOption booleanToggle(
            Component title,
            String description,
            String exampleTip,
            boolean defaultValue,
            BooleanSupplier getter,
            Consumer<Boolean> setter
    ) {
        RotaConfigOption opt = new RotaConfigOption(
                Type.BOOLEAN,
                title,
                description,
                exampleTip,
                defaultValue ? "Ativado (SIM)" : "Desativado (NÃO)"
        );
        opt.booleanGetter = getter;
        opt.booleanSetter = setter;
        return opt;
    }

    public static RotaConfigOption intSlider(
            Component title,
            String description,
            String exampleTip,
            int defaultValue,
            int min,
            int max,
            Function<Integer, String> formatter,
            IntSupplier getter,
            IntConsumer setter
    ) {
        RotaConfigOption opt = new RotaConfigOption(
                Type.INT_SLIDER,
                title,
                description,
                exampleTip,
                formatter != null ? formatter.apply(defaultValue) : String.valueOf(defaultValue)
        );
        opt.intMin = min;
        opt.intMax = max;
        opt.intFormatter = formatter != null ? formatter : String::valueOf;
        opt.intGetter = getter;
        opt.intSetter = setter;
        return opt;
    }

    public static RotaConfigOption doubleSlider(
            Component title,
            String description,
            String exampleTip,
            double defaultValue,
            double min,
            double max,
            Function<Double, String> formatter,
            DoubleSupplier getter,
            DoubleConsumer setter
    ) {
        RotaConfigOption opt = new RotaConfigOption(
                Type.DOUBLE_SLIDER,
                title,
                description,
                exampleTip,
                formatter != null ? formatter.apply(defaultValue) : String.valueOf(defaultValue)
        );
        opt.doubleMin = min;
        opt.doubleMax = max;
        opt.doubleFormatter = formatter != null ? formatter : (d) -> String.format("%.2f", d);
        opt.doubleGetter = getter;
        opt.doubleSetter = setter;
        return opt;
    }

    public static RotaConfigOption action(
            Component title,
            String description,
            String exampleTip,
            String buttonText,
            Runnable action
    ) {
        RotaConfigOption opt = new RotaConfigOption(
                Type.ACTION,
                title,
                description,
                exampleTip,
                "—"
        );
        opt.actionButtonText = buttonText;
        opt.actionCallback = action;
        return opt;
    }

    public static RotaConfigOption cycle(
            Component title,
            String description,
            String exampleTip,
            String defaultValueText,
            Supplier<String> getter,
            Runnable cycleAction
    ) {
        RotaConfigOption opt = new RotaConfigOption(
                Type.CYCLE,
                title,
                description,
                exampleTip,
                defaultValueText
        );
        opt.cycleGetter = getter;
        opt.cycleAction = cycleAction;
        return opt;
    }

    public static RotaConfigOption booleanToggle(
            Component title,
            String description,
            boolean defaultValue,
            BooleanSupplier getter,
            Consumer<Boolean> setter
    ) {
        return booleanToggle(title, description, null, defaultValue, getter, setter);
    }

    public static RotaConfigOption intSlider(
            Component title,
            String description,
            int defaultValue,
            int min,
            int max,
            Function<Integer, String> formatter,
            IntSupplier getter,
            IntConsumer setter
    ) {
        return intSlider(title, description, null, defaultValue, min, max, formatter, getter, setter);
    }

    public static RotaConfigOption doubleSlider(
            Component title,
            String description,
            double defaultValue,
            double min,
            double max,
            Function<Double, String> formatter,
            DoubleSupplier getter,
            DoubleConsumer setter
    ) {
        return doubleSlider(title, description, null, defaultValue, min, max, formatter, getter, setter);
    }

    public static RotaConfigOption action(
            Component title,
            String description,
            String buttonText,
            Runnable action
    ) {
        return action(title, description, null, buttonText, action);
    }

    public static RotaConfigOption cycle(
            Component title,
            String description,
            String defaultValueText,
            Supplier<String> getter,
            Runnable cycleAction
    ) {
        return cycle(title, description, null, defaultValueText, getter, cycleAction);
    }

    // Getters
    public Type getType() { return type; }
    public Component getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExampleTip() { return exampleTip; }
    public String getDefaultValueText() { return defaultValueText; }

    public boolean hasTip() {
        if (exampleTip == null || exampleTip.trim().isEmpty()) {
            return false;
        }
        if (customTipExplicit) {
            return true;
        }
        return !isGenericTip(exampleTip);
    }

    public boolean hasCustomTip() {
        return hasTip();
    }

    public static boolean isGenericTip(String tip) {
        if (tip == null || tip.trim().isEmpty()) {
            return true;
        }
        String lower = tip.trim().toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("ativado:") || lower.startsWith("ativado =") || lower.startsWith("ativado -")
                || lower.startsWith("desativado:") || lower.startsWith("desativado =") || lower.startsWith("desativado -")
                || lower.startsWith("padrão:") || lower.startsWith("padrao:") || lower.startsWith("padrão =") || lower.startsWith("padrao =")
                || lower.startsWith("padrão recomendado") || lower.startsWith("padrao recomendado");
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
        return customTipExplicit;
    }

    public RotaConfigOption withForceTip(boolean force) {
        this.tipExplicitlyForced = force;
        return this;
    }

    public boolean isTipExplicitlyForced() {
        return tipExplicitlyForced;
    }

    public RotaConfigOption withShowDefaultValue(boolean show) {
        this.showDefaultValue = show;
        return this;
    }

    public boolean shouldShowDefaultValue() {
        return showDefaultValue;
    }

    public boolean getBooleanValue() { return booleanGetter != null && booleanGetter.getAsBoolean(); }
    public void setBooleanValue(boolean v) { if (booleanSetter != null) booleanSetter.accept(v); }

    public int getIntValue() { return intGetter != null ? intGetter.getAsInt() : intMin; }
    public void setIntValue(int v) { if (intSetter != null) intSetter.accept(v); }
    public int getIntMin() { return intMin; }
    public int getIntMax() { return intMax; }
    public Function<Integer, String> getIntFormatter() { return intFormatter; }

    public double getDoubleValue() { return doubleGetter != null ? doubleGetter.getAsDouble() : doubleMin; }
    public void setDoubleValue(double v) { if (doubleSetter != null) doubleSetter.accept(v); }
    public double getDoubleMin() { return doubleMin; }
    public double getDoubleMax() { return doubleMax; }
    public Function<Double, String> getDoubleFormatter() { return doubleFormatter; }

    public String getCycleText() { return cycleGetter != null ? cycleGetter.get() : ""; }
    public void triggerCycle() { if (cycleAction != null) cycleAction.run(); }

    public String getActionButtonText() { return actionButtonText != null ? actionButtonText : "Abrir"; }
    public void triggerAction() { if (actionCallback != null) actionCallback.run(); }

    public RotaConfigOption withSecondaryAction(Runnable action, Component tooltip) {
        this.secondaryAction = action;
        this.secondaryActionTooltip = tooltip != null ? tooltip : Component.literal("Configurar Parâmetros...");
        return this;
    }

    public RotaConfigOption withSecondaryAction(Runnable action) {
        return withSecondaryAction(action, Component.literal("Configurar Parâmetros..."));
    }

    public boolean hasSecondaryAction() {
        return secondaryAction != null;
    }

    public Runnable getSecondaryAction() {
        return secondaryAction;
    }

    public Component getSecondaryActionTooltip() {
        return secondaryActionTooltip;
    }

    public RotaConfigOption withIconPreviews(java.util.List<RotaIconPreview> previews) {
        if (previews != null) {
            this.iconPreviews = java.util.List.copyOf(previews);
        } else {
            this.iconPreviews = java.util.List.of();
        }
        return this;
    }

    public java.util.List<RotaIconPreview> getIconPreviews() {
        return iconPreviews;
    }

    public boolean hasIconPreviews() {
        return iconPreviews != null && !iconPreviews.isEmpty();
    }

    public RotaConfigOption withTexturePreview(ResourceLocation texture, int width, int height, Component caption) {
        this.texturePreviewLocation = texture;
        this.texturePreviewWidth = width;
        this.texturePreviewHeight = height;
        this.texturePreviewCaption = caption;
        return this;
    }

    public RotaConfigOption withTexturePreview(ResourceLocation texture, int width, int height) {
        return withTexturePreview(texture, width, height, null);
    }

    public boolean hasTexturePreview() {
        return texturePreviewLocation != null;
    }

    public ResourceLocation getTexturePreviewLocation() {
        return texturePreviewLocation;
    }

    public int getTexturePreviewWidth() {
        return texturePreviewWidth;
    }

    public int getTexturePreviewHeight() {
        return texturePreviewHeight;
    }

    public Component getTexturePreviewCaption() {
        return texturePreviewCaption;
    }

    public RotaConfigOption requiresAdmin(boolean requiresAdmin) {
        this.requiresAdmin = requiresAdmin;
        return this;
    }

    public boolean requiresAdmin() {
        return requiresAdmin;
    }

    public RotaConfigOption clientOnly(boolean clientOnly) {
        this.clientOnly = clientOnly;
        return this;
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    public boolean clientOnly() {
        return clientOnly;
    }

    private boolean active = true;

    public RotaConfigOption active(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public boolean canPlayerEdit(Minecraft mc) {
        if (!active) {
            return false;
        }
        if (mc == null) {
            return true;
        }
        boolean isSingleplayer = mc.getSingleplayerServer() != null;
        boolean hasAdmin = mc.player != null && mc.player.hasPermissions(2);
        return canPlayerEdit(isSingleplayer, hasAdmin);
    }

    public boolean canPlayerEdit(boolean isSingleplayer, boolean hasAdminPermissions) {
        if (!active) {
            return false;
        }
        if (isSingleplayer) {
            return true;
        }
        if (!requiresAdmin) {
            return true;
        }
        return hasAdminPermissions;
    }
}
