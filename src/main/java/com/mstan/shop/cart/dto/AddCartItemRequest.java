package com.mstan.shop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequest(

        @NotNull(message = "L'identifiant du produit est obligatoire")
        UUID productId,

        UUID variantId,

        @Min(value = 1, message = "La quantité doit être d'au moins 1")
        int quantity
) {
}
