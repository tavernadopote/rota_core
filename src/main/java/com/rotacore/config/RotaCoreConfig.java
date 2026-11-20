/*
 * Decompiled with CFR 0.152.
 */
package com.rotacore.config;

import com.rotacore.world.naming.RouteNamingStyle;
import com.rotacore.world.routes.RoadCurvatureMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class RotaCoreConfig {
    public boolean showPauseMenuButton = true;
    public boolean showHubInOptionsScreen = true;
    public Map<String, Boolean> moduleOptionsEnabled = new LinkedHashMap<String, Boolean>();
    public boolean enableControlledWorldgen = false;
    public int regionalCellSizeChunks = 38;
    public int minVillageSeparationBlocks = 500;
    public int minOutpostFromVillageBlocks = 450;
    public boolean terrainSuitabilityCheck = true;
    public int maxVillageHeightVariance = 8;
    public RouteNamingStyle routeNamingStyle = RouteNamingStyle.FANTASY_KINGDOM;
    public RoadCurvatureMode roadCurvatureMode = RoadCurvatureMode.BALANCED;
    public Map<String, HudElementConfig> hudElements = new LinkedHashMap<String, HudElementConfig>();

    public boolean isModuleEnabledInOptions(String moduleId) {
        if (moduleId == null) {
            return false;
        }
        return this.moduleOptionsEnabled.getOrDefault(moduleId, true);
    }

    public void setModuleEnabledInOptions(String moduleId, boolean enabled) {
        if (moduleId != null) {
            this.moduleOptionsEnabled.put(moduleId, enabled);
        }
    }

    public HudElementConfig getHudElementConfig(String id) {
        if (id == null) {
            return new HudElementConfig();
        }
        if (this.hudElements == null) {
            this.hudElements = new LinkedHashMap<String, HudElementConfig>();
        }
        return this.hudElements.computeIfAbsent(id, k -> new HudElementConfig());
    }

    public void setHudElementPosition(String id, int x, int y) {
        if (id == null) {
            return;
        }
        HudElementConfig cfg = this.getHudElementConfig(id);
        cfg.customPosition = true;
        cfg.customX = x;
        cfg.customY = y;
    }

    public void resetHudElementPosition(String id) {
        if (id == null) {
            return;
        }
        HudElementConfig cfg = this.getHudElementConfig(id);
        cfg.customPosition = false;
        cfg.customX = 0;
        cfg.customY = 0;
    }

    public void resetAllHudPositions() {
        if (this.hudElements != null) {
            this.hudElements.clear();
        }
    }

    public void validate() {
        if (this.moduleOptionsEnabled == null) {
            this.moduleOptionsEnabled = new LinkedHashMap<String, Boolean>();
        }
        if (this.hudElements == null) {
            this.hudElements = new LinkedHashMap<String, HudElementConfig>();
        }
        for (Map.Entry<String, HudElementConfig> entry : this.hudElements.entrySet()) {
            if (entry.getValue() != null) continue;
            entry.setValue(new HudElementConfig());
        }
        if (this.regionalCellSizeChunks < 24) {
            this.regionalCellSizeChunks = 24;
        }
        if (this.regionalCellSizeChunks > 128) {
            this.regionalCellSizeChunks = 128;
        }
        if (this.minVillageSeparationBlocks < 300) {
            this.minVillageSeparationBlocks = 300;
        }
        if (this.minVillageSeparationBlocks > 2000) {
            this.minVillageSeparationBlocks = 2000;
        }
        if (this.minOutpostFromVillageBlocks < 200) {
            this.minOutpostFromVillageBlocks = 200;
        }
        if (this.minOutpostFromVillageBlocks > 1000) {
            this.minOutpostFromVillageBlocks = 1000;
        }
        if (this.maxVillageHeightVariance < 4) {
            this.maxVillageHeightVariance = 4;
        }
        if (this.maxVillageHeightVariance > 24) {
            this.maxVillageHeightVariance = 24;
        }
        if (this.routeNamingStyle == null) {
            this.routeNamingStyle = RouteNamingStyle.FANTASY_KINGDOM;
        }
        if (this.roadCurvatureMode == null) {
            this.roadCurvatureMode = RoadCurvatureMode.BALANCED;
        }
    }

    public static class HudElementConfig {
        public boolean customPosition = false;
        public int customX = 0;
        public int customY = 0;
        public String anchor = "DEFAULT";
        public boolean visible = true;

        public HudElementConfig() {
        }

        public HudElementConfig(boolean customPosition, int customX, int customY) {
            this.customPosition = customPosition;
            this.customX = customX;
            this.customY = customY;
        }
    }
}

