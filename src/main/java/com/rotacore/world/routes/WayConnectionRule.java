package com.rotacore.world.routes;

import com.google.gson.JsonObject;

public record WayConnectionRule(
        WayBodyType targetType,
        boolean blockPole,
        boolean blockBody
) {
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("target_type", targetType.name());
        obj.addProperty("block_pole", blockPole);
        obj.addProperty("block_body", blockBody);
        return obj;
    }

    public static WayConnectionRule fromJson(JsonObject obj) {
        if (obj == null || !obj.has("target_type")) {
            return null;
        }
        try {
            WayBodyType type = WayBodyType.valueOf(obj.get("target_type").getAsString());
            boolean blockPole = obj.has("block_pole") && obj.get("block_pole").getAsBoolean();
            boolean blockBody = obj.has("block_body") && obj.get("block_body").getAsBoolean();
            return new WayConnectionRule(type, blockPole, blockBody);
        } catch (Exception ignored) {
            return null;
        }
    }
}
