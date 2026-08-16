package com.mstan.shop.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record VariantCreateRequest(

        String colorName,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Le code couleur doit être un code hexadécimal valide (ex : #FF0000)")
        String colorHex,

        @NotEmpty(message = "Au moins une taille est obligatoire")
        List<@NotBlank(message = "Une taille ne peut pas être vide") String> sizes,

        @NotNull(message = "Le stock est obligatoire")
        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stock,

        boolean active
) {
}
