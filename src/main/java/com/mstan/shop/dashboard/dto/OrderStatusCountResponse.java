package com.mstan.shop.dashboard.dto;

import com.mstan.shop.order.entity.OrderStatus;

public record OrderStatusCountResponse(
        OrderStatus status,
        long count
) {
}
