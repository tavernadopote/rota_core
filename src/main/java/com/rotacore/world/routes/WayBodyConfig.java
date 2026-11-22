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
    private final List<WayConnectionRule> blacklist;

    public WayBodyConfig(WayBodyType bodyType, int minLength, int maxLength, int width,
                         int clearanceHeight, int barrierSpacing, boolean allowBodyMagnetism,
                         List<WayConnectionRule> blacklist) {
        this.bodyType = bodyType;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.width = width;
        this.clearanceHeight = clearanceHeight;
        this.barrierSpacing = barrierSpacing;
        this.allowBodyMagnetism = allowBodyMagnetism;
        this.blacklist = new ArrayList<>(blacklist != null ? blacklist : Collections.emptyList());
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
            return new WayBodyConfig(type, minLen, maxLen, width, clearance, barrierSpacing, allowMag, rules);
        } catch (Exception ignored) {
            return null;
        }
    }
}
