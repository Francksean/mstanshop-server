package com.mstan.shop.promo.dto;

import com.mstan.shop.promo.entity.PromoDiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromoCodeResponse(
        UUID id,
        String code,
        PromoDiscountType discountType,
        BigDecimal discountValue,
        Instant expiresAt,
        BigDecimal minOrderAmount,
        Integer maxUses,
        int usesCount,
        boolean active
) {
}
