package com.rotacore.world.routes;

public enum WayBodyType {
    ROAD("Estrada", 32, 400, 3, 4, 0, true),
    BRIDGE("Ponte", 10, 80, 3, 0, 0, false),
    TUNNEL("Túnel", 16, 180, 5, 4, 0, true),
    MARITIME("Rota Marítima", 32, 500, 12, 0, 4, true),
    TRAIL("Trilha", 16, 120, 2, 3, 0, true);

    private final String displayName;
    private final int defaultMinLength;
    private final int defaultMaxLength;
    private final int defaultWidth;
    private final int defaultClearanceHeight;
    private final int defaultBarrierSpacing;
    private final boolean defaultAllowBodyMagnetism;

    WayBodyType(String displayName, int defaultMinLength, int defaultMaxLength, int defaultWidth,
                int defaultClearanceHeight, int defaultBarrierSpacing, boolean defaultAllowBodyMagnetism) {
        this.displayName = displayName;
        this.defaultMinLength = defaultMinLength;
        this.defaultMaxLength = defaultMaxLength;
        this.defaultWidth = defaultWidth;
        this.defaultClearanceHeight = defaultClearanceHeight;
        this.defaultBarrierSpacing = defaultBarrierSpacing;
        this.defaultAllowBodyMagnetism = defaultAllowBodyMagnetism;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultMinLength() {
        return defaultMinLength;
    }

    public int getDefaultMaxLength() {
        return defaultMaxLength;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public int getDefaultClearanceHeight() {
        return defaultClearanceHeight;
    }

    public int getDefaultBarrierSpacing() {
        return defaultBarrierSpacing;
    }

    public boolean isDefaultAllowBodyMagnetism() {
        return defaultAllowBodyMagnetism;
    }
}
