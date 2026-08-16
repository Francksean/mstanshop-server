package com.mstan.shop.category.dto;

import com.mstan.shop.promo.entity.PromoDiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        long productCount,
        String bannerUrl,
        String thumbnailUrl,
        boolean promoActive,
        PromoDiscountType promoDiscountType,
        BigDecimal promoDiscountValue,
        Instant promoEndsAt,
        UUID parentId,
        String parentName
) {
}
