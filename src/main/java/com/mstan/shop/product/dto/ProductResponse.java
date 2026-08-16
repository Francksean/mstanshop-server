package com.mstan.shop.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        BigDecimal oldPrice,
        int stock,
        String categoryName,
        String thumbnailUrl,
        Double averageRating,
        long reviewCount
) {
}
