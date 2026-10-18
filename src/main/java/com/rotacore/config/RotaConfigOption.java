package com.rotacore.config;

import net.minecraft.network.chat.Component;

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
    private final String exampleTip;
    private final String defaultValueText;

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

    // Getters
    public Type getType() { return type; }
    public Component getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExampleTip() { return exampleTip; }
    public String getDefaultValueText() { return defaultValueText; }

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
}
