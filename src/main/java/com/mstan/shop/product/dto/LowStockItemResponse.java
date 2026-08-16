package com.mstan.shop.product.dto;

import java.util.UUID;

public record LowStockItemResponse(
        UUID productId,
        String productName,
        UUID variantId,
        String variantColorName,
        String variantSize,
        int currentStock,
        int threshold
) {
}
