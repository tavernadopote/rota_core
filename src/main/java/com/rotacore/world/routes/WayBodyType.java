package com.rotacore.world.routes;

public enum WayBodyType {
    ROAD("Estrada", 32, 400, 3, 4, 0, true, 0xFF3B4252, true, 3, 0.3, 0, false, false),
    BRIDGE("Ponte", 10, 80, 3, 0, 0, false, 0xFFD97706, true, 4, 0.05, 0, false, true),
    TUNNEL("Túnel", 16, 180, 5, 4, 0, true, 0xFF4C1D95, false, 2, 0.2, 6, true, true),
    MARITIME("Rota Marítima", 32, 500, 12, 0, 4, true, 0xFF0284C7, false, 0, 0.1, 0, false, false),
    TRAIL("Trilha", 16, 120, 2, 3, 0, true, 0xFF8B5A2B, false, 2, 0.8, 0, false, false);

    private final String displayName;
    private final int defaultMinLength;
    private final int defaultMaxLength;
    private final int defaultWidth;
    private final int defaultClearanceHeight;
    private final int defaultBarrierSpacing;
    private final boolean defaultAllowBodyMagnetism;
    private final int defaultMapColor;
    private final boolean defaultSmoothElevation;
    private final int defaultMinRunBlocksPerStep;
    private final double defaultHorizontalCurvatureWeight;
    private final int defaultMinOverheadRock;
    private final boolean defaultCanCarveSolid;
    private final boolean defaultDetectCaves;

    WayBodyType(String displayName, int defaultMinLength, int defaultMaxLength, int defaultWidth,
                int defaultClearanceHeight, int defaultBarrierSpacing, boolean defaultAllowBodyMagnetism,
                int defaultMapColor, boolean defaultSmoothElevation, int defaultMinRunBlocksPerStep,
                double defaultHorizontalCurvatureWeight, int defaultMinOverheadRock,
                boolean defaultCanCarveSolid, boolean defaultDetectCaves) {
        this.displayName = displayName;
        this.defaultMinLength = defaultMinLength;
        this.defaultMaxLength = defaultMaxLength;
        this.defaultWidth = defaultWidth;
        this.defaultClearanceHeight = defaultClearanceHeight;
        this.defaultBarrierSpacing = defaultBarrierSpacing;
        this.defaultAllowBodyMagnetism = defaultAllowBodyMagnetism;
        this.defaultMapColor = defaultMapColor;
        this.defaultSmoothElevation = defaultSmoothElevation;
        this.defaultMinRunBlocksPerStep = defaultMinRunBlocksPerStep;
        this.defaultHorizontalCurvatureWeight = defaultHorizontalCurvatureWeight;
        this.defaultMinOverheadRock = defaultMinOverheadRock;
        this.defaultCanCarveSolid = defaultCanCarveSolid;
        this.defaultDetectCaves = defaultDetectCaves;
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

    public int getDefaultMapColor() {
        return defaultMapColor;
    }

    public boolean isDefaultSmoothElevation() {
        return defaultSmoothElevation;
    }

    public int getDefaultMinRunBlocksPerStep() {
        return defaultMinRunBlocksPerStep;
    }

    public double getDefaultHorizontalCurvatureWeight() {
        return defaultHorizontalCurvatureWeight;
    }

    public int getDefaultMinOverheadRock() {
        return defaultMinOverheadRock;
    }

    public boolean isDefaultCanCarveSolid() {
        return defaultCanCarveSolid;
    }

    public boolean isDefaultDetectCaves() {
        return defaultDetectCaves;
    }
}
