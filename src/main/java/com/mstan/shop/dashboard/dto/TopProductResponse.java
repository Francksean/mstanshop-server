package com.mstan.shop.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProductResponse(
        UUID productId,
        String productName,
        long quantitySold,
        BigDecimal revenue
) {
}
