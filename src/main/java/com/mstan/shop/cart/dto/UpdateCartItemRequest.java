package com.mstan.shop.cart.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(

        @Min(value = 1, message = "La quantité doit être d'au moins 1")
        int quantity
) {
}
