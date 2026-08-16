package com.mstan.shop.category.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmImageRequest(

        @NotBlank(message = "La clé d'objet est obligatoire")
        String objectKey
) {
}
