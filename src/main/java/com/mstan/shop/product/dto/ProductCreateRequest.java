package com.mstan.shop.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductCreateRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        String description,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
        BigDecimal price,

        @DecimalMin(value = "0.0", message = "Le prix d'achat ne peut pas être négatif")
        BigDecimal purchasePrice,

        @NotNull(message = "Le stock est obligatoire")
        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stock,

        @NotNull(message = "La catégorie est obligatoire")
        UUID categoryId,

        UUID supplierId,

        @Valid
        List<ProductImageCreateItem> images,

        @Valid
        List<VariantCreateRequest> variants
) {
}
