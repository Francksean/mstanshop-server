package com.mstan.shop.order.dto;

import com.mstan.shop.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Le statut est obligatoire")
        OrderStatus status
) {
}
