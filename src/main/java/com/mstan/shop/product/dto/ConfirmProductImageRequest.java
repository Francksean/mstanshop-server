package com.mstan.shop.product.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ConfirmProductImageRequest(

        @NotBlank(message = "La clé d'objet est obligatoire")
        String objectKey,

        boolean markAsThumbnail,

        UUID variantId
) {
}
