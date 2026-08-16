package com.mstan.shop.category.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CategoryRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        String description,

        String bannerObjectKey,

        String thumbnailObjectKey,

        UUID parentId
) {
}
