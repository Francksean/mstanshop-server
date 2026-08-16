package com.mstan.shop.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GuestOrderItemRequest(

        @NotNull(message = "Le produit est obligatoire")
        UUID productId,

        UUID variantId,

        @Min(value = 1, message = "La quantité doit être d'au moins 1")
        int quantity
) {
}
