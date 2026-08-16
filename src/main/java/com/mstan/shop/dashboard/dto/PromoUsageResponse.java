package com.mstan.shop.dashboard.dto;

import java.math.BigDecimal;

public record PromoUsageResponse(
        String code,
        int usesCount,
        BigDecimal totalDiscount
) {
}
