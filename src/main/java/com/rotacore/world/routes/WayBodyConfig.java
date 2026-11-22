package com.rotacore.world.routes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WayBodyConfig {
    private final WayBodyType bodyType;
    private int minLength;
    private int maxLength;
    private int width;
    private int clearanceHeight;
    private int barrierSpacing;
    private boolean allowBodyMagnetism;
    private int mapColor;
    private boolean smoothElevation;
    private int minRunBlocksPerStep;
    private double horizontalCurvatureWeight;
    private int minOverheadRock;
    private boolean canCarveSolid;
    private boolean detectCaves;
    private final List<String> biomeWhitelist;
    private final List<String> biomeBlacklist;
    private final List<WayConnectionRule> blacklist;

    public WayBodyConfig(WayBodyType bodyType, int minLength, int maxLength, int width,
                         int clearanceHeight, int barrierSpacing, boolean allowBodyMagnetism,
                         int mapColor, boolean smoothElevation, int minRunBlocksPerStep,
                         double horizontalCurvatureWeight, int minOverheadRock,
                         boolean canCarveSolid, boolean detectCaves,
                         List<String> biomeWhitelist, List<String> biomeBlacklist,
                         List<WayConnectionRule> blacklist) {
        this.bodyType = bodyType;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.width = width;
        this.clearanceHeight = clearanceHeight;
        this.barrierSpacing = barrierSpacing;
        this.allowBodyMagnetism = allowBodyMagnetism;
        this.mapColor = mapColor;
        this.smoothElevation = smoothElevation;
        this.minRunBlocksPerStep = minRunBlocksPerStep;
        this.horizontalCurvatureWeight = horizontalCurvatureWeight;
        this.minOverheadRock = minOverheadRock;
        this.canCarveSolid = canCarveSolid;
        this.detectCaves = detectCaves;
        this.biomeWhitelist = new ArrayList<>(biomeWhitelist != null ? biomeWhitelist : Collections.emptyList());
        this.biomeBlacklist = new ArrayList<>(biomeBlacklist != null ? biomeBlacklist : Collections.emptyList());
        this.blacklist = new ArrayList<>(blacklist != null ? blacklist : Collections.emptyList());
    }

    public WayBodyConfig(WayBodyType bodyType, int minLength, int maxLength, int width,
                         int clearanceHeight, int barrierSpacing, boolean allowBodyMagnetism,
                         int mapColor, List<WayConnectionRule> blacklist) {
        this(bodyType, minLength, maxLength, width, clearanceHeight, barrierSpacing, allowBodyMagnetism,
                mapColor,
                bodyType != null && bodyType.isDefaultSmoothElevation(),
                bodyType != null ? bodyType.getDefaultMinRunBlocksPerStep() : 2,
                bodyType != null ? bodyType.getDefaultHorizontalCurvatureWeight() : 0.3,
                bodyType != null ? bodyType.getDefaultMinOverheadRock() : 0,
                bodyType != null && bodyType.isDefaultCanCarveSolid(),
                bodyType != null && bodyType.isDefaultDetectCaves(),
                Collections.emptyList(), Collections.emptyList(), blacklist);
    }

    public WayBodyConfig(WayBodyType bodyType, int minLength, int maxLength, int width,
                         int clearanceHeight, int barrierSpacing, boolean allowBodyMagnetism,
                         List<WayConnectionRule> blacklist) {
        this(bodyType, minLength, maxLength, width, clearanceHeight, barrierSpacing, allowBodyMagnetism,
                bodyType != null ? bodyType.getDefaultMapColor() : 0xFFFFFFFF, blacklist);
    }

    public WayBodyType getBodyType() {
        return bodyType;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getClearanceHeight() {
        return clearanceHeight;
    }

    public void setClearanceHeight(int clearanceHeight) {
        this.clearanceHeight = clearanceHeight;
    }

    public boolean hasClearance() {
        return this.clearanceHeight > 0;
    }

    public int getBarrierSpacing() {
        return barrierSpacing;
    }

    public void setBarrierSpacing(int barrierSpacing) {
        this.barrierSpacing = barrierSpacing;
    }

    public boolean isAllowBodyMagnetism() {
        return allowBodyMagnetism;
    }

    public void setAllowBodyMagnetism(boolean allowBodyMagnetism) {
        this.allowBodyMagnetism = allowBodyMagnetism;
    }

    public int getMapColor() {
        return mapColor;
    }

    public void setMapColor(int mapColor) {
        this.mapColor = mapColor;
    }

    public boolean isSmoothElevation() {
        return smoothElevation;
    }

    public void setSmoothElevation(boolean smoothElevation) {
        this.smoothElevation = smoothElevation;
    }

    public int getMinRunBlocksPerStep() {
        return minRunBlocksPerStep;
    }

    public void setMinRunBlocksPerStep(int minRunBlocksPerStep) {
        this.minRunBlocksPerStep = minRunBlocksPerStep;
    }

    public double getHorizontalCurvatureWeight() {
        return horizontalCurvatureWeight;
    }

    public void setHorizontalCurvatureWeight(double horizontalCurvatureWeight) {
        this.horizontalCurvatureWeight = horizontalCurvatureWeight;
    }

    public int getMinOverheadRock() {
        return minOverheadRock;
    }

    public void setMinOverheadRock(int minOverheadRock) {
        this.minOverheadRock = minOverheadRock;
    }

    public boolean isCanCarveSolid() {
        return canCarveSolid;
    }

    public void setCanCarveSolid(boolean canCarveSolid) {
        this.canCarveSolid = canCarveSolid;
    }

    public boolean isDetectCaves() {
        return detectCaves;
    }

    public void setDetectCaves(boolean detectCaves) {
        this.detectCaves = detectCaves;
    }

    public List<String> getBiomeWhitelist() {
        return Collections.unmodifiableList(biomeWhitelist);
    }

    public void addBiomeToWhitelist(String biomeId) {
        if (biomeId != null && !this.biomeWhitelist.contains(biomeId)) {
            this.biomeWhitelist.add(biomeId);
        }
    }

    public void removeBiomeFromWhitelist(String biomeId) {
        this.biomeWhitelist.remove(biomeId);
    }

    public List<String> getBiomeBlacklist() {
        return Collections.unmodifiableList(biomeBlacklist);
    }

    public void addBiomeToBlacklist(String biomeId) {
        if (biomeId != null && !this.biomeBlacklist.contains(biomeId)) {
            this.biomeBlacklist.add(biomeId);
        }
    }

    public void removeBiomeFromBlacklist(String biomeId) {
        this.biomeBlacklist.remove(biomeId);
    }

    public boolean isBiomeAllowed(String biomeId) {
        if (biomeId == null) {
            return true;
        }
        if (!this.biomeBlacklist.isEmpty() && this.biomeBlacklist.contains(biomeId)) {
            return false;
        }
        if (!this.biomeWhitelist.isEmpty()) {
            return this.biomeWhitelist.contains(biomeId);
        }
        return true;
    }

    public List<WayConnectionRule> getBlacklist() {
        return Collections.unmodifiableList(blacklist);
    }

    public void addRule(WayConnectionRule rule) {
        if (rule != null) {
            this.blacklist.removeIf(r -> r.targetType() == rule.targetType());
            this.blacklist.add(rule);
        }
    }

    public void removeRule(WayBodyType targetType) {
        this.blacklist.removeIf(r -> r.targetType() == targetType);
    }

    public boolean canConnectPoleToPole(WayBodyType other) {
        if (other == null) {
            return false;
        }
        for (WayConnectionRule rule : this.blacklist) {
            if (rule.targetType() == other && rule.blockPole()) {
                return false;
            }
        }
        return true;
    }

    public boolean canConnectPoleToBody(WayBodyType other) {
        if (other == null) {
            return false;
        }
        for (WayConnectionRule rule : this.blacklist) {
            if (rule.targetType() == other && rule.blockBody()) {
                return false;
            }
        }
        return true;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("body_type", this.bodyType.name());
        obj.addProperty("min_length", this.minLength);
        obj.addProperty("max_length", this.maxLength);
        obj.addProperty("width", this.width);
        obj.addProperty("clearance_height", this.clearanceHeight);
        obj.addProperty("barrier_spacing", this.barrierSpacing);
        obj.addProperty("allow_body_magnetism", this.allowBodyMagnetism);
        obj.addProperty("map_color", String.format("#%08X", this.mapColor));
        obj.addProperty("smooth_elevation", this.smoothElevation);
        obj.addProperty("min_run_blocks_per_step", this.minRunBlocksPerStep);
        obj.addProperty("horizontal_curvature_weight", this.horizontalCurvatureWeight);
        obj.addProperty("min_overhead_rock", this.minOverheadRock);
        obj.addProperty("can_carve_solid", this.canCarveSolid);
        obj.addProperty("detect_caves", this.detectCaves);

        JsonArray wlArr = new JsonArray();
        for (String b : this.biomeWhitelist) {
            wlArr.add(b);
        }
        obj.add("biome_whitelist", wlArr);

        JsonArray blArr = new JsonArray();
        for (String b : this.biomeBlacklist) {
            blArr.add(b);
        }
        obj.add("biome_blacklist", blArr);

        JsonArray rulesArray = new JsonArray();
        for (WayConnectionRule rule : this.blacklist) {
            rulesArray.add(rule.toJson());
        }
        obj.add("blacklist", rulesArray);
        return obj;
    }

    public static WayBodyConfig fromJson(JsonObject obj) {
        if (obj == null || !obj.has("body_type")) {
            return null;
        }
        try {
            WayBodyType type = WayBodyType.valueOf(obj.get("body_type").getAsString());
            int minLen = obj.has("min_length") ? obj.get("min_length").getAsInt() : type.getDefaultMinLength();
            int maxLen = obj.has("max_length") ? obj.get("max_length").getAsInt() : type.getDefaultMaxLength();
            int width = obj.has("width") ? obj.get("width").getAsInt() : type.getDefaultWidth();
            int clearance = obj.has("clearance_height") ? obj.get("clearance_height").getAsInt() : type.getDefaultClearanceHeight();
            int barrierSpacing = obj.has("barrier_spacing") ? obj.get("barrier_spacing").getAsInt() : type.getDefaultBarrierSpacing();
            boolean allowMag = !obj.has("allow_body_magnetism") || obj.get("allow_body_magnetism").getAsBoolean();

            int color = type.getDefaultMapColor();
            if (obj.has("map_color")) {
                String colorStr = obj.get("map_color").getAsString();
                if (colorStr.startsWith("#")) {
                    color = (int) Long.parseLong(colorStr.substring(1), 16);
                } else {
                    color = obj.get("map_color").getAsInt();
                }
            }

            boolean smoothElev = obj.has("smooth_elevation") ? obj.get("smooth_elevation").getAsBoolean() : type.isDefaultSmoothElevation();
            int minRun = obj.has("min_run_blocks_per_step") ? obj.get("min_run_blocks_per_step").getAsInt() : type.getDefaultMinRunBlocksPerStep();
            double curvWeight = obj.has("horizontal_curvature_weight") ? obj.get("horizontal_curvature_weight").getAsDouble() : type.getDefaultHorizontalCurvatureWeight();
            int minOverhead = obj.has("min_overhead_rock") ? obj.get("min_overhead_rock").getAsInt() : type.getDefaultMinOverheadRock();
            boolean canCarve = obj.has("can_carve_solid") ? obj.get("can_carve_solid").getAsBoolean() : type.isDefaultCanCarveSolid();
            boolean detectCaves = obj.has("detect_caves") ? obj.get("detect_caves").getAsBoolean() : type.isDefaultDetectCaves();

            List<String> wl = new ArrayList<>();
            if (obj.has("biome_whitelist")) {
                for (JsonElement el : obj.getAsJsonArray("biome_whitelist")) {
                    wl.add(el.getAsString());
                }
            }

            List<String> bl = new ArrayList<>();
            if (obj.has("biome_blacklist")) {
                for (JsonElement el : obj.getAsJsonArray("biome_blacklist")) {
                    bl.add(el.getAsString());
                }
            }

            ArrayList<WayConnectionRule> rules = new ArrayList<>();
            if (obj.has("blacklist")) {
                for (JsonElement el : obj.getAsJsonArray("blacklist")) {
                    if (el.isJsonObject()) {
                        WayConnectionRule rule = WayConnectionRule.fromJson(el.getAsJsonObject());
                        if (rule != null) {
                            rules.add(rule);
                        }
                    }
                }
            }
            return new WayBodyConfig(type, minLen, maxLen, width, clearance, barrierSpacing, allowMag, color,
                    smoothElev, minRun, curvWeight, minOverhead, canCarve, detectCaves, wl, bl, rules);
        } catch (Exception ignored) {
            return null;
        }
    }
}
