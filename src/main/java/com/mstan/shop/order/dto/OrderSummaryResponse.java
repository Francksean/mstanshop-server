package com.mstan.shop.order.dto;

import com.mstan.shop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        OrderCustomerResponse customer,
        Instant createdAt
) {
}
