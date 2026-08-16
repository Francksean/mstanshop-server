package com.mstan.shop.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        BigDecimal oldPrice,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        BigDecimal purchasePrice,
        int stock,
        boolean active,
        UUID categoryId,
        String categoryName,
        UUID supplierId,
        String supplierName,
        List<ProductImageResponse> images,
        List<VariantResponse> variants,
        Double averageRating,
        long reviewCount
) {
}
