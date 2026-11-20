/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package com.rotacore.config;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record RotaIconPreview(ResourceLocation texture, Component label) {
    public RotaIconPreview {
        if (texture == null) {
            throw new IllegalArgumentException("A textura do icone de preview nao pode ser nula");
        }
        if (label == null) {
            label = Component.empty();
        }
    }

    public static RotaIconPreview of(ResourceLocation texture, String label) {
        return new RotaIconPreview(texture, (Component)Component.literal((String)(label != null ? label : "")));
    }

    public static RotaIconPreview of(ResourceLocation texture, Component label) {
        return new RotaIconPreview(texture, (Component)(label != null ? label : Component.empty()));
    }
}

