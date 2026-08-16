package com.mstan.shop.product.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductImageCreateItem(

        @NotBlank(message = "La clé d'objet est obligatoire")
        String objectKey,

        boolean markAsThumbnail
) {
}
