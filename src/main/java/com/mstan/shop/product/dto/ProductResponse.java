package com.mstan.shop.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        BigDecimal oldPrice,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        BigDecimal purchasePrice,
        int stock,
        String categoryName,
        String thumbnailUrl,
        Double averageRating,
        long reviewCount
) {
}
