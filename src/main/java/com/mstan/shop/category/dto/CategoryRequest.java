package com.mstan.shop.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        String description
) {
}
