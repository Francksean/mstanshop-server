package com.mstan.shop.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal revenue,
        long orderCount,
        BigDecimal averageBasket,
        long pendingOrders,
        long lowStockCount,
        long newNewsletterSubscribers
) {
}
