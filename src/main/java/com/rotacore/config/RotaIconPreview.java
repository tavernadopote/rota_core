package com.rotacore.config;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Representa um icone para exibicao visual em galerias de pre-visualizacao no painel Inspector do Rota Core.
 */
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
        return new RotaIconPreview(texture, Component.literal(label != null ? label : ""));
    }

    public static RotaIconPreview of(ResourceLocation texture, Component label) {
        return new RotaIconPreview(texture, label != null ? label : Component.empty());
    }
}
